package xyz.shmeleva.eight.fragments

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import kotlinx.android.synthetic.main.fragment_chat.*

import com.stfalcon.multiimageview.MultiImageView

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.activities.BaseFragmentActivity
import xyz.shmeleva.eight.adapters.MessageListAdapter
import xyz.shmeleva.eight.models.Chat
import xyz.shmeleva.eight.models.Message
import xyz.shmeleva.eight.models.User
import xyz.shmeleva.eight.utilities.*
import java.io.ByteArrayOutputStream
import java.util.*

class ChatFragment : Fragment() {

    private val TAG = "ChatFragment"

    private var chatId: String? = null
    private var isGroupChat: Boolean = false
    private var joinedAt: Long = 0

    private var chat: Chat? = null
    private var shouldScrollToBottom = true

    private var fragmentInteractionListener: OnFragmentInteractionListener? = null
    private val doubleClickBlocker: DoubleClickBlocker = DoubleClickBlocker()

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference

    private var chatListener: ValueEventListener? = null
    private var usersListener: ValueEventListener? = null

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            chatId = arguments!!.getString(ARG_CHAT_ID)
            isGroupChat = arguments!!.getBoolean(ARG_IS_GROUP_CHAT)
            joinedAt = arguments!!.getLong(ARG_JOINED_AT)
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatImageView.shape = MultiImageView.Shape.CIRCLE

        // Example of loading a picture:
        chatImageView.loadImages(arrayListOf("https://pixel.nymag.com/imgs/daily/vulture/2016/11/23/23-san-junipero.w330.h330.jpg"), 40 ,0)

        chatBackButton.setOnClickListener { _ ->
            if (doubleClickBlocker.isSingleClick()) {
                activity?.onBackPressed()
            }
        }
        chatImageView.setOnClickListener {_ ->
            if (doubleClickBlocker.isSingleClick()) {
                openChatSettings()
            }
        }
        chatTitleTextView.setOnClickListener {_ ->
            if (doubleClickBlocker.isSingleClick()) {
                openChatSettings()
            }
        }
        chatActionImageButton.setOnClickListener {_ ->
            if (doubleClickBlocker.isSingleClick()) {
                val message = chatEditText.text.toString()
                if (message.isBlank()) {
                    sendImage()
                }
                else {
                    sendMessage(message)
                }
            }
        }

        chatEditText.addTextChangedListener((object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val message = chatEditText.text.toString()
                chatActionImageButton.setImageResource(if (message.isBlank()) R.drawable.ic_baseline_photo_camera_24px else R.drawable.ic_baseline_send_24px)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }))

        val query = database.child("chatMessages").child(chatId!!).orderByChild("timestamp").startAt(joinedAt.toDouble())
        val options = FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(query, Message::class.java)
                .build()
        adapter = MessageListAdapter(
                userId = auth.currentUser!!.uid,
                isGroupChat = isGroupChat,
                options = options,
                clickListener = { chat : Message -> onMessageClicked(chat) }
        )

        layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val lastMessage = adapter.getItem(adapter.itemCount - 1)
                if (shouldScrollToBottom || lastMessage.senderId == auth.currentUser!!.uid) {
                    scrollToBottom()
                }
            }
        })

        chatMessagesRecyclerView.layoutManager = layoutManager
        chatMessagesRecyclerView.adapter = adapter
        chatMessagesRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                val lastPosition = adapter.itemCount - 1

                shouldScrollToBottom = lastVisiblePosition == lastPosition
            }
        })
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
        attachChatListener()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
        detachChatListener()
        detachUsersListenter()
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

    private fun detachUsersListenter() {
        if (usersListener != null) {
            database.child("users").removeEventListener(usersListener!!)
        }
    }

    private fun attachUsersListener() {
        detachUsersListenter()

        if (usersListener == null) {
            usersListener = object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)

                            if (user != null) {
                                if (chat != null && chat!!.isMember(user.id)) {
                                    chat!!.updateMember(user)
                                }

                                // TODO: Update user in each message (in adapter)
                            }
                        }

                        chatTitleTextView.text = chat!!.getMemberNames(auth.currentUser!!.uid)
                        // TODO: Set chat image
                    }
                }

                override fun onCancelled(e: DatabaseError) {
                    Log.e(TAG, "Failed to retrieve users: ${e.message}")
                }
            }
        }

        database.child("users").addValueEventListener(usersListener!!)
    }

    private fun detachChatListener() {
        if (chatListener != null) {
            database.child("chats").child(chatId!!).removeEventListener(chatListener!!)
        }
    }

    private fun attachChatListener() {
        detachChatListener()

        if (chatListener == null) {
            chatListener = object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val dbChat = snapshot.getValue(Chat::class.java)

                        if (dbChat != null) {
                            chat = dbChat

                            if (!chat!!.isMember(auth.currentUser!!.uid)) {
                                activity?.finish()
                                Toast.makeText(activity, "You've been removed from this chat", Toast.LENGTH_LONG).show()
                            }

                            attachUsersListener()
                        }
                    }
                }

                override fun onCancelled(e: DatabaseError) {
                    Log.e(TAG, "Failed to retrieve chat $chatId: ${e.message}")
                }
            }
        }

        database.child("chats").child(chatId!!).addValueEventListener(chatListener!!)
    }

    private fun openChatSettings() {
        activity?.hideKeyboard()
        val chatSettingsFragment = PrivateChatSettingsFragment.newInstance(false)
        (activity as BaseFragmentActivity?)?.addFragment(chatSettingsFragment as android.support.v4.app.Fragment)
    }

    private fun scrollToBottom() {
        layoutManager.scrollToPosition(adapter.itemCount - 1)
    }

    private fun sendImage() {
        (activity as BaseFragmentActivity).dispatchTakeOrPickPictureIntent { bitmap ->
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val data = byteArrayOutputStream.toByteArray()

            val imageUrl = "images/${UUID.randomUUID()}"
            storage.child(imageUrl).putBytes(data)
                    .addOnSuccessListener {
                        val messageId = database.child("chatMessages").child(chatId!!).push().key!!
                        val message = Message(
                                id = messageId,
                                imageUrl = imageUrl,
                                senderId = auth.currentUser!!.uid
                        )

                        val childUpdates = HashMap<String, Any>()
                        childUpdates["chatMessages/$chatId/$messageId"] = message.toMap()
                        childUpdates["chats/$chatId/lastMessage"] = "\uD83D\uDCF7"
                        childUpdates["chats/$chatId/updatedAt"] = message.timestamp
                        database.updateChildren(childUpdates)
                                .addOnFailureListener {
                                    activity?.runOnUiThread {
                                        Snackbar.make(view!!, R.string.error_picture_upload_failed, Snackbar.LENGTH_LONG)
                                                .show()
                                    }
                                }
                    }
                    .addOnFailureListener {
                        activity?.runOnUiThread {
                            Snackbar.make(view!!, R.string.error_picture_upload_failed, Snackbar.LENGTH_LONG)
                                    .show()
                        }
                    }
        }
    }



    private fun sendMessage(text: String) {
        val messageId = database.child("chatMessages").child(chatId!!).push().key!!
        val message = Message(
                id = messageId,
                text = text,
                senderId = auth.currentUser!!.uid
        )

        val childUpdates = HashMap<String, Any>()
        childUpdates["chatMessages/$chatId/$messageId"] = message.toMap()
        childUpdates["chats/$chatId/lastMessage"] = text
        childUpdates["chats/$chatId/updatedAt"] = message.timestamp
        database.updateChildren(childUpdates)
                .addOnSuccessListener {
                    chatEditText.setText("")
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to update new message $messageId: ${it.message}")
                }
    }

    private fun onMessageClicked(message: Message) {
        //TODO
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private val ARG_CHAT_ID = "chatId"
        private val ARG_IS_GROUP_CHAT = "isGroupChat"
        private val ARG_JOINED_AT = "joinedAt"

        fun newInstance(chatId: String, isGroupChat: Boolean, joinedAt: Long): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString(ARG_CHAT_ID, chatId)
            args.putBoolean(ARG_IS_GROUP_CHAT, isGroupChat)
            args.putLong(ARG_JOINED_AT, joinedAt)
            fragment.arguments = args
            return fragment
        }
    }
}
