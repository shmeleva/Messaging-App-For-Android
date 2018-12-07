package xyz.shmeleva.eight.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

/**
 * Created by shagg on 19.11.2018.
 */
@IgnoreExtraProperties
class Message(
        var id: String = "",
        var text: String = "",
        var senderId: String = "",
        var timestamp: Long = Date().time,
        var imageUrl: String = "",
        var imageFeature: String = "",
        var imageTimestamp: Long = 0,
        var receivers: ArrayList<String> = ArrayList()
) {
    var sender: User? = null

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "id" to id,
                "text" to text,
                "imageUrl" to imageUrl,
                "senderId" to senderId,
                "timestamp" to timestamp,
                "imageFeature" to text,
                "imageTimestamp" to timestamp,
                "receivers" to receivers
        )
    }
}