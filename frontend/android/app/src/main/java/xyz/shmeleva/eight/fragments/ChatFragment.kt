package xyz.shmeleva.eight.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import kotlinx.android.synthetic.main.fragment_chat.*

import com.stfalcon.multiimageview.MultiImageView

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.activities.BaseFragmentActivity
import xyz.shmeleva.eight.adapters.MessageListAdapter
import xyz.shmeleva.eight.models.Message
import xyz.shmeleva.eight.utilities.*

class ChatFragment : Fragment() {

    private var chatId: Int? = null

    private var fragmentInteractionListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            chatId = arguments!!.getInt(ARG_CHAT_ID)
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

        chatBackButton.setOnClickListener({ _ -> activity?.onBackPressed()})
        chatImageView.setOnClickListener({_ -> openChatSettings()})
        chatTitleTextView.setOnClickListener({_ -> openChatSettings()})
        chatActionImageButton.setOnClickListener({_ ->
            val message = chatEditText.text.toString()
            if (message.isBlank()) {
                sendImage()
            }
            else {
                sendMessage(message)
            }})

        chatMessagesRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        val messages = ArrayList<Message>(listOf(Message(), Message(), Message(), Message(), Message(), Message(), Message(), Message(), Message(), Message(), Message(), Message(), Message(), Message(), Message(), Message()))
        var adapter = MessageListAdapter(messages, { chat : Message -> onMessageClicked(chat) })
        chatMessagesRecyclerView.adapter = adapter
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

    private fun openChatSettings() {
        val chatSettingsFragment = PrivateChatSettingsFragment.newInstance(false)
        (activity as BaseFragmentActivity?)?.addFragment(chatSettingsFragment as android.support.v4.app.Fragment)
    }

    private fun sendImage() {
        // TODO
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

        fun newInstance(chatId: Int): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putInt(ARG_CHAT_ID, chatId)
            fragment.arguments = args
            return fragment
        }
    }
}
