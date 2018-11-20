package xyz.shmeleva.eight.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_registration.*
import xyz.shmeleva.eight.R

class RegistrationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
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
                        val profileUpdate = UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build()
                        auth.currentUser?.updateProfile(profileUpdate)
                                ?.addOnCompleteListener{ task ->
                                    if (task.isSuccessful) {
                                        // TODO: do something
                                    } else {
                                        // TODO: do something else
                                    }
                                }

                        // TODO: redirect straight to the chat list
                        auth.signOut()

                        val loginActivityIntent = Intent(this, LoginActivity::class.java)
                                .putExtra(getString(R.string.extra_message_key), getString(R.string.message_registration_successful))
                        loginActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        loginActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

                        startActivity(loginActivityIntent)
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
    }

    fun navigateBack(view:View) {
        onBackPressed()
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
        /*
        if (password.length < 6) {
            loginPasswordTextInputLayout.error = getString(R.string.error_invalid_password)
            return false
        }
        */
        return true
    }
}