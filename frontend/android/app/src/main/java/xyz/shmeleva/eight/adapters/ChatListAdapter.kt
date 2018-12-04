package xyz.shmeleva.eight.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.stfalcon.multiimageview.MultiImageView

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.models.*
import xyz.shmeleva.eight.utilities.TimestampFormatter
import java.util.*

/**
 * Created by shagg on 19.11.2018.
 */
//
class ChatListAdapter(val chatList: ArrayList<Chat>, val clickListener: (Chat) -> Unit, val currentUserId: String?) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return  chatList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_preview, parent, false)
        return ViewHolder(v);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position];
        holder.imageView.shape = MultiImageView.Shape.CIRCLE
        holder.participantsTextView.text = chat.getMemberNames(currentUserId)

        if (Date(chat.joinedAt).after(Date(chat.updatedAt))) {
            holder.lastMessageTextView.text = ""
            holder.lastMessageTimeTextView.text = ""
        } else {
            holder.lastMessageTextView.text = chat.lastMessage
            holder.lastMessageTimeTextView.text = TimestampFormatter.format(chat.updatedAt)
        }

        holder.itemView.setOnClickListener { clickListener(chat)}
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageView = itemView.findViewById<MultiImageView>(R.id.chatPreviewPictureImageView)
        val participantsTextView = itemView.findViewById<TextView>(R.id.chatPreviewHeaderTextView)
        val lastMessageTextView = itemView.findViewById<TextView>(R.id.chatPreviewLastMessageTextView)
        val lastMessageTimeTextView = itemView.findViewById<TextView>(R.id.chatPreviewLastMessageTimeTextView)
    }

}