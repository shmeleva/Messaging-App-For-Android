package xyz.shmeleva.eight.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_private_chat_settings.*

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.activities.BaseFragmentActivity
import xyz.shmeleva.eight.activities.ChatActivity
import xyz.shmeleva.eight.models.Chat
import xyz.shmeleva.eight.models.User
import xyz.shmeleva.eight.utilities.DoubleClickBlocker

class PrivateChatSettingsFragment : Fragment() {

    private val TAG = "PrivChatSettingsFrgmnt"

    private var shouldLaunchChat: Boolean? = null
    private var user: User? = null

    private var fragmentInteractionListener: OnFragmentInteractionListener? = null
    private val doubleClickBlocker: DoubleClickBlocker = DoubleClickBlocker()

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            shouldLaunchChat = arguments!!.getBoolean(ARG_SHOULD_LAUNCH_CHAT)
        }
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_private_chat_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        privateChatBackButton.setOnClickListener {_ ->
            if (doubleClickBlocker.isSingleClick()) {
                activity?.onBackPressed()
            }
        }
        privateChatStartFab.setOnClickListener {_ ->
            if (doubleClickBlocker.isSingleClick()) {
                if (shouldLaunchChat == true) getOrCreatePrivateChat() else activity?.onBackPressed()
            }
        }
        privateChatGalleryRelativeLayout.setOnClickListener { _ ->
            if (doubleClickBlocker.isSingleClick()) {
                (activity as BaseFragmentActivity).addFragment(GalleryFragment.newInstance(true))
            }
        }
    }

    fun setUser(targetUser: User) {
        user = targetUser
    }

    private fun activateChat(chatId: String) {
        val chatActivityIntent = Intent(activity, ChatActivity::class.java)
        chatActivityIntent.putExtra("chatId", chatId)
        startActivity(chatActivityIntent)
        activity?.finishAfterTransition()
    }

    private fun createChat(memberIds: Set<String>) {
        val newChatId: String = database.child("chats").push().key!!
        val newChat = Chat(
                id = newChatId,
                isGroupChat = memberIds.size > 2,
                members = memberIds.map { it to true }.toMap()
        )

        val childUpdates = HashMap<String, Any>()
        childUpdates["/chats/$newChatId"] = newChat.toMap()

        for (userId in memberIds) {
            childUpdates["/users/$userId/chats/$newChatId"] = mapOf("joinedAt" to newChat.updatedAt)
        }

        database.updateChildren(childUpdates)
                .addOnSuccessListener {
                    activateChat(newChatId)
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to update new chat $newChatId: ${it.message}")
                }
    }

    private fun getOrCreatePrivateChat() {
        database.child("chats").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val selectedUserIds = setOf(auth.currentUser!!.uid, user!!.id)

                    for (chatSnapshot in snapshot.children) {
                        val chat = chatSnapshot.getValue(Chat::class.java)

                        if (chat != null && chat.members.keys == selectedUserIds) {
                            Log.i(TAG, "Found chat ${chat.id}")
                            activateChat(chat.id)
                            return
                        }
                    }

                    Log.i(TAG, "Private chat not found")
                    createChat(selectedUserIds)
                }
            }

            override fun onCancelled(e: DatabaseError) {
                Log.e(TAG, "Failed to retrieve chats: ${e.message}")
            }
        })
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            fragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        fragmentInteractionListener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private val ARG_SHOULD_LAUNCH_CHAT = "shouldLaunchChat"

        fun newInstance(shouldLaunchChat: Boolean): PrivateChatSettingsFragment {
            val fragment = PrivateChatSettingsFragment()
            val args = Bundle()
            args.putBoolean(ARG_SHOULD_LAUNCH_CHAT, shouldLaunchChat)
            fragment.arguments = args
            return fragment
        }
    }
}
