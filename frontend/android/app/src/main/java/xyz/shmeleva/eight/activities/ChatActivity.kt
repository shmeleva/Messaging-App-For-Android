package xyz.shmeleva.eight.activities

import android.net.Uri
import android.os.Bundle

import  xyz.shmeleva.eight.R
import xyz.shmeleva.eight.fragments.*

class ChatActivity : BaseFragmentActivity(R.id.chatFragmentContainer),
        ChatFragment.OnFragmentInteractionListener,
        PrivateChatSettingsFragment.OnFragmentInteractionListener,
        GroupChatSettingsFragment.OnFragmentInteractionListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        var chatId = intent?.extras?.getString("chatId") ?: ""
        var isGroupChat = intent?.extras?.getBoolean("isGroupChat") ?: false
        replaceFragment(ChatFragment.newInstance(chatId, isGroupChat) as android.support.v4.app.Fragment)
}

    override fun onFragmentInteraction(uri: Uri) {

    }
}
