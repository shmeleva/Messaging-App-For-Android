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
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import kotlinx.android.synthetic.main.fragment_chat.*

import com.stfalcon.multiimageview.MultiImageView
import jp.wasabeef.glide.transformations.MaskTransformation
import kotlinx.android.synthetic.main.item_incoming_text_message.*

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

    private lateinit var chatSettingsFragment: android.support.v4.app.Fragment

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

        if (isGroupChat) {
            chatSettingsFragment = GroupChatSettingsFragment.newInstance(chatId!!, auth.currentUser!!.uid, joinedAt)
        } else {
            chatSettingsFragment = PrivateChatSettingsFragment.newInstance(false, chatId, auth.currentUser!!.uid, joinedAt)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatImageView.shape = MultiImageView.Shape.CIRCLE

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

                if (isGroupChat) {
                    for (position in positionStart until positionStart + itemCount) {
                        val message = adapter.getItem(position)

                        if (message.senderId != auth.currentUser!!.uid) {
                            database.child("users").child(message.senderId).addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    Log.i(TAG, "users/${message.senderId} onDataChange!")

                                    if (userSnapshot.exists()) {
                                        val user = userSnapshot.getValue(User::class.java)

                                        if (user != null) {
                                            message.sender = user
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                }

                                override fun onCancelled(e: DatabaseError) {
                                    Log.e(TAG, "Failed to retrieve user ${message.senderId}: ${e.message}")
                                }
                            })
                        }
                    }
                }

                val lastMessage = adapter.getItem(adapter.itemCount - 1)
                if (shouldScrollToBottom || lastMessage.senderId == auth.currentUser!!.uid) {
                    scrollToBottom()
                }
            }
        })

        chatMessagesRecyclerView.layoutManager = layoutManager
        chatMessagesRecyclerView.adapter = adapter
        chatMessagesRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
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
            Log.i(TAG, "Detached usersListener")
        }
    }

    private fun attachUsersListener() {
        detachUsersListenter()

        if (usersListener == null) {
            usersListener = object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(TAG, "users onDataChange!")
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)

                            if (user != null) {
                                if (chat != null && chat!!.isMember(user.id)) {
                                    chat!!.updateMember(user)
                                }

                                if (isGroupChat) {
                                    for (i in 0 until adapter.itemCount) {
                                        val message = adapter.getItem(i)
                                        if (message.senderId == user.id) {
                                            message.sender = user
                                        }
                                    }
                                }
                            }
                        }
                        adapter.notifyDataSetChanged()

                        chatTitleTextView.text = chat!!.getMemberNames(auth.currentUser!!.uid)
                        if (isGroupChat) {
                            val numberOfMembers = chat!!.members.size.toString()
                            val generator = ColorGenerator.MATERIAL
                            val colour = generator.getColor(numberOfMembers)
                            val drawable = TextDrawable.builder().buildRoundRect(numberOfMembers, colour, 48)
                            chatImageView.setImageDrawable(drawable)
                        }
                        else {
                            val secondUser = chat!!.users.firstOrNull{ it.id != auth.currentUser!!.uid }
                            val profilePictureUrl = secondUser?.profilePicUrl
                            if (profilePictureUrl != null && profilePictureUrl.isNotEmpty()) {
                                val ref = FirebaseStorage.getInstance().reference.child(profilePictureUrl)
                                val requestOptions = RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                                Glide.with(chatImageView.context)
                                        .load(ref)
                                        .apply(bitmapTransform(MultiTransformation<Bitmap>(CenterCrop(),
                                                MaskTransformation(R.drawable.shape_circle_small))))
                                        .apply(requestOptions)
                                        .into(chatImageView)
                            }
                            else {
                                val generator = ColorGenerator.MATERIAL
                                val username = secondUser?.username ?: "?"
                                val colour = generator.getColor(username)

                                val drawable = TextDrawable.builder().buildRoundRect(username.substring(0, 1).toUpperCase(), colour, 48)
                                chatImageView.setImageDrawable(drawable)
                            }
                        }
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

    private fun detachChatListener() {
        if (chatListener != null) {
            database.child("chats").child(chatId!!).removeEventListener(chatListener!!)
            Log.i(TAG, "Detached chatListener")
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
        Log.i(TAG, "Attached chatListener")
    }

    private fun openChatSettings() {
        activity?.hideKeyboard()



        (activity as BaseFragmentActivity?)?.addFragment(chatSettingsFragment)
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
                        message.imageTimestamp = message.timestamp

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
