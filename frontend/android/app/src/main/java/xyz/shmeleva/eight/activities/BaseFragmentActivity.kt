package xyz.shmeleva.eight.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask.execute
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import xyz.shmeleva.eight.R
import com.bumptech.glide.Glide
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


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
    val REQUEST_PICTURE_SELECT = 2

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
        currentPicturePath = ""
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    runOnUiThread {
                        Snackbar.make(findViewById(containerViewId), R.string.error_picture_upload_failed, Snackbar.LENGTH_LONG)
                                .show()
                    }
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.example.android.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_PICTURE_CAPTURE)
                }
            }
        }
    }

    private fun  dispatchPickPictureIntent() {
        val pickPictureIntent = Intent()
        pickPictureIntent.type = "image/*"
        pickPictureIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(pickPictureIntent, "Gallery"), REQUEST_PICTURE_SELECT)
    }

    var currentPicturePath: String = ""

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPicturePath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if ((requestCode == REQUEST_PICTURE_CAPTURE || requestCode == REQUEST_PICTURE_SELECT) && resultCode == RESULT_OK) {
            try {
                execute {
                    val bitmap = if (requestCode == REQUEST_PICTURE_CAPTURE)
                        //data.extras.get("data") as Bitmap
                        Glide.with(this).asBitmap().load(currentPicturePath).submit().get()
                    else
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
