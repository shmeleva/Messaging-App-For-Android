package xyz.shmeleva.eight.adapters

import android.graphics.BitmapFactory
import android.media.Image
import android.os.AsyncTask
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
import kotlinx.android.synthetic.main.fragment_private_chat_settings.*

import  xyz.shmeleva.eight.R
import  xyz.shmeleva.eight.models.*
import xyz.shmeleva.eight.utilities.TimestampFormatter
import xyz.shmeleva.eight.utilities.loadImages
import java.util.*
import android.R.attr.thumb
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.google.android.flexbox.FlexboxLayout
import jp.wasabeef.glide.transformations.MaskTransformation
import xyz.shmeleva.eight.utilities.getResizedPictureUrl


class MessageListAdapter(
        val userId: String,
        val isGroupChat: Boolean,
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
                val incomingImageMessageView = inflate(R.layout.item_incoming_image_message)
                return IncomingImageMessageViewHolder(incomingImageMessageView)
            }
            OUTGOING_TEXT_MESSAGE -> {
                val outgoingTextMessageView = inflate(R.layout.item_outgoing_text_message)
                return OutgoingTextMessageViewHolder(outgoingTextMessageView)
            }
            OUTGOING_IMAGE_MESSAGE -> {
                val outgoingImageMessageView = inflate(R.layout.item_outgoing_image_message)
                return OutgoingImageMessageViewHolder(outgoingImageMessageView)
            }
        }
        throw IllegalArgumentException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, message: Message) {
        when (holder.itemViewType) {
            INCOMING_TEXT_MESSAGE -> (holder as IncomingTextMessageViewHolder).bind(message, isGroupChat, clickListener)
            INCOMING_IMAGE_MESSAGE -> (holder as IncomingImageMessageViewHolder).bind(message, isGroupChat, clickListener)
            OUTGOING_TEXT_MESSAGE -> (holder as OutgoingTextMessageViewHolder).bind(message, clickListener)
            OUTGOING_IMAGE_MESSAGE -> (holder as OutgoingImageMessageViewHolder).bind(message, clickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        if (message.senderId == userId) {
            return  if (message.text.isNotEmpty()) OUTGOING_TEXT_MESSAGE else OUTGOING_IMAGE_MESSAGE
        }
        return  if (message.text.isNotEmpty()) INCOMING_TEXT_MESSAGE else INCOMING_IMAGE_MESSAGE
    }

    open class IncomingMessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val senderImageView = itemView.findViewById<ImageView>(R.id.incomingMessageSenderImageView)
        val senderTextView = itemView.findViewById<TextView>(R.id.incomingMessageSenderTextView)
        val timeTextView = itemView.findViewById<TextView>(R.id.incomingMessageTimeTextView)
        val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same

        open fun bind(message: Message, isGroupChat: Boolean, clickListener: (Message) -> Unit) {
            if (isGroupChat) {
                senderTextView.text = message.sender?.username ?: ""

                val profilePictureUrl = message.sender?.profilePicUrl ?: ""
                if (profilePictureUrl.isNotEmpty()) {
                    val ref = FirebaseStorage.getInstance().reference.child(profilePictureUrl)
                    Glide.with(senderImageView.context)
                            .load(ref)
                            .apply(bitmapTransform(MultiTransformation<Bitmap>(CenterCrop(),
                                    MaskTransformation(R.drawable.shape_circle_small))))
                            .apply(requestOptions)
                            .into(senderImageView)
                }
                else {
                    val generator = ColorGenerator.MATERIAL
                    val username = message.sender?.username ?: "?"
                    val colour = generator.getColor(username)
                    val drawable = TextDrawable.builder().buildRound(username.substring(0, 1).toUpperCase(), colour)
                    senderImageView.setImageDrawable(drawable)
                }
            }
            else {
                senderTextView.visibility = View.GONE
                senderImageView.visibility = View.GONE
            }
            timeTextView.text = TimestampFormatter.format(message.timestamp)
        }
    }

    class IncomingTextMessageViewHolder(itemView: View): IncomingMessageViewHolder(itemView) {

        val contentTextView = itemView.findViewById<TextView>(R.id.incomingMessageContentTextView)
        val bubbleFlexboxLayout = itemView.findViewById<FlexboxLayout>(R.id.incomingMessageBubbleFlexboxLayout)

        override fun bind(message: Message, isGroupChat: Boolean, clickListener: (Message) -> Unit) {
            super.bind(message, isGroupChat, clickListener)

            bubbleFlexboxLayout.setOnClickListener { clickListener(message) }

            contentTextView.text = message.text
        }
    }

    class IncomingImageMessageViewHolder(itemView: View): IncomingMessageViewHolder(itemView) {

        val contentImageView = itemView.findViewById<ImageView>(R.id.incomingMessageContentImageView)
        val storageRef = FirebaseStorage.getInstance().reference

        override fun bind(message: Message, isGroupChat: Boolean, clickListener: (Message) -> Unit) {
            super.bind(message, isGroupChat, clickListener)

            contentImageView.setOnClickListener { _ -> clickListener(message) }

            val resizedImageUrl = contentImageView.context.getResizedPictureUrl(message.imageUrl)
            val ref = storageRef.child(resizedImageUrl)
            val fallbackRef = storageRef.child(message.imageUrl)
            Glide
                    .with(contentImageView.context)
                    .load(ref)
                    .error(Glide
                            .with(contentImageView.context)
                            .load(fallbackRef)
                            .apply(bitmapTransform(MultiTransformation<Bitmap>(CenterCrop(),
                                    MaskTransformation(R.drawable.bg_incoming_message)))))
                    .apply(bitmapTransform(MultiTransformation<Bitmap>(CenterCrop(),
                            MaskTransformation(R.drawable.bg_incoming_message))))
                    .into(contentImageView)
        }
    }

    open class OutgoingMessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val timeTextView = itemView.findViewById<TextView>(R.id.outgoingMessageTimeTextView)

        open fun bind(message: Message, clickListener: (Message) -> Unit) {
            timeTextView.text = TimestampFormatter.format(message.timestamp)
        }
    }

    class OutgoingTextMessageViewHolder(itemView: View): OutgoingMessageViewHolder(itemView) {

        val contentTextView = itemView.findViewById<TextView>(R.id.outgoingMessageContentTextView)
        val bubbleFlexboxLayout = itemView.findViewById<FlexboxLayout>(R.id.outgoingMessageBubbleFlexboxLayout)

        override fun bind(message: Message, clickListener: (Message) -> Unit) {
            super.bind(message, clickListener)

            bubbleFlexboxLayout.setOnClickListener { clickListener(message) }

            contentTextView.text = message.text
        }
    }

    class OutgoingImageMessageViewHolder(itemView: View): OutgoingMessageViewHolder(itemView) {

        val contentImageView = itemView.findViewById<ImageView>(R.id.outgoingMessageContentImageView)
        val storageRef = FirebaseStorage.getInstance().reference

        override fun bind(message: Message, clickListener: (Message) -> Unit) {
            super.bind(message, clickListener)

            contentImageView.setOnClickListener { _ -> clickListener(message) }

            val resizedImageUrl = contentImageView.context.getResizedPictureUrl(message.imageUrl)
            val ref = storageRef.child(resizedImageUrl)
            val fallbackRef = storageRef.child(message.imageUrl)
            Glide
                    .with(contentImageView.context)
                    .load(ref)
                    .error(Glide
                            .with(contentImageView.context)
                            .load(fallbackRef)
                            .apply(bitmapTransform(MultiTransformation<Bitmap>(CenterCrop(),
                                    MaskTransformation(R.drawable.bg_outgoing_message)))))
                    .apply(bitmapTransform(MultiTransformation<Bitmap>(CenterCrop(),
                            MaskTransformation(R.drawable.bg_outgoing_message))))
                    .into(contentImageView)
        }
    }
}