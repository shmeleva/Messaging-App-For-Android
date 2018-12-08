package xyz.shmeleva.eight.activities

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import xyz.shmeleva.eight.R
import kotlinx.android.synthetic.main.activity_fullscreen_image.*
import xyz.shmeleva.eight.utilities.DoubleClickBlocker

class FullscreenImageActivity : AppCompatActivity() {
    private val doubleClickBlocker: DoubleClickBlocker = DoubleClickBlocker()
    private val hideHandler = Handler()
    private val showRunnable = Runnable {
        fullscreenImageControls.visibility = View.VISIBLE
    }
    private var areControlsVisible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    private val delayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        areControlsVisible = true
        
        val ref = FirebaseStorage.getInstance().reference.child(intent.getStringExtra("imageUrl"))
        Glide.with(this)
                .load(ref)
                .into(fullscreenImageView)


        fullscreenImageView.setOnClickListener { toggle() }
        fullscreenImageBackButton.setOnTouchListener(delayHideTouchListener)
        fullscreenImageDownloadButton.setOnTouchListener(delayHideTouchListener)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delayedHide(AUTO_HIDE_DELAY_MILLIS)
    }

    private fun toggle() {
        if (areControlsVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        fullscreenImageControls.visibility = View.GONE
        areControlsVisible = false
        hideHandler.removeCallbacks(showRunnable)
    }

    private fun show() {
        areControlsVisible = true
        hideHandler.postDelayed(showRunnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    fun onBack(@Suppress("UNUSED_PARAMETER")view: View) {
        if (doubleClickBlocker.isSingleClick()) {
            onBackPressed()
        }
    }

    fun onDownload(@Suppress("UNUSED_PARAMETER")view: View) {
        if (doubleClickBlocker.isSingleClick()) {

        }
    }

    companion object {
        private val AUTO_HIDE = true
        private val AUTO_HIDE_DELAY_MILLIS = 3000
        private val UI_ANIMATION_DELAY = 300
    }
}
