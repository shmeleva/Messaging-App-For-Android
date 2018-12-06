package xyz.shmeleva.eight.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registration.*
import xyz.shmeleva.eight.R
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.FirebaseStorage
import xyz.shmeleva.eight.models.User
import xyz.shmeleva.eight.utilities.DoubleClickBlocker
import xyz.shmeleva.eight.utilities.hideKeyboard
import java.io.ByteArrayOutputStream
import java.util.*

class RegistrationActivity : BaseFragmentActivity() {

    private val doubleClickBlocker: DoubleClickBlocker = DoubleClickBlocker()

    private lateinit var auth: FirebaseAuth
    private var profilePhoto: Bitmap? = null

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

    fun signUp(@Suppress("UNUSED_PARAMETER")view: View) {

        if (doubleClickBlocker.isDoubleClick()) {
            return
        }

        signUpButton.isEnabled = false
        hideKeyboard()

        val username = registrationUsernameEditText.text.toString()
        val email = registrationEmailEditText.text.toString()
        val password = registrationPasswordEditText.text.toString()

        val isValidUsername = validateUsername(username)
        val isValidEmail = validateEmail(email)
        val isValidPassword = validatePassword(password)
        //
        if (!isValidUsername || !isValidEmail || !isValidPassword) {
            signUpButton.isEnabled = true
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val imageUri = if (profilePhoto != null) "images/" + UUID.randomUUID().toString() else "";
                        val user = User(auth.currentUser!!.uid, username, username.toLowerCase(), imageUri)
                        database.child("users").child(user.id).setValue(user)
                                .addOnSuccessListener {

                                    database.child("usernames").child(user.username).setValue(user.id)
                                            .addOnFailureListener{ e->
                                        showErrorResult(e.message)
                                    }

                                    if (profilePhoto != null) {
                                        val baos = ByteArrayOutputStream()
                                        profilePhoto!!.compress(Bitmap.CompressFormat.PNG, 100, baos)
                                        val data = baos.toByteArray()

                                        storageRef.child(imageUri).putBytes(data)
                                                .addOnSuccessListener {
                                                    navigateToChatListActivity()
                                                }
                                                .addOnFailureListener { e ->
                                                    signUpButton.isEnabled = true
                                                    showErrorResult(e.message)
                                                }
                                    } else {
                                        signUpButton.isEnabled = true
                                        navigateToChatListActivity()
                                    }

                                }.addOnFailureListener { _ ->
                                    auth.currentUser?.delete()
                                            ?.addOnCompleteListener{ _ ->
                                                signUpButton.isEnabled = true
                                                showErrorResult("Duplicate username!")
                                            }
                                }
                    } else {
                        signUpButton.isEnabled = true
                        showErrorResult(task.exception?.message)
                    }
                }
    }

    private fun navigateToChatListActivity() {
        hideKeyboard()

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

    fun navigateBack(@Suppress("UNUSED_PARAMETER")view: View) {
        if (doubleClickBlocker.isDoubleClick()) {
            return
        }
        hideKeyboard()
        onBackPressed()
    }

    fun pickPhoto(@Suppress("UNUSED_PARAMETER")view: View) {

        if (doubleClickBlocker.isDoubleClick()) {
            return
        }

        dispatchTakeOrPickPictureIntent { bitmap ->
            profilePhoto = bitmap
            runOnUiThread {
                registrationProfilePictureImageView.setImageBitmap(profilePhoto)
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
}