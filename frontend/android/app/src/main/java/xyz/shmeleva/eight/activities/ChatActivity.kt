package xyz.shmeleva.eight.activities

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.stfalcon.multiimageview.MultiImageView
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_login.*
import  xyz.shmeleva.eight.R
import xyz.shmeleva.eight.fragments.ChatFragment
import xyz.shmeleva.eight.fragments.GroupChatSettingsFragment
import xyz.shmeleva.eight.fragments.PrivateChatSettingsFragment

class ChatActivity : AppCompatActivity(),
        ChatFragment.OnFragmentInteractionListener,
        PrivateChatSettingsFragment.OnFragmentInteractionListener,
        GroupChatSettingsFragment.OnFragmentInteractionListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.chatFragmentContainer, ChatFragment() as android.support.v4.app.Fragment)
                ?.commit()
    }

    override fun onBackPressed() {
        if (!supportFragmentManager.popBackStackImmediate()) {
            finishAfterTransition() // Lollipop +
        }
    }

    override fun onFragmentInteraction(uri: Uri) {

    }
}
