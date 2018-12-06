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

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.settings_username_dialog.view.*
import xyz.shmeleva.eight.R
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import xyz.shmeleva.eight.models.User
import java.io.ByteArrayOutputStream
import java.util.*

// TODO: handle DB failures?

class SettingsActivity : BaseFragmentActivity() {

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
        var uploadImageResolution = sharedPreferences.getString("uploadResolution", "Low")
        var downloadImageResolution = sharedPreferences.getString("downloadResolution", "Low")
        var theme = sharedPreferences.getString("theme", "Seaweed")


        val imageResolutionArray = arrayOf(
                getResources().getString(R.string.settings_image_resolution_low),
                getResources().getString(R.string.settings_image_resolution_medium),
                getResources().getString(R.string.settings_image_resolution_high)
        )
        val themeArray = arrayOf("Seaweed", "Dark")

        getUserFromDBAndPopulate(auth.currentUser!!.uid)
        setUploadResolutionLabel(uploadImageResolution)
        setDownloadResolutionLabel(downloadImageResolution)
        setThemeLabel(theme)

        usernameRelativeLayout.setOnClickListener() {
            val usernameDialog = LayoutInflater.from(this)
                    .inflate(R.layout.settings_username_dialog, null)

            val dialogEditText = usernameDialog.findViewById<EditText>(R.id.dialogUsername)
            dialogEditText.setText(usernameTextView.text)

            AlertDialog.Builder(this)
                    .setView(usernameDialog)
                    .setPositiveButton(R.string.settings_prompt_save_btn, DialogInterface.OnClickListener { dialog, whichButton ->
                        val changedUsername = usernameDialog.dialogUsername.text.toString()
                        uploadUsernameToDB(changedUsername)
                                .addOnSuccessListener {
                                    usernameTextView.setText(changedUsername)
                                    editor.putString("username", changedUsername)
                                    editor.apply()
                                }
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
        dispatchTakeOrPickPictureIntent { bitmap ->
            profilePhoto = bitmap
            runOnUiThread {
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
                populateProfilePhotoFromDB(currentUser.profilePicUrl)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting User failed, log a message
                Log.i(TAG, databaseError.message)
            }
        }

        database.child("users").child(uid)
                .addListenerForSingleValueEvent(userOneTimeListener)
    }

    private fun populateProfilePhotoFromDB(url: String) {
        if (url != null && url != "") {
            AsyncTask.execute(Runnable {
                val profilePhotoRef = storageRef.child(url)
                profilePhotoRef.getBytes(50*1000*1000).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    runOnUiThread(Runnable {
                        profilePictureImageView.setImageBitmap(bitmap)
                    })
                }
            })
        }
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
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        if (currentUser.profilePicUrl != "") {
            storageRef.child(currentUser.profilePicUrl).putBytes(data)
        } else {
            val url = "images/" + UUID.randomUUID().toString()
            storageRef.child(url).putBytes(data)

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
