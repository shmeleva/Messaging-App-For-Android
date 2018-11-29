package xyz.shmeleva.eight.models

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Created by shagg on 21.11.2018.
 */
@IgnoreExtraProperties
data class User(
        var username: String?,
        var profilePicUrl: String? = ""
)