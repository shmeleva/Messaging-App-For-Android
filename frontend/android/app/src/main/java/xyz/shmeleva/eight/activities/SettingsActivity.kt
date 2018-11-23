package xyz.shmeleva.eight.activities


import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View

import android.view.WindowManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.settings_username_dialog.*
import kotlinx.android.synthetic.main.settings_username_dialog.view.*
import xyz.shmeleva.eight.R
import android.content.DialogInterface
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log


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

        val sharedPreferences = getSharedPreferences("userSettings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        Picasso.get()
               .load("https://pixel.nymag.com/imgs/daily/vulture/2016/11/23/23-san-junipero.w330.h330.jpg")
               .into(profilePictureImageView);

        // user settings variables
        var username = sharedPreferences.getString("username", "username")
        var uploadImageResolution = sharedPreferences.getString("uploadResolution", "Low")
        var downloadImageResolution = sharedPreferences.getString("downloadResolution", "Low")
        var theme = sharedPreferences.getString("theme", "Seaweed")

        val imageResolutionArray = arrayOf(
                getResources().getString(R.string.settings_image_resolution_low),
                getResources().getString(R.string.settings_image_resolution_medium),
                getResources().getString(R.string.settings_image_resolution_high)
        )
        val themeArray = arrayOf("Seaweed", "Dark")

        setUsernameLabel(username)
        setUploadResolutionLabel(uploadImageResolution)
        setDownloadResolutionLabel(downloadImageResolution)
        setThemeLabel(theme)

        usernameRelativeLayout.setOnClickListener() {

            val usernameDialog = LayoutInflater.from(this)
                    .inflate(R.layout.settings_username_dialog, null)

            val dialogEditText = usernameDialog.findViewById<EditText>(R.id.dialogUsername)
            dialogEditText.setText(username)

            val mBuilder = AlertDialog.Builder(this)
                    .setView(usernameDialog)
                    .setPositiveButton(R.string.settings_prompt_save_btn, DialogInterface.OnClickListener { dialog, whichButton ->
                        val changedUsername = usernameDialog.dialogUsername.text.toString()
                        usernameTextView.setText(changedUsername)
                        editor.putString("username", changedUsername)
                        editor.apply()
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
