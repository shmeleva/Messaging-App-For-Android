package xyz.shmeleva.eight.adapters

import android.media.Image
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.stfalcon.multiimageview.MultiImageView
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import  xyz.shmeleva.eight.R
import  xyz.shmeleva.eight.models.*
import xyz.shmeleva.eight.utilities.TimestampFormatter
import xyz.shmeleva.eight.utilities.loadImages
import java.util.*

class MessageListAdapter(
        val userId: String,
        val isGroupChat: Boolean,
        val messageList: ArrayList<Message> = arrayListOf(),
        val options: FirebaseRecyclerOptions<Message>,
        val clickListener: (Message) -> Unit
) : FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {

    private val TAG = "MessageListAdapter"

    val INCOMING_TEXT_MESSAGE = 0
    val INCOMING_IMAGE_MESSAGE = 1
    val OUTGOING_TEXT_MESSAGE = 2
    val OUTGOING_IMAGE_MESSAGE = 3

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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, message: Message) {
        when (holder.itemViewType) {
            INCOMING_TEXT_MESSAGE -> (holder as IncomingTextMessageViewHolder).bind(message, isGroupChat)
            INCOMING_IMAGE_MESSAGE -> (holder as IncomingImageMessageViewHolder).bind(message, isGroupChat)
            OUTGOING_TEXT_MESSAGE -> (holder as OutgoingTextMessageViewHolder).bind(message)
            OUTGOING_IMAGE_MESSAGE -> (holder as OutgoingImageMessageViewHolder).bind(message)
        }
        holder.itemView.setOnClickListener { clickListener(message)}
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
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

        open fun bind(message: Message, isGroupChat: Boolean) {
            if (isGroupChat) {
                senderTextView.text = message.senderId
                senderImageView.loadImages(arrayListOf("https://pixel.nymag.com/imgs/daily/vulture/2016/11/23/23-san-junipero.w330.h330.jpg"), 24, 0)
            }
            else {
                senderTextView.visibility = View.GONE
                senderImageView.visibility = View.GONE
            }
            timeTextView.text = TimestampFormatter.format(message.timestamp)
        }
    }

    class IncomingTextMessageViewHolder(itemView: View): IncomingMessageViewHolder(itemView) {
        init {
            contentImageView.visibility = View.GONE
        }

        override fun bind(message: Message, isGroupChat: Boolean) {
            super.bind(message, isGroupChat)
            contentTextView.text = message.text
        }
    }

    class IncomingImageMessageViewHolder(itemView: View): IncomingMessageViewHolder(itemView) {

        private val requestOptions = RequestOptions().transforms(CenterCrop(), RoundedCorners(15))

        init {
            contentTextView.visibility = View.GONE
        }

        override fun bind(message: Message, isGroupChat: Boolean) {
            super.bind(message, isGroupChat)
            Glide.with(contentImageView.context)
                    .load(message.imageUrl)
                    .apply(requestOptions)
                    .into(contentImageView)
        }
    }

    open class OutgoingMessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val timeTextView = itemView.findViewById<TextView>(R.id.outgoingMessageTimeTextView)
        val contentTextView = itemView.findViewById<TextView>(R.id.outgoingMessageContentTextView)
        val contentImageView = itemView.findViewById<ImageView>(R.id.outgoingMessageContentImageView)

        open fun bind(message: Message) {
            timeTextView.text = TimestampFormatter.format(message.timestamp)
        }
    }

    class OutgoingTextMessageViewHolder(itemView: View): OutgoingMessageViewHolder(itemView) {
        init {
            contentImageView.visibility = View.GONE
        }

        override fun bind(message: Message) {
            super.bind(message)
            contentTextView.text = message.text
        }
    }

    class OutgoingImageMessageViewHolder(itemView: View): OutgoingMessageViewHolder(itemView) {

        private val requestOptions = RequestOptions().transforms(CenterCrop(), RoundedCorners(15))

        init {
            contentTextView.visibility = View.GONE
        }

        override fun bind(message: Message) {
            super.bind(message)
            Glide.with(contentImageView.context)
                    .load(message.imageUrl)
                    .apply(requestOptions)
                    .into(contentImageView)
        }
    }
}