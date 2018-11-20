package xyz.shmeleva.eight.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.stfalcon.multiimageview.MultiImageView
import kotlinx.android.synthetic.main.activity_chat.*
import  xyz.shmeleva.eight.R

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatMultiImageView.shape = MultiImageView.Shape.CIRCLE
    }

    fun navigateBack(view: View) {
        onBackPressed()
    }
}
