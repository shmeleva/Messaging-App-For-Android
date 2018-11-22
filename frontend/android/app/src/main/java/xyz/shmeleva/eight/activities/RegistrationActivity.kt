package xyz.shmeleva.eight.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_settings.*
import xyz.shmeleva.eight.R
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class RegistrationActivity : AppCompatActivity() {
    @JvmField val PICK_PHOTO = 1
    private lateinit var auth: FirebaseAuth
    private lateinit var profilePhoto: Bitmap

    private var fileUri: Uri? = null
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
    }

    override fun onStart() {
        super.onStart()
    }

    fun signUp(view: View) {
        var username = registrationUsernameEditText.text.toString()
        var email = registrationEmailEditText.text.toString()
        var password = registrationPasswordEditText.text.toString()

        var isValidUsername = validateUsername(username)
        var isValidEmail = validateEmail(email)
        var isValidPassword = validatePassword(password)
        //
        if (!isValidUsername || !isValidEmail || !isValidPassword) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        uploadImage()
                        val chatListActivityIntent = Intent(this, ChatListActivity::class.java)
                        chatListActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        chatListActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

                        startActivity(chatListActivityIntent)
                        finish()
                    } else {
                        signUpButton.error = getString(R.string.error_registration_failed)

                        val toast = Toast.makeText(
                                applicationContext,
                                getString(R.string.error_registration_failed) + "\n" + task.exception?.message,
                                Toast.LENGTH_LONG
                        )
                        toast.show()
                    }
                }

        // TODO: store profilePhoto and username
    }

    fun navigateBack(view:View) {
        onBackPressed()
    }

    fun pickPhoto(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select profile picture"), PICK_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PHOTO && resultCode == Activity.RESULT_OK) {
            AsyncTask.execute {
                profilePhoto = getProfilePhotoFromIntent(data)
                runOnUiThread {
                    registrationProfilePictureImageView.setImageBitmap(profilePhoto)
                    fileUri = data?.data
                }
            }
        }
    }

    private fun uploadImage() {
        if (fileUri != null) {
            val imageUri = "images/" + UUID.randomUUID().toString()
            val ref = storageRef.child(imageUri)
            ref.putFile(fileUri!!)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(applicationContext, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                    }
        }
    }

    private fun validateUsername(username: String) : Boolean {
        if (username.isBlank()) {
            registrationUsernameTextInputLayout.error = getString(R.string.error_required_field_display_name)
            return false
        }

        // TODO: username must also be unique!

        return true
    }

    private fun validateEmail(email: String) : Boolean {
        if (email.isBlank()) {
            registrationEmailTextInputLayout.error = getString(R.string.error_required_field_email)
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            registrationEmailTextInputLayout.error = getString(R.string.error_invalid_email)
            return false
        }

        return true
    }

    private fun validatePassword(password: String) : Boolean {
        if (password.isBlank()) {
            registrationPasswordTextInputLayout.error = getString(R.string.error_required_field_password)
            return false
        }

        if (password.length < 6) {
            registrationPasswordTextInputLayout.error = getString(R.string.error_invalid_password)
            return false
        }

        return true
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
}