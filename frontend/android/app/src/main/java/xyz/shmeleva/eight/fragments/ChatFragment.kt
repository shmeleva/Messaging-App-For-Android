package xyz.shmeleva.eight.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.fragment_chat.*

import com.stfalcon.multiimageview.MultiImageView
import kotlinx.android.synthetic.main.activity_login.*

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.activities.BaseFragmentActivity
import xyz.shmeleva.eight.adapters.MessageListAdapter
import xyz.shmeleva.eight.models.Message
import xyz.shmeleva.eight.utilities.*
import java.util.*

class ChatFragment : Fragment() {

    private var chatId: String? = null

    private var fragmentInteractionListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            chatId = arguments!!.getString(ARG_CHAT_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatImageView.shape = MultiImageView.Shape.CIRCLE

        // Example of loading a picture:
        chatImageView.loadImages(arrayListOf("https://pixel.nymag.com/imgs/daily/vulture/2016/11/23/23-san-junipero.w330.h330.jpg"), 40 ,0)

        // Example of setting a name:
        chatTitleTextView.text = "Pavel Durov"

        chatBackButton.setOnClickListener { _ -> activity?.onBackPressed()}
        chatImageView.setOnClickListener {_ -> openChatSettings()}
        chatTitleTextView.setOnClickListener {_ -> openChatSettings()}
        chatActionImageButton.setOnClickListener {_ ->
            val message = chatEditText.text.toString()
            if (message.isBlank()) {
                sendImage()
            }
            else {
                sendMessage(message)
            }}

        chatEditText.addTextChangedListener((object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val message = chatEditText.text.toString()
                chatActionImageButton.setImageResource(if (message.isBlank()) R.drawable.ic_baseline_photo_camera_24px else R.drawable.ic_baseline_send_24px)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }))

        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        chatMessagesRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        val messages = ArrayList<Message>(listOf(
                Message("X", "1st message", "", uid, Date().time),
                Message("X", "2nd message", "", "01", Date().time),
                Message("X", "3rd message", "", "02", Date().time),
                Message("X", "4th message", "", uid, Date().time),
                Message("X", "5th message", "", "02", Date().time),
                Message("X", "6th message", "", "01", Date().time),
                Message("X", "7th message", "", "01", Date().time),
                Message("X", "8th message", "", "02", Date().time),
                Message("X", "9th message", "", uid, Date().time),
                Message("X", "10th message", "", "01", Date().time),
                Message("X", "11th message", "", "01", Date().time),
                Message("X", "12th message", "", uid, Date().time),
                Message("X", "13th message", "", "02", Date().time),
                Message("X", "14th message", "", "02", Date().time),
                Message("X", "15th message", "", uid, Date().time),
                Message("X", "16th message", "", "02", Date().time)))
        var adapter = MessageListAdapter(uid, true, messages, { chat : Message -> onMessageClicked(chat) })
        chatMessagesRecyclerView.adapter = adapter
        chatMessagesRecyclerView.scrollToPosition(messages.size - 1)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            fragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener") as Throwable
        }
    }

    override fun onDetach() {
        super.onDetach()
        fragmentInteractionListener = null
    }

    private fun openChatSettings() {
        val chatSettingsFragment = PrivateChatSettingsFragment.newInstance(false)
        (activity as BaseFragmentActivity?)?.addFragment(chatSettingsFragment as android.support.v4.app.Fragment)
    }

    private fun sendImage() {
        (activity as BaseFragmentActivity).dispatchTakeOrPickPictureIntent { _ ->  }
    }

    private fun sendMessage(text: String) {
        // TODO
    }

    private fun onMessageClicked(message: Message) {

    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private val ARG_CHAT_ID = "chatId"

        fun newInstance(chatId: String): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString(ARG_CHAT_ID, chatId)
            fragment.arguments = args
            return fragment
        }
    }
}
