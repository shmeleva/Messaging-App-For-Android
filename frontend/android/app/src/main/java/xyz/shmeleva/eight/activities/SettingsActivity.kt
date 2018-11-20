package xyz.shmeleva.eight.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*
import xyz.shmeleva.eight.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        Picasso.get()
               .load("https://pixel.nymag.com/imgs/daily/vulture/2016/11/23/23-san-junipero.w330.h330.jpg")
               .into(profilePictureImageView)
    }

    override fun onStart() {
        super.onStart()
    }

    fun navigateBack(view: View) {
        onBackPressed()
    }

    fun logOut(view: View) {
        auth.signOut()

        val loginActivityIntent = Intent(this, LoginActivity::class.java)
                .putExtra(getString(R.string.extra_message_key), getString(R.string.message_logout))
        loginActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        loginActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        startActivity(loginActivityIntent)
        finish()
    }
}
