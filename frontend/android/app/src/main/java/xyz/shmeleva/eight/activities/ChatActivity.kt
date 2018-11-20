package xyz.shmeleva.eight.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.stfalcon.multiimageview.MultiImageView
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_login.*
import  xyz.shmeleva.eight.R

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatMultiImageView.shape = MultiImageView.Shape.CIRCLE

        chatEditText.addTextChangedListener((object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()) {
                    chatActionImageButton.setImageResource(R.drawable.ic_baseline_photo_camera_24px)
                }
                else {
                    chatActionImageButton.setImageResource(R.drawable.ic_baseline_send_24px)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }))
    }

    fun navigateBack(view: View) {
        onBackPressed()
    }

    fun openChatSettings(view: View) {

    }

    fun onActionButtonClicked(view: View) {
        val message = chatEditText.text
        if (message.isNullOrBlank()) {
            // Open image selection...
        }
        else {
            // Send message...
        }
    }
}
