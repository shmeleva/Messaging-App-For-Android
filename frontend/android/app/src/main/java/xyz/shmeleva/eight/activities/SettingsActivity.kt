package xyz.shmeleva.eight.activities


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View

import android.widget.EditText

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.settings_username_dialog.view.*
import xyz.shmeleva.eight.R
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_private_chat_settings.*
import xyz.shmeleva.eight.models.User
import xyz.shmeleva.eight.utilities.DoubleClickBlocker
import xyz.shmeleva.eight.utilities.loadFullProfilePictureFromFirebase
import java.io.ByteArrayOutputStream
import java.util.*

// TODO: handle DB failures?

class SettingsActivity : BaseFragmentActivity() {

    private val doubleClickBlocker: DoubleClickBlocker = DoubleClickBlocker()

    private val TAG: String = "SettingsActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var profilePhoto: Bitmap
    private lateinit var storageRef: StorageReference
    private lateinit var database: DatabaseReference

    private var currentUser = User()

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
        val defaultResolution = resources.getString(R.string.settings_image_resolution_default)
        var uploadImageResolution = sharedPreferences.getString("uploadResolution", defaultResolution)
        var downloadImageResolution = sharedPreferences.getString("downloadResolution", defaultResolution)
        var theme = sharedPreferences.getString("theme", "Seaweed")


        val imageResolutionArray = arrayOf(
                resources.getString(R.string.settings_image_resolution_low),
                resources.getString(R.string.settings_image_resolution_high),
                resources.getString(R.string.settings_image_resolution_full)
        )
        val themeArray = arrayOf("Seaweed", "Dark")

        getUserFromDBAndPopulate(auth.currentUser!!.uid)
        setUploadResolutionLabel(uploadImageResolution)
        setDownloadResolutionLabel(downloadImageResolution)
        setThemeLabel(theme)

        usernameRelativeLayout.setOnClickListener() {

            if (doubleClickBlocker.isSingleClick()) {
                val usernameDialog = LayoutInflater.from(this)
                        .inflate(R.layout.settings_username_dialog, null)

                val dialogEditText = usernameDialog.findViewById<EditText>(R.id.dialogUsername)
                dialogEditText.setText(usernameTextView.text)

                AlertDialog.Builder(this)
                        .setView(usernameDialog)
                        .setPositiveButton(R.string.settings_prompt_save_btn, DialogInterface.OnClickListener { dialog, _ ->
                            val changedUsername = usernameDialog.dialogUsername.text.toString()
                            uploadUsernameToDB(changedUsername)
                                    .addOnSuccessListener {
                                        usernameTextView.setText(changedUsername)
                                        editor.putString("username", changedUsername)
                                        editor.apply()
                                    }
                            dialog.dismiss()
                        })
                        .setNegativeButton(R.string.settings_prompt_cancel_btn, DialogInterface.OnClickListener{ dialog, _ ->
                            dialog.cancel()
                        })
                        .show()
            }
        }

        uploadResolutionRelativeLayout.setOnClickListener {
            if (doubleClickBlocker.isSingleClick()) {
                val currentImageResolutionIndex= imageResolutionArray.indexOf(uploadImageResolution)
                AlertDialog.Builder(this)
                        .setSingleChoiceItems(imageResolutionArray, currentImageResolutionIndex, null)
                        .setPositiveButton(R.string.settings_prompt_save_btn, { dialog, _ ->
                            val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                            uploadImageResolution = imageResolutionArray[selectedPosition]
                            uploadResolutionTextView.setText(uploadImageResolution)
                            editor.putString("uploadResolution", uploadImageResolution)
                            editor.apply()
                            dialog.dismiss()
                        })
                        .setNegativeButton(R.string.settings_prompt_cancel_btn, { dialog, _ ->
                            dialog.cancel()
                        })
                        .show()
            }
        }

        downloadResolutionRelativeLayout.setOnClickListener {
            if (doubleClickBlocker.isSingleClick()) {
                val currentImageResolutionIndex= imageResolutionArray.indexOf(downloadImageResolution)
                AlertDialog.Builder(this)
                        .setSingleChoiceItems(imageResolutionArray, currentImageResolutionIndex, null)
                        .setPositiveButton(R.string.settings_prompt_save_btn) { dialog, _ ->
                            val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                            downloadImageResolution = imageResolutionArray[selectedPosition]
                            downloadResolutionTextView.setText(downloadImageResolution)
                            editor.putString("downloadResolution", downloadImageResolution)
                            editor.apply()
                            dialog.dismiss()
                        }
                        .setNegativeButton(R.string.settings_prompt_cancel_btn) { dialog, _ ->
                            dialog.cancel()
                        }
                        .show()
            }
        }

        themeRelativeLayout.setOnClickListener {
            if (doubleClickBlocker.isSingleClick()) {
                val currentTheme = themeArray.indexOf(theme)
                AlertDialog.Builder(this)
                        .setSingleChoiceItems(themeArray, currentTheme, null)
                        .setPositiveButton(R.string.settings_prompt_save_btn) { dialog, _ ->
                            val selectedTheme = (dialog as AlertDialog).listView.checkedItemPosition
                            theme = themeArray[selectedTheme]
                            themeTextView.setText(theme)
                            editor.putString("theme", theme)
                            editor.apply()
                            dialog.dismiss()
                        }
                        .setNegativeButton(R.string.settings_prompt_cancel_btn) { dialog, _ ->
                            dialog.cancel()
                        }
                        .show()
            }
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

    fun navigateBack(@Suppress("UNUSED_PARAMETER")view: View) {

        if (doubleClickBlocker.isDoubleClick()) {
            return
        }

        onBackPressed()
    }

    fun logOut(@Suppress("UNUSED_PARAMETER")view: View) {

        if (doubleClickBlocker.isDoubleClick()) {
            return
        }

        database.child("tokens").child(auth.currentUser!!.uid).removeValue()

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

    fun pickPhoto(@Suppress("UNUSED_PARAMETER")view: View) {

        if (doubleClickBlocker.isDoubleClick()) {
            return
        }

        dispatchTakeOrPickPictureIntent { bitmap ->
            Log.i("imageUpload", "Callback received the bitmap.")
            profilePhoto = bitmap
            runOnUiThread {
                Log.i("imageUpload", "Setting image on UI thread...")
                profilePictureImageView.setImageBitmap(profilePhoto)
            }
            uploadProfilePhotoToDB(profilePhoto)
        }
    }

    private fun getUserFromDBAndPopulate(uid: String) {
        var userOneTimeListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value == null) {
                    Log.i(TAG, "User not found.")
                    logOut(logOutTextView)
                    return
                }

                // Get User object and use the values to update the UI
                currentUser = dataSnapshot.getValue(User::class.java)!!

                setUsernameLabel(currentUser.username)
                populateProfilePhotoFromDB(currentUser)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting User failed, log a message
                Log.i(TAG, databaseError.message)
            }
        }

        database.child("users").child(uid)
                .addListenerForSingleValueEvent(userOneTimeListener)
    }

    private fun populateProfilePhotoFromDB(user: User) {
        profilePictureImageView.loadFullProfilePictureFromFirebase(user)
    }

    private fun uploadUsernameToDB(newUsername: String) : Task<Void> {
        val uid = currentUser.id
        val oldUsername = currentUser.username
        currentUser.username = newUsername
        currentUser.lowercaseUsername = newUsername.toLowerCase()

        return updateUserInDB()
                .addOnSuccessListener {
                    database.child("usernames").child(oldUsername).removeValue()
                    database.child("usernames").child(newUsername).setValue(uid)
                }
                .addOnFailureListener {
                    currentUser.username = oldUsername
                    currentUser.lowercaseUsername = oldUsername.toLowerCase()
                    showErrorResult("Duplicate username!")
                }
    }

    private fun uploadProfilePhotoToDB(bitmap: Bitmap) {
        Log.i("imageUpload", "Compressing image...")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        Log.i("imageUpload", "Image compressed! Sending image...")

        if (currentUser.profilePicUrl != "") {
            Log.i("imageUpload", "Updating ${currentUser.profilePicUrl}")
            storageRef.child(currentUser.profilePicUrl).putBytes(data)
                    .addOnSuccessListener {
                        Log.i("imageUpload", "Uploaded successfully!")
                    }
                    .addOnFailureListener {
                        Log.i("imageUpload", "Upload failed!")
                    }
                    .addOnCanceledListener {
                        Log.i("imageUpload", "Upload canceled!")
                    }
        } else {
            val url = "images/" + UUID.randomUUID().toString()
            Log.i("imageUpload", "Creating ${url}")
            storageRef.child(url).putBytes(data)
                    .addOnSuccessListener {
                        Log.i("imageUpload", "Uploaded successfully!")
                    }
                    .addOnFailureListener {
                        Log.i("imageUpload", "Upload failed!")
                    }
                    .addOnCanceledListener {
                        Log.i("imageUpload", "Upload canceled!")
                    }

            currentUser.profilePicUrl = url
            updateUserInDB()
        }
    }

    private fun updateUserInDB() : Task<Void> {
        val uid = currentUser.id
        val currentUserValues = currentUser.toMap()
        val userUpdate = HashMap<String, Any>()
        userUpdate["users/$uid"] = currentUserValues
        return database.updateChildren(userUpdate)
    }

    private fun showErrorResult(message: String?) {
        val toast = Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
        )
        toast.show()
    }
}
