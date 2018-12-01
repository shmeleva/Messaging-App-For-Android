package xyz.shmeleva.eight.models

import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

/**
 * Created by shagg on 19.11.2018.
 */
@IgnoreExtraProperties
class Message(
        var id: String = "",
        var text: String = "",
        var imageUrl: String = "",
        var senderId: String = "",
        var timestamp: Long = Date().time
) {

}