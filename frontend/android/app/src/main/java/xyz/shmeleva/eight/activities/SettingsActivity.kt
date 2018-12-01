package xyz.shmeleva.eight.activities


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View

import android.view.WindowManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.settings_username_dialog.*
import kotlinx.android.synthetic.main.settings_username_dialog.view.*
import xyz.shmeleva.eight.R
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_registration.*
import xyz.shmeleva.eight.models.User
import java.io.File


class SettingsActivity : AppCompatActivity() {

    private val TAG: String = "SettingsActivity"
    @JvmField val PICK_PHOTO = 1
    private lateinit var auth: FirebaseAuth
    private lateinit var profilePhoto: Bitmap
    private lateinit var storageRef: StorageReference
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        database = FirebaseDatabase.getInstance().reference

        val sharedPreferences = getSharedPreferences("userSettings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // local user settings variables
        var uploadImageResolution = sharedPreferences.getString("uploadResolution", "Low")
        var downloadImageResolution = sharedPreferences.getString("downloadResolution", "Low")
        var theme = sharedPreferences.getString("theme", "Seaweed")


        val imageResolutionArray = arrayOf(
                getResources().getString(R.string.settings_image_resolution_low),
                getResources().getString(R.string.settings_image_resolution_medium),
                getResources().getString(R.string.settings_image_resolution_high)
        )
        val themeArray = arrayOf("Seaweed", "Dark")

        populateProfilePhotoFromDB(auth.currentUser!!.uid)
        populateUsernameFromDB(auth.currentUser!!.uid)
        setUploadResolutionLabel(uploadImageResolution)
        setDownloadResolutionLabel(downloadImageResolution)
        setThemeLabel(theme)

        usernameRelativeLayout.setOnClickListener() {

            // TODO: make sure the username gets uploaded to the server properly!
            val usernameDialog = LayoutInflater.from(this)
                    .inflate(R.layout.settings_username_dialog, null)

            val dialogEditText = usernameDialog.findViewById<EditText>(R.id.dialogUsername)
            dialogEditText.setText(usernameTextView.text)

            AlertDialog.Builder(this)
                    .setView(usernameDialog)
                    .setPositiveButton(R.string.settings_prompt_save_btn, DialogInterface.OnClickListener { dialog, whichButton ->
                        val changedUsername = usernameDialog.dialogUsername.text.toString()
                        usernameTextView.setText(changedUsername)
                        editor.putString("username", changedUsername)
                        editor.apply()
                        dialog.dismiss()
                    })
                    .setNegativeButton(R.string.settings_prompt_cancel_btn, DialogInterface.OnClickListener{ dialog, whichButton ->
                            dialog.cancel()
                    })
                    .show()

        }

        uploadResolutionRelativeLayout.setOnClickListener(){
            val currentImageResolutionIndex= imageResolutionArray.indexOf(uploadImageResolution)
            AlertDialog.Builder(this)
                    .setSingleChoiceItems(imageResolutionArray, currentImageResolutionIndex, null)
                    .setPositiveButton(R.string.settings_prompt_save_btn, DialogInterface.OnClickListener { dialog, whichButton ->
                        val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                        uploadImageResolution = imageResolutionArray[selectedPosition]
                        uploadResolutionTextView.setText(uploadImageResolution)
                        editor.putString("uploadResolution", uploadImageResolution)
                        editor.apply()
                        dialog.dismiss()
                    })
                    .setNegativeButton(R.string.settings_prompt_cancel_btn, DialogInterface.OnClickListener { dialog, whichButton ->
                        dialog.cancel()
                    })
                    .show()

        }

        downloadResolutionRelativeLayout.setOnClickListener() {
            val currentImageResolutionIndex= imageResolutionArray.indexOf(downloadImageResolution)
            AlertDialog.Builder(this)
                    .setSingleChoiceItems(imageResolutionArray, currentImageResolutionIndex, null)
                    .setPositiveButton(R.string.settings_prompt_save_btn, DialogInterface.OnClickListener { dialog, whichButton ->
                        val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                        downloadImageResolution = imageResolutionArray[selectedPosition]
                        downloadResolutionTextView.setText(downloadImageResolution)
                        editor.putString("downloadResolution", downloadImageResolution)
                        editor.apply()
                        dialog.dismiss()
                    })
                    .setNegativeButton(R.string.settings_prompt_cancel_btn, DialogInterface.OnClickListener { dialog, whichButton ->
                        dialog.cancel()
                    })
                    .show()
        }

        themeRelativeLayout.setOnClickListener() {
            val currentTheme = themeArray.indexOf(theme)
            AlertDialog.Builder(this)
                    .setSingleChoiceItems(themeArray, currentTheme, null)
                    .setPositiveButton(R.string.settings_prompt_save_btn, DialogInterface.OnClickListener { dialog, whichButton ->
                        val selectedTheme = (dialog as AlertDialog).listView.checkedItemPosition
                        theme = themeArray[selectedTheme]
                        themeTextView.setText(theme)
                        editor.putString("theme", theme)
                        editor.apply()
                        dialog.dismiss()
                    })
                    .setNegativeButton(R.string.settings_prompt_cancel_btn, DialogInterface.OnClickListener { dialog, whichButton ->
                        dialog.cancel()
                    }).show()
        }
    }

    fun setUsernameLabel(username: String) {
        usernameTextView.setText(username)
    }

    fun setUploadResolutionLabel(resolution: String) {
        uploadResolutionTextView.setText(resolution)
    }

    fun setDownloadResolutionLabel(resolution: String) {
        downloadResolutionTextView.setText(resolution)
    }

    fun setThemeLabel(theme: String) {
        themeTextView.setText(theme)
    }

    override fun onStart() {
        super.onStart()
    }

    fun navigateBack(view: View) {
        onBackPressed()
    }

    fun logOut(view: View) {
        auth.signOut()

        // delete the profile picture from internal storage
        deleteFile("profile_photo")

        val loginActivityIntent = Intent(this, LoginActivity::class.java)
                .putExtra(getString(R.string.extra_message_key), getString(R.string.message_logout))
        loginActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        loginActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(loginActivityIntent)
        finish()
    }

    fun pickPhoto(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select profile picture"), PICK_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // TODO: make sure the photo gets uploaded to the server properly!

        if (requestCode == PICK_PHOTO && resultCode == Activity.RESULT_OK) {
            AsyncTask.execute(Runnable {
                profilePhoto = getProfilePhotoFromIntent(data)
                runOnUiThread({
                    profilePictureImageView.setImageBitmap(profilePhoto)
                })

                // save the photo to internal storage
                openFileOutput("profile_photo", Context.MODE_PRIVATE).use {
                    profilePhoto.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            })
        }
    }

    private fun getProfilePhotoFromIntent(intentData: Intent?) : Bitmap {
        var bitmap : Bitmap
        try {
            bitmap = Picasso.get()
                    .load(intentData?.data)
                    .resize(400, 0)
                    .get()
        } catch (ex: Exception) {
            // TODO: catch the exception better!
            // default profile picture
            return Picasso.get()
                    .load("https://pixel.nymag.com/imgs/daily/vulture/2016/11/23/23-san-junipero.w330.h330.jpg")
                    .resize(400, 0)
                    .get()
        }

        return bitmap
    }

    private fun populateUsernameFromDB(uid: String) {
        var userOneTimeListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get User object and use the values to update the UI
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    setUsernameLabel(user!!.username)
                } else {
                    setUsernameLabel("")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting User failed, log a message
                setUsernameLabel("")
                Log.i(TAG, databaseError.message)
            }
        }

        database.child("users").child(uid)
                .addListenerForSingleValueEvent(userOneTimeListener)
    }

    private fun populateProfilePhotoFromDB(uid: String) {
        var userOneTimeListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get User object and use the values to update the UI
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    AsyncTask.execute(Runnable {
                        val profilePhotoRef = storageRef.child(user.profilePicUrl)
                        profilePhotoRef.getBytes(50*1000*1000).addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                            runOnUiThread(Runnable {
                                profilePictureImageView.setImageBitmap(bitmap)
                            })
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting User failed, log a message
                Log.i(TAG, databaseError.message)
            }
        }

        database.child("users").child(uid)
                .addListenerForSingleValueEvent(userOneTimeListener)
    }
}
