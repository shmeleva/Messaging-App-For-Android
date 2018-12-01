package xyz.shmeleva.eight.models

import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

/**
 * Created by shagg on 19.11.2018.
 */
@IgnoreExtraProperties
class Chat(
        var id: String = "",
        var isGroupChat: Boolean = false,
        var members: MutableMap<String, Boolean> = mutableMapOf(),
        var lastMessage: String = "",
        var createdAt: Long = Date().time,
        var updatedAt: Long = Date().time
) {
    var users: MutableList<User> = mutableListOf()
    var messages: List<Message> = listOf()
    var joinedAt: Long = Date().time

    override fun equals(other: Any?): Boolean {
        if (other !is Chat) {
            return false
        }

        return other.id == id
    }

    fun isMember(uid: String): Boolean {
        return members.containsKey(uid) && members[uid]!!
    }

    fun updateMember(user: User) {
        // TODO
    }

    fun getMemberNames(currentUserUid: String): String {
        val memberNames: MutableList<String> = mutableListOf()
        for (user in users) {
            if (user.id != currentUserUid) {
                memberNames.add(user.username)
            }
        }
        return memberNames.joinToString(", ")
    }
}