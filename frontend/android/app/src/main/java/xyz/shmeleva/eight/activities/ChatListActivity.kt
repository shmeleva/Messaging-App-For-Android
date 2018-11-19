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
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import xyz.shmeleva.eight.adapters.ChatListAdapter
import xyz.shmeleva.eight.models.Chat


class ChatListActivity : AppCompatActivity() {

    private var isFabOpen = false

    private var fabOpenAnimation: Animation? = null
    private var fabCloseAnimation: Animation? = null
    private var rotateForwardAnimation: Animation? = null
    private var rotateBackwardAnimation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        setSupportActionBar(toolbar)

        fabOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fabCloseAnimation = AnimationUtils.loadAnimation(applicationContext,R.anim.fab_close)
        rotateForwardAnimation = AnimationUtils.loadAnimation(applicationContext,R.anim.rotate_forward)
        rotateBackwardAnimation = AnimationUtils.loadAnimation(applicationContext,R.anim.rotate_backward)


        chatListStartChatFab.setOnClickListener { view ->
            if(isFabOpen){
                chatListStartChatFab.startAnimation(rotateBackwardAnimation);
                chatListStartGroupChatFab.startAnimation(fabCloseAnimation);
                chatListStartPrivateChatFab.startAnimation(fabCloseAnimation);
                //
                chatListStartGroupChatFab.isClickable = false;
                chatListStartPrivateChatFab.isClickable = false;
                //
                isFabOpen = false;
            } else {
                chatListStartChatFab.startAnimation(rotateForwardAnimation);
                chatListStartGroupChatFab.startAnimation(fabOpenAnimation);
                chatListStartPrivateChatFab.startAnimation(fabOpenAnimation);
                //
                chatListStartGroupChatFab.isClickable = true;
                chatListStartPrivateChatFab.isClickable = true;
                //
                isFabOpen = true;

            }
        }

        chatListStartGroupChatFab.setOnClickListener { view ->

        }

        chatListStartPrivateChatFab.setOnClickListener { view ->

        }

        chatListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val chats = ArrayList<Chat>(listOf(Chat(0), Chat(1), Chat(2), Chat(3), Chat(4), Chat(5), Chat(6), Chat(7), Chat(8), Chat(9), Chat(10)))
        var adapter = ChatListAdapter(chats)
        chatListRecyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_chat_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
