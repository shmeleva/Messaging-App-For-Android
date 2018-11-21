package xyz.shmeleva.eight.activities

import android.os.Bundle
import android.net.Uri

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.fragments.*

class ChatListActivity : BaseFragmentActivity(R.id.chatListFragmentContainer),
        ChatListFragment.OnFragmentInteractionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        replaceFragment(ChatListFragment() as android.support.v4.app.Fragment)
    }

    override fun onFragmentInteraction(uri: Uri) {

    }
}
