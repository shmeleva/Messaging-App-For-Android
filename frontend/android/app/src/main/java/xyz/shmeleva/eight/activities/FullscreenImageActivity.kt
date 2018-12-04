package xyz.shmeleva.eight.activities

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.squareup.picasso.Picasso
import xyz.shmeleva.eight.R
import kotlinx.android.synthetic.main.activity_fullscreen_image.*

class FullscreenImageActivity : AppCompatActivity() {
    private val mHideHandler = Handler()
    private val mShowPart2Runnable = Runnable {
        fullscreenImageControls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        mVisible = true

        val builder = Picasso.Builder(this)
        builder.listener(object : Picasso.Listener {
            override fun onImageLoadFailed(picasso: Picasso, uri: Uri, exception: Exception) {
                exception.printStackTrace()
            }
        })
        val imageUrl = intent.getStringExtra("imageUrl")
        builder.build().load(imageUrl).into(fullscreenImageView)

        fullscreenImageView.setOnClickListener { toggle() }
        fullscreenImageBackButton.setOnTouchListener(mDelayHideTouchListener)
        fullscreenImageDownloadButton.setOnTouchListener(mDelayHideTouchListener)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delayedHide(AUTO_HIDE_DELAY_MILLIS)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        fullscreenImageControls.visibility = View.GONE
        mVisible = false
        mHideHandler.removeCallbacks(mShowPart2Runnable)
    }

    private fun show() {
        mVisible = true
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        private val AUTO_HIDE = true
        private val AUTO_HIDE_DELAY_MILLIS = 3000
        private val UI_ANIMATION_DELAY = 300
    }
}
