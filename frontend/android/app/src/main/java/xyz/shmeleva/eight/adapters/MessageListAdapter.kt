package xyz.shmeleva.eight.adapters

import android.media.Image
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.stfalcon.multiimageview.MultiImageView

import  xyz.shmeleva.eight.R
import  xyz.shmeleva.eight.models.*
import java.text.SimpleDateFormat
import java.util.*

class MessageListAdapter(val userId: String, val messageList: ArrayList<Message>, val clickListener: (Message) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val INCOMING_TEXT_MESSAGE = 0
    val INCOMING_IMAGE_MESSAGE = 1
    val OUTGOING_TEXT_MESSAGE = 2
    val OUTGOING_IMAGE_MESSAGE = 3

    val formatter = SimpleDateFormat("d MMM yyyy HH:mm", Locale("en", "GB"))
    fun formatTimestamp(timestamp: Long) : String {
        return formatter.format(Date(timestamp))
    }

    override fun getItemCount(): Int {
        return  messageList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = { viewResId: Int -> LayoutInflater.from(parent.context).inflate(viewResId, parent, false) }

        when (viewType) {
            INCOMING_TEXT_MESSAGE -> {
                val incomingTextMessageView = inflate(R.layout.item_incoming_text_message)
                return IncomingTextMessageViewHolder(incomingTextMessageView)
            }
            INCOMING_IMAGE_MESSAGE -> {
                val incomingImageMessageView = inflate(R.layout.item_incoming_text_message)
                return IncomingImageMessageViewHolder(incomingImageMessageView)
            }
            OUTGOING_TEXT_MESSAGE -> {
                val outgoingTextMessageView = inflate(R.layout.item_outgoing_text_message)
                return OutgoingTextMessageViewHolder(outgoingTextMessageView)
            }
            OUTGOING_IMAGE_MESSAGE -> {
                val outgoingImageMessageView = inflate(R.layout.item_outgoing_text_message)
                return OutgoingImageMessageViewHolder(outgoingImageMessageView)
            }
        }
        throw IllegalArgumentException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position];

        when (holder.itemViewType) {
            INCOMING_TEXT_MESSAGE -> {
                val incomingTextMessageViewHolder = holder as IncomingTextMessageViewHolder
                incomingTextMessageViewHolder.senderTextView.text = message.senderId
                // TODO: Load a sender image
                incomingTextMessageViewHolder.contentTextView.text = message.text
                incomingTextMessageViewHolder.timeTextView.text = formatTimestamp(message.timestamp)
            }
            INCOMING_IMAGE_MESSAGE -> {
                val incomingImageMessageViewHolder = holder as IncomingImageMessageViewHolder
                incomingImageMessageViewHolder.senderTextView.text = message.senderId
                // TODO: Load a sender image
                // TODO: Load an image
                incomingImageMessageViewHolder.timeTextView.text = formatTimestamp(message.timestamp)
            }
            OUTGOING_TEXT_MESSAGE -> {
                val outgoingTextMessageViewHolder = holder as OutgoingTextMessageViewHolder
                outgoingTextMessageViewHolder.contentTextView.text = message.text
                outgoingTextMessageViewHolder.timeTextView.text = formatTimestamp(message.timestamp)
            }
            OUTGOING_IMAGE_MESSAGE -> {
                val outgoingImageMessageViewHolder = holder as OutgoingImageMessageViewHolder
                // TODO: Load an image
                outgoingImageMessageViewHolder.timeTextView.text = formatTimestamp(message.timestamp)
            }
        }

        holder.itemView.setOnClickListener { clickListener(message)}
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position];
        if (message.senderId == userId) {
            return  if (message.text.isNotEmpty()) OUTGOING_TEXT_MESSAGE else OUTGOING_IMAGE_MESSAGE
        }
        return  if (message.text.isNotEmpty()) INCOMING_TEXT_MESSAGE else INCOMING_IMAGE_MESSAGE
    }

    open class IncomingMessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val senderImageView = itemView.findViewById<MultiImageView>(R.id.incomingMessageSenderImageView)
        val senderTextView = itemView.findViewById<TextView>(R.id.incomingMessageSenderTextView)
        val timeTextView = itemView.findViewById<TextView>(R.id.incomingMessageTimeTextView)
        val contentTextView = itemView.findViewById<TextView>(R.id.incomingMessageContentTextView)
        val contentImageView = itemView.findViewById<ImageView>(R.id.incomingMessageContentImageView)

        init {
            senderImageView.shape = MultiImageView.Shape.CIRCLE
        }
    }

    class IncomingTextMessageViewHolder(itemView: View): IncomingMessageViewHolder(itemView) {
        init {
            contentImageView.visibility = View.GONE
        }
    }

    class IncomingImageMessageViewHolder(itemView: View): IncomingMessageViewHolder(itemView) {
        init {
            contentTextView.visibility = View.GONE
        }
    }

    open class OutgoingMessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val timeTextView = itemView.findViewById<TextView>(R.id.outgoingMessageTimeTextView)
        val contentTextView = itemView.findViewById<TextView>(R.id.outgoingMessageContentTextView)
        val contentImageView = itemView.findViewById<ImageView>(R.id.outgoingMessageContentImageView)
    }

    class OutgoingTextMessageViewHolder(itemView: View): OutgoingMessageViewHolder(itemView) {
        init {
            contentImageView.visibility = View.GONE
        }
    }

    class OutgoingImageMessageViewHolder(itemView: View): OutgoingMessageViewHolder(itemView) {
        init {
            contentTextView.visibility = View.GONE
        }
    }
}