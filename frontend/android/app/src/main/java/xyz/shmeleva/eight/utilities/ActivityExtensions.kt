package xyz.shmeleva.eight.utilities

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import xyz.shmeleva.eight.R

fun Activity.hideKeyboard() {
    val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
}

fun Context.getResizedPictureUrl(pictureUrl: String) : String {
    val sharedPreferences = getSharedPreferences("userSettings", Context.MODE_PRIVATE)
    val defaultResolution = resources.getString(R.string.settings_image_resolution_default)
    val downloadImageResolution = sharedPreferences.getString("downloadResolution", defaultResolution)

    when (downloadImageResolution) {
        resources.getString(R.string.settings_image_resolution_low)
            -> return pictureUrl.replaceFirst("images/", "images/${resources.getString(R.string.image_resolution_firebase_prefix_low)}")
        resources.getString(R.string.settings_image_resolution_high)
            -> return pictureUrl.replaceFirst("images/", "images/${resources.getString(R.string.image_resolution_firebase_prefix_high)}")
    }

    return pictureUrl
}