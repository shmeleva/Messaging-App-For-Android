package xyz.shmeleva.eight.activities

import android.content.Intent
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
        var joinedAt = intent?.extras?.getLong("joinedAt") ?: 0
        replaceFragment(ChatFragment.newInstance(chatId, isGroupChat, joinedAt) as android.support.v4.app.Fragment)
}

    override fun onFragmentInteraction(uri: Uri) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.fragments.forEach {
            if (it is GroupChatSettingsFragment)
                it.onActivityResult(requestCode, resultCode, data)
        }
    }
}
