package xyz.shmeleva.eight.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.adapters.ChatListAdapter
import xyz.shmeleva.eight.models.Chat
import kotlinx.android.synthetic.main.fragment_chat_list.*
import xyz.shmeleva.eight.activities.BaseFragmentActivity
import xyz.shmeleva.eight.activities.ChatActivity
import xyz.shmeleva.eight.activities.SearchActivity
import xyz.shmeleva.eight.activities.SettingsActivity
import xyz.shmeleva.eight.models.User
import xyz.shmeleva.eight.utilities.DoubleClickBlocker

class ChatListFragment : Fragment() {

    private val TAG: String = "ChatListFragment"

    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null
    private val doubleClickBlocker: DoubleClickBlocker = DoubleClickBlocker()

    private var isFabOpen = false

    private var fabOpenAnimation: Animation? = null
    private var fabCloseAnimation: Animation? = null
    private var rotateForwardAnimation: Animation? = null
    private var rotateBackwardAnimation: Animation? = null

    private val chats = ArrayList<Chat>()
    private lateinit var adapter: ChatListAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var userListener: ValueEventListener
    private var chatsListener: ValueEventListener? = null
    private var usersListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fabOpenAnimation = AnimationUtils.loadAnimation(activity, R.anim.fab_open)
        fabCloseAnimation = AnimationUtils.loadAnimation(activity,R.anim.fab_close)
        rotateForwardAnimation = AnimationUtils.loadAnimation(activity,R.anim.rotate_forward)
        rotateBackwardAnimation = AnimationUtils.loadAnimation(activity,R.anim.rotate_backward)

        chatListStartChatFab.setOnClickListener { _ ->
            if (doubleClickBlocker.isSingleClick()) {
                if (isFabOpen) closeFab() else openFab()
            }
        }

        chatListStartGroupChatFab.setOnClickListener { _ ->
            if (doubleClickBlocker.isSingleClick()) {
                closeFab()
                val intent = Intent(activity, SearchActivity::class.java)
                intent.putExtra(SearchFragment.ARG_SOURCE, SearchFragment.SOURCE_NEW_GROUP_CHAT)
                startActivity(intent)
            }
        }

        chatListStartPrivateChatFab.setOnClickListener { _ ->
            if (doubleClickBlocker.isSingleClick()) {
                closeFab()
                val intent = Intent(activity, SearchActivity::class.java)
                intent.putExtra(SearchFragment.ARG_SOURCE, SearchFragment.SOURCE_NEW_PRIVATE_CHAT)
                startActivity(intent)
            }
        }

        chatListSettingsButton.setOnClickListener { _ ->
            if (doubleClickBlocker.isSingleClick()) {
                val intent = Intent(activity, SettingsActivity::class.java)
                startActivity(intent)
            }
        }

        chatListSearchButton.setOnClickListener { _ ->
            if (doubleClickBlocker.isSingleClick()) {
                val intent = Intent(activity, SearchActivity::class.java)
                intent.putExtra(SearchFragment.ARG_SOURCE, SearchFragment.SOURCE_SEARCH)
                startActivity(intent)
            }
        }

        chatListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        adapter = ChatListAdapter(chats, { chat : Chat -> onChatClicked(chat) }, auth.currentUser!!.uid)
        chatListRecyclerView.adapter = adapter

        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("________", "getInstanceId failed", task.exception)
                    }

                    val token = task.result!!.token
                    database.child("tokens").child(auth.uid!!).setValue(token)
                }
    }

    private fun detachUsersListener() {
        if (usersListener != null) {
            database.child("users").removeEventListener(usersListener!!)
            Log.i(TAG, "Detached usersListener")
        }
    }

    private fun attachUsersListener() {
        detachUsersListener()

        if (usersListener == null) {
            usersListener = object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(TAG, "users onDataChange!")

                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)

                            if (user != null) {
                                for (chat in chats) {
                                    if (chat.isMember(user.id)) {
                                        chat.updateMember(user)
                                    }
                                }
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(e: DatabaseError) {
                    Log.e(TAG, "Failed to retrieve users: ${e.message}")
                }
            }
        }

        database.child("users").addValueEventListener(usersListener!!)
        Log.i(TAG, "Attached usersListener")
    }

    private fun detachChatsListener() {
        if (chatsListener != null) {
            database.child("chats").removeEventListener(chatsListener!!)
            Log.i(TAG, "Detached chatsListener")
        }
    }

    private fun attachChatsListener() {
        detachChatsListener()

        if (chatsListener == null) {
            chatsListener = object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(TAG, "chats onDataChange!")
                    if (snapshot.exists()) {
                        for (chatSnapshot in snapshot.children) {
                            val chat = chatSnapshot.getValue(Chat::class.java)

                            if (chat != null && chats.contains(chat)) {
                                val oldChat = chats[chats.indexOf(chat)]
                                chat.joinedAt = oldChat.joinedAt
                                chat.users = oldChat.users

                                // Remove users who aren't members anymore
                                val usersToRemove = arrayListOf<User>()
                                for (user in chat.users) {
                                    if (!chat.members.containsKey(user.id)) {
                                        usersToRemove.add(user)
                                    }
                                }
                                chat.users.removeAll(usersToRemove)

                                chats.remove(oldChat)
                                chats.add(chat)
                            }
                        }

                        sortChats()
                        adapter.notifyDataSetChanged()

                        attachUsersListener()
                    }
                }

                override fun onCancelled(e: DatabaseError) {
                    Log.e(TAG, "Failed to retrieve chats: ${e.message}")
                }
            }
        }

        database.child("chats").addValueEventListener(chatsListener!!)
        Log.i(TAG, "Attached chatsListener")
    }

    private fun sortChats() {
        chats.sortWith(compareByDescending<Chat> { it.updatedAt }
                .thenByDescending { it.id })
        Log.i(TAG, "Finished sorting")
    }

    override fun onStart() {
        super.onStart()
        userListener = object: ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                Log.i(TAG, "users/${auth.currentUser!!.uid} onDataChange!")

                if (userSnapshot.exists()) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        // Remove no longer valid chats
                        val chatsToRemove = arrayListOf<Chat>()
                        for (chat in chats) {
                            if (!user.chats.containsKey(chat.id)) {
                                chatsToRemove.add(chat)
                            }
                        }
                        chats.removeAll(chatsToRemove)

                        // Add user's new chats
                        for ((chatId, value) in user.chats) {
                            val joinedAt: Long = value["joinedAt"]!!
                            val chat = Chat(id = chatId)
                            chat.joinedAt = joinedAt

                            if (!chats.contains(chat)) {
                                chats.add(chat)
                            }
                        }

                        sortChats()
                        adapter.notifyDataSetChanged()

                        attachChatsListener()
                    }
                }
            }

            override fun onCancelled(e: DatabaseError) {
                Log.e(TAG, "Failed to retrieve user ${auth.currentUser!!.uid}'s chats: $e.message")
            }
        }
        database.child("users").child(auth.currentUser!!.uid).addValueEventListener(userListener)
        Log.i(TAG, "Attached userListener")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "Detach all listeners")
        database.child("users").child(auth.currentUser!!.uid).removeEventListener(userListener)
        detachChatsListener()
        detachUsersListener()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun openFab() {
        chatListStartChatFab.startAnimation(rotateForwardAnimation);
        chatListStartGroupChatFab.startAnimation(fabOpenAnimation);
        chatListStartPrivateChatFab.startAnimation(fabOpenAnimation);
        //
        chatListStartGroupChatFab.isClickable = true;
        chatListStartPrivateChatFab.isClickable = true;
        //
        isFabOpen = true;
    }

    private fun closeFab() {
        chatListStartChatFab.startAnimation(rotateBackwardAnimation);
        chatListStartGroupChatFab.startAnimation(fabCloseAnimation);
        chatListStartPrivateChatFab.startAnimation(fabCloseAnimation);
        //
        chatListStartGroupChatFab.isClickable = false;
        chatListStartPrivateChatFab.isClickable = false;
        //
        isFabOpen = false;
    }

    private fun onChatClicked(chat : Chat) {
        val chatActivityIntent = Intent(activity, ChatActivity::class.java)
        chatActivityIntent.putExtra("chatId", chat.id)
        chatActivityIntent.putExtra("isGroupChat", chat.isGroupChat)
        startActivity(chatActivityIntent)
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): ChatListFragment {
            val fragment = ChatListFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
