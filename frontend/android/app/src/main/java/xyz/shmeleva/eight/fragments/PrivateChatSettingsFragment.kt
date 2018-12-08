package xyz.shmeleva.eight.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import jp.wasabeef.glide.transformations.MaskTransformation
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_private_chat_settings.*

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.activities.BaseFragmentActivity
import xyz.shmeleva.eight.activities.ChatActivity
import xyz.shmeleva.eight.models.Chat
import xyz.shmeleva.eight.models.User
import xyz.shmeleva.eight.utilities.DoubleClickBlocker
import xyz.shmeleva.eight.utilities.loadFullProfilePictureFromFirebase

class PrivateChatSettingsFragment : Fragment() {

    private val TAG = "PrivChatSettingsFrgmnt"

    private var shouldLaunchChat: Boolean? = null
    private var targetUser: User? = null
    private var chatId: String? = null
    private var sourceUserId: String? = null
    private var joinedAt: Long = 0

    private var fragmentInteractionListener: OnFragmentInteractionListener? = null
    private val doubleClickBlocker: DoubleClickBlocker = DoubleClickBlocker()

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            shouldLaunchChat = arguments!!.getBoolean(ARG_SHOULD_LAUNCH_CHAT)
            chatId = arguments!!.getString(ARG_CHAT_ID)
            sourceUserId = arguments!!.getString(ARG_SOURCE_USER_ID)
            joinedAt = arguments!!.getLong(ARG_JOINED_AT)
        }
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storageRef = FirebaseStorage.getInstance().reference
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_private_chat_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (shouldLaunchChat!!) {
            privateChatGalleryCardView.visibility = View.GONE
        }

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
                (activity as BaseFragmentActivity).addFragment(GalleryFragment.newInstance(chatId,true, joinedAt))
            }
        }

        // populate user info
        if (targetUser == null) {
            getTargetUserAndPopulate()
        } else {
            setUsernameLabel(targetUser!!.username)
            populateProfilePhotoFromDB(targetUser!!)
        }

    }

    fun setUser(targetUser: User) {
        this.targetUser = targetUser
    }

    private fun activateChat(chat: Chat) {
        val chatActivityIntent = Intent(activity, ChatActivity::class.java)
        chatActivityIntent.putExtra("chatId", chat.id)
        chatActivityIntent.putExtra("isGroupChat", chat.isGroupChat)
        chatActivityIntent.putExtra("joinedAt", chat.joinedAt)
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
                    activateChat(newChat)
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to update new chat $newChatId: ${it.message}")
                }
    }

    private fun getOrCreatePrivateChat() {
        database.child("chats").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val selectedUserIds = setOf(auth.currentUser!!.uid, targetUser!!.id)

                    for (chatSnapshot in snapshot.children) {
                        val chat = chatSnapshot.getValue(Chat::class.java)

                        if (chat != null && chat.members.keys == selectedUserIds) {
                            Log.i(TAG, "Found chat ${chat.id}")
                            activateChat(chat)
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
        private val ARG_CHAT_ID = "chatID"
        private val ARG_SOURCE_USER_ID = "sourceUserId"
        private val ARG_JOINED_AT = "joinedAt"

        fun newInstance(shouldLaunchChat: Boolean, chatID: String?, sourceUserId: String?, joinedAt: Long): PrivateChatSettingsFragment {
            val fragment = PrivateChatSettingsFragment()
            val args = Bundle()
            args.putBoolean(ARG_SHOULD_LAUNCH_CHAT, shouldLaunchChat)
            args.putString(ARG_CHAT_ID, chatID)
            args.putString(ARG_SOURCE_USER_ID, sourceUserId)
            args.putLong(ARG_JOINED_AT, joinedAt)
            fragment.arguments = args
            return fragment
        }
    }

    private fun getTargetUserAndPopulate() {
        var targetUserOneTimeListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value == null) {
                    Log.i(TAG, "User not found.")
                    //logOut(logOutTextView)
                    return
                }

                // Get User object and use the values to update the UI
                val currentUser = dataSnapshot.getValue(User::class.java)!!

                setUsernameLabel(currentUser.username)
                populateProfilePhotoFromDB(currentUser)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, databaseError.message)
            }
        }

        val chatOneTimeListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value == null) {
                    Log.i(TAG, "Chat not found")
                    return
                }

                val chat = dataSnapshot.getValue(Chat::class.java)!!
                var targetUserId : String? = null

                // this is a private chat, so there are exactly 2 members
                chat.members.forEach{
                    if (it.key != sourceUserId) {
                        targetUserId = it.key
                    }
                }

                database.child("users").child(targetUserId!!)
                        .addListenerForSingleValueEvent(targetUserOneTimeListener)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, databaseError.message)
            }
        }

        database.child("chats").child(chatId!!)
                .addListenerForSingleValueEvent(chatOneTimeListener)

    }

    fun setUsernameLabel(username: String) {
        privateChatUsernameTextView.setText(username)
    }

    private fun populateProfilePhotoFromDB(user: User) {
        privateChatPictureImageView.loadFullProfilePictureFromFirebase(user)
    }
}
