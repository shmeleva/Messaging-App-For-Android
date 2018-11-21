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

class ChatActivity : AppCompatActivity(), ChatFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
    }

    override fun onFragmentInteraction(uri: Uri) {

    }

    fun navigateBack(view: View) {

    }
}
