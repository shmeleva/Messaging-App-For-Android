package xyz.shmeleva.eight.adapters

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.stfalcon.multiimageview.MultiImageView
import jp.wasabeef.glide.transformations.MaskTransformation
import kotlinx.android.synthetic.main.fragment_chat.*

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.models.*
import xyz.shmeleva.eight.utilities.TimestampFormatter
import java.util.*

/**
 * Created by shagg on 19.11.2018.
 */
//
class ChatListAdapter(val chatList: ArrayList<Chat>, val clickListener: (Chat) -> Unit, val currentUserId: String) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return  chatList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_preview, parent, false)
        return ViewHolder(v);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position];
        holder.participantsTextView.text = chat.getMemberNames(currentUserId)

        if (chat.isGroupChat) {
            val numberOfMembers = chat.members.size.toString()
            val generator = ColorGenerator.MATERIAL
            val colour = generator.getColor(numberOfMembers)
            val drawable = TextDrawable.builder().buildRoundRect(numberOfMembers, colour, 48)
            holder.imageView.setImageDrawable(drawable)
        }
        else {
            val secondUser = chat.users.firstOrNull{ it.id != currentUserId }
            val profilePictureUrl = secondUser?.profilePicUrl
            if (profilePictureUrl != null && profilePictureUrl.isNotEmpty()) {
                val ref = FirebaseStorage.getInstance().reference.child(profilePictureUrl)
                val requestOptions = RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                Glide.with(holder.imageView.context)
                        .load(ref)
                        .apply(RequestOptions.bitmapTransform(MultiTransformation<Bitmap>(CenterCrop(),
                                MaskTransformation(R.drawable.shape_circle_small))))
                        .apply(requestOptions)
                        .into(holder.imageView)
            }
            else {
                val generator = ColorGenerator.MATERIAL
                val username = secondUser?.username ?: "?"
                val colour = generator.getColor(username)

                val drawable = TextDrawable.builder().buildRoundRect(username.substring(0, 1).toUpperCase(), colour, 48)
                holder.imageView.setImageDrawable(drawable)
            }
        }


        if (Date(chat.joinedAt).after(Date(chat.updatedAt))) {
            holder.lastMessageTextView.text = ""
            holder.lastMessageTimeTextView.text = TimestampFormatter.format(chat.joinedAt)
        } else {
            holder.lastMessageTextView.text = chat.lastMessage
            holder.lastMessageTimeTextView.text = TimestampFormatter.format(chat.updatedAt)
        }

        holder.itemView.setOnClickListener { clickListener(chat)}
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageView = itemView.findViewById<ImageView>(R.id.chatPreviewPictureImageView)
        val participantsTextView = itemView.findViewById<TextView>(R.id.chatPreviewHeaderTextView)
        val lastMessageTextView = itemView.findViewById<TextView>(R.id.chatPreviewLastMessageTextView)
        val lastMessageTimeTextView = itemView.findViewById<TextView>(R.id.chatPreviewLastMessageTimeTextView)
    }

}