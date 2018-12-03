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
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_settings.*
import xyz.shmeleva.eight.R
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.FirebaseStorage
import xyz.shmeleva.eight.models.User
import java.io.ByteArrayOutputStream
import java.time.temporal.TemporalAdjusters.next
import java.util.*

class RegistrationActivity : AppCompatActivity() {
    @JvmField
    val PICK_PHOTO = 1
    private lateinit var auth: FirebaseAuth
    private lateinit var profilePhoto: Bitmap

    private var fileUri: Uri? = null
    private lateinit var storageRef: StorageReference
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        database = FirebaseDatabase.getInstance().reference
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

                        val imageUri = if (fileUri != null) "images/" + UUID.randomUUID().toString() else "";
                        val user = User(auth.currentUser!!.uid, username, imageUri)
                        database.child("users").child(user.id).setValue(user)
                                .addOnSuccessListener {

                                    database.child("usernames").child(user.username).setValue(user.id)
                                            .addOnFailureListener{e->
                                        showErrorResult(e.message)
                                    }

                                    if (fileUri != null) {
                                        val baos = ByteArrayOutputStream()
                                        profilePhoto.compress(Bitmap.CompressFormat.PNG, 100, baos)
                                        val data = baos.toByteArray()

                                        storageRef.child(imageUri).putBytes(data)
                                                .addOnSuccessListener {
                                                    navigatToChatListActivity()
                                                }
                                                .addOnFailureListener { e ->
                                                    showErrorResult(e.message)
                                                }
                                    } else {
                                        navigatToChatListActivity()
                                    }

                                }.addOnFailureListener { e ->
                                    auth.currentUser?.delete()
                                            ?.addOnCompleteListener{ task ->
                                                showErrorResult("Duplicate username!")
                                            }
                                }
                    } else {
                        showErrorResult(task.exception?.message)
                    }
                }
    }

    private fun navigatToChatListActivity() {
        val chatListActivityIntent = Intent(this, ChatListActivity::class.java)
        chatListActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        chatListActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        startActivity(chatListActivityIntent)
        finish()
    }

    private fun showErrorResult(message: String?) {
        signUpButton.error = getString(R.string.error_registration_failed)

        val toast = Toast.makeText(
                applicationContext,
                getString(R.string.error_registration_failed) + "\n" + message,
                Toast.LENGTH_LONG
        )
        toast.show()
    }

    fun navigateBack(view: View) {
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

    private fun validateUsername(username: String): Boolean {
        if (username.isBlank()) {
            registrationUsernameTextInputLayout.error = getString(R.string.error_required_field_display_name)
            return false
        }

        return true
    }

    private fun validateEmail(email: String): Boolean {
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

    private fun validatePassword(password: String): Boolean {
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

    private fun getProfilePhotoFromIntent(intentData: Intent?): Bitmap {
        var bitmap: Bitmap
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