package xyz.shmeleva.eight.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by shagg on 19.11.2018.
 */
@IgnoreExtraProperties
class Chat(
        var id: String = "",
        @field:JvmField var isGroupChat: Boolean = false,
        var members: Map<String, Boolean> = mapOf(),
        var lastMessage: String = "",
        var updatedAt: Long = Date().time // Should be updated with chat is created and a new message is sent
) {
    var users: MutableList<User> = mutableListOf()
    var joinedAt: Long = Date().time

    override fun equals(other: Any?): Boolean {
        if (other !is Chat) {
            return false
        }

        return other.id == id
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "id" to id,
                "isGroupChat" to isGroupChat,
                "members" to members,
                "lastMessage" to lastMessage,
                "updatedAt" to updatedAt
        )
    }

    fun isMember(uid: String): Boolean {
        return members.containsKey(uid) && members[uid]!!
    }

    fun updateMember(user: User) {
        if (users.contains(user)) {
            users.remove(user)
        }
        users.add(user)
    }

    fun getMemberNames(currentUserId: String?): String {
        val memberNames: MutableList<String> = mutableListOf()
        for (user in users) {
            if (currentUserId == null || user.id != currentUserId) {
                memberNames.add(user.username)
            }
        }
        return memberNames.joinToString(", ")
    }

    fun getMemberNamesList(currentUserId: String?) : ArrayList<String> {
        val memberNames: ArrayList<String> = ArrayList()
        for (user in users) {
            if (currentUserId == null || user.id != currentUserId) {
                memberNames.add(user.username)
            }
        }
        return memberNames
    }

    fun getMemberIds(currentUserId: String?) : ArrayList<String> {
        val memberIds: ArrayList<String> = ArrayList()
        for (user in users) {
            if (currentUserId == null || user.id != currentUserId) {
                memberIds.add(user.id)
            }
        }
        return memberIds
    }
}