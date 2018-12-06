package xyz.shmeleva.eight.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * Created by shagg on 21.11.2018.
 */
@IgnoreExtraProperties
data class User(
        var id: String = "",
        var username: String = "",
        var lowercaseUsername: String = "",
        var profilePicUrl: String = "",
        var chats: MutableMap<String, MutableMap<String, Long>> = mutableMapOf(),
        var isSelected: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (other !is User) {
            return false
        }

        return other.id == id
    }

    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
                "id" to id,
                "username" to username,
                "lowercaseUsername" to lowercaseUsername,
                "profilePicUrl" to profilePicUrl,
                "chats" to chats
        )
    }
}