package xyz.shmeleva.eight.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        //
        val isAuthenticated = auth.currentUser != null;
        //
        val mainActivityIntent = Intent(this, if (isAuthenticated) ChatListActivity::class.java else LoginActivity::class.java);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        //
        startActivity(mainActivityIntent)
        finish()
    }
}
