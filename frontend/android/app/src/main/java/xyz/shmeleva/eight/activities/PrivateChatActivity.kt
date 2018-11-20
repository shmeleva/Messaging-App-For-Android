package xyz.shmeleva.eight.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import xyz.shmeleva.eight.R

class PrivateChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_chat)
    }

    fun navigateBack(view: View) {
        onBackPressed()
    }
}
