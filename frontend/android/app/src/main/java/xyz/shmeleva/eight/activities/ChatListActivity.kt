package xyz.shmeleva.eight.activities

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import xyz.shmeleva.eight.R

import kotlinx.android.synthetic.main.activity_chat_list.*
import android.view.animation.Animation
import  android.view.animation.AnimationUtils
import android.view.MenuInflater
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import xyz.shmeleva.eight.adapters.ChatListAdapter
import xyz.shmeleva.eight.fragments.ChatListFragment
import xyz.shmeleva.eight.models.Chat

class ChatListActivity : AppCompatActivity(), ChatListFragment.OnFragmentInteractionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
    }

    override fun onFragmentInteraction(uri: Uri) {

    }
}
