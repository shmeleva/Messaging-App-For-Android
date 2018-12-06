package xyz.shmeleva.eight.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask.execute
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import xyz.shmeleva.eight.R
import com.bumptech.glide.Glide


open class BaseFragmentActivity(private val containerViewId: Int =  0) : AppCompatActivity() {

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
                ?.beginTransaction()
                ?.replace(containerViewId, fragment)
                ?.commit()
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager
                ?.beginTransaction()
                ?.add(containerViewId, fragment)
                ?.addToBackStack(null)
                ?.commit()
    }

    override fun onBackPressed() {
        if (!supportFragmentManager.popBackStackImmediate()) {
            finishAfterTransition()
        }
    }



    val REQUEST_PICTURE_CAPTURE = 1
    val PICK_PICTURE = 2

    private var pictureCallback: (Bitmap) -> Unit = { }

    fun dispatchTakeOrPickPictureIntent(callback: (Bitmap) -> Unit) {
        AlertDialog.Builder(this)
                .setItems(R.array.dialog_picture_source) { _, which ->
                    pictureCallback = callback
                    if (which == 0) dispatchTakePictureIntent() else dispatchPickPictureIntent()
                }
                .show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_PICTURE_CAPTURE)
            }
        }
    }

    private fun  dispatchPickPictureIntent() {
        val pickPictureIntent = Intent()
        pickPictureIntent.type = "image/*"
        pickPictureIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(pickPictureIntent, "Gallery"), PICK_PICTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if ((requestCode == REQUEST_PICTURE_CAPTURE || requestCode == PICK_PICTURE) && resultCode == RESULT_OK) {
            try {
                execute {
                    val bitmap = if (requestCode == REQUEST_PICTURE_CAPTURE)
                        data.extras.get("data") as Bitmap
                    else
                        //Picasso.get().load(data.data).get()
                        Glide.with(this).asBitmap().load(data.data).submit().get()

                    pictureCallback(bitmap)
                }
            }
            catch (ex: Exception) {
                Snackbar.make(findViewById(containerViewId), R.string.error_picture_upload_failed, Snackbar.LENGTH_LONG)
                        .show()
                Log.e("chat", ex.message)
            }
        }
    }
}
