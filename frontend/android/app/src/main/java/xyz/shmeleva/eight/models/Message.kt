package xyz.shmeleva.eight.models

/**
 * Created by shagg on 19.11.2018.
 */
class Message {
    var id: Int = 0
    var isGroupChat: Boolean = false
    var messages: List<Message> = arrayListOf<Message>()
}