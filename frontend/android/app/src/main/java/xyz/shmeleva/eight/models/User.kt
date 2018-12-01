package xyz.shmeleva.eight.models

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Created by shagg on 21.11.2018.
 */
@IgnoreExtraProperties
data class User(
        var id: String = "",
        var username: String = "",
        var profilePicUrl: String = "",
        var chats: MutableMap<String, MutableMap<String, Long>> = mutableMapOf()
) {
    override fun equals(other: Any?): Boolean {
        if (other !is User) {
            return false
        }

        return other.id == id
    }
}