package xyz.shmeleva.eight.activities

import android.net.Uri
import android.os.AsyncTask.execute
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import xyz.shmeleva.eight.R
import kotlinx.android.synthetic.main.activity_fullscreen_image.*
import xyz.shmeleva.eight.utilities.DoubleClickBlocker
import android.graphics.Bitmap
import java.io.FileOutputStream
import android.content.Intent
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v4.provider.DocumentFile
import android.util.Log
import java.util.*
import xyz.shmeleva.eight.utilities.getResizedPictureUrl
import xyz.shmeleva.eight.utilities.loadFromFirebase
import java.io.File
import java.text.SimpleDateFormat


class FullscreenImageActivity : AppCompatActivity() {

    private val REQUEST_CODE_OPEN_DIRECTORY: Int = 1

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

        fullscreenImageView.loadFromFirebase(intent.getStringExtra("imageUrl"))

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
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.putExtra("android.content.extra.SHOW_ADVANCED",true);
            startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY)
        }
    }

    fun onShare(@Suppress("UNUSED_PARAMETER")view: View) {
        if (doubleClickBlocker.isSingleClick()) {
            execute {
                try {
                    val ref = FirebaseStorage.getInstance().reference.child(intent.getStringExtra("imageUrl"))
                    val bitmap = Glide.with(this).asBitmap().load(ref).submit().get()
                    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val file = File.createTempFile(
                            "PNG_${timeStamp}_", /* prefix */
                            ".png", /* suffix */
                            storageDir /* directory */
                    )
                    val uri = FileProvider.getUriForFile(
                            this,
                            "com.example.android.fileprovider",
                            file
                    )
                    val out = contentResolver.openOutputStream(uri)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    val shareIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "image/png"
                    }
                    runOnUiThread {
                        startActivity(Intent.createChooser(shareIntent, "Share"))
                    }
                }
                catch (ex: Exception) {
                    Log.e("shareImage", ex.message)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == RESULT_OK && data != null && data.data != null) {
            execute {
                try {
                    val ref = FirebaseStorage.getInstance().reference.child(intent.getStringExtra("imageUrl"))
                    val bitmap = Glide.with(this).asBitmap().load(ref).submit().get()
                    val dir = DocumentFile.fromTreeUri(this, data.data)
                    val file = dir.createFile("image/png", UUID.randomUUID().toString() + ".png")
                    Log.i("imageSave", file.uri.toString())
                    val out = contentResolver.openOutputStream(file.uri)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                catch (ex: Exception) {
                    runOnUiThread {
                        Snackbar.make(fullscreenImageView, R.string.error_picture_upload_failed, Snackbar.LENGTH_LONG)
                                .show()
                    }
                    Log.e("fullscreenImageActivity", ex.message)
                }
            }
        }
    }

    companion object {
        private val AUTO_HIDE = true
        private val AUTO_HIDE_DELAY_MILLIS = 3000
        private val UI_ANIMATION_DELAY = 300
    }
}
