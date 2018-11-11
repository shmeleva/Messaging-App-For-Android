package xyz.shmeleva.eight.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
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

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser

        // TODO: do something
    }

    fun signUp(view: View) {
        var email = registration_email_text_view.text.toString()
        var password = registration_password_text_view.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        var text = "Registration succeeded for email: " + email + " and password: " + password + "!";
                        Log.d("info", "createUserWithEmail:success")
                        Toast.makeText(baseContext, text,
                                Toast.LENGTH_SHORT).show()
                        val user = auth.currentUser
                    } else {
                        // If sign in fails, display a message to the user.
                        var text = "Registration failed for email: " + email + " and password: " + password;
                        Log.w("info", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, text,
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }
}