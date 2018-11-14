package xyz.shmeleva.eight.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*
import xyz.shmeleva.eight.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        Picasso.get()
               .load("https://pixel.nymag.com/imgs/daily/vulture/2016/11/23/23-san-junipero.w330.h330.jpg")
               .into(profilePictureImageView);
    }

    override fun onStart() {
        super.onStart()
    }

    fun navigateBack(view: View) {
        onBackPressed()
    }
}
