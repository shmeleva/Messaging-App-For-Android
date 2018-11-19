package xyz.shmeleva.eight.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.ArrayList
import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registration.*
import xyz.shmeleva.eight.R

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        //
        loginEmailEditText.addTextChangedListener((object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                loginEmailTextInputLayout.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }))
        //
        loginPasswordEditText.addTextChangedListener((object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                loginPasswordTextInputLayout.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }))
    }

    fun logIn(view: View) {
        var email = loginEmailEditText.text.toString()
        var password = loginPasswordEditText.text.toString()
        //
        var isValidEmail = validateEmail(email)
        var isValidPassword = validatePassword(password)
        //
        if (!isValidEmail || !isValidPassword) {
            return
        }
        //
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, ChatListActivity::class.java)
                        startActivity(intent)
                    }
                }
    }

    fun startRegistrationActivity(view: View) {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun validateEmail(email: String) : Boolean {
        if (email.isBlank()) {
            loginEmailTextInputLayout.error = getString(R.string.error_required_field_email)
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmailTextInputLayout.error = getString(R.string.error_invalid_email)
            return false
        }
        return true
    }

    private fun validatePassword(password: String) : Boolean {
        if (password.isBlank()) {
            loginPasswordTextInputLayout.error = getString(R.string.error_required_field_password)
            return false
        }
        if (password.length < 6) {
            loginPasswordTextInputLayout.error = getString(R.string.error_invalid_password)
            return false
        }
        return true
    }
}