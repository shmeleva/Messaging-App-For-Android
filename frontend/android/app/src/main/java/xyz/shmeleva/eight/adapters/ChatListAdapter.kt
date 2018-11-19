package xyz.shmeleva.eight.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import  xyz.shmeleva.eight.R
import  xyz.shmeleva.eight.models.*

/**
 * Created by shagg on 19.11.2018.
 */
class ChatListAdapter(val chatList: ArrayList<Chat>) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return  chatList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_preview, parent, false)
        return ViewHolder(v);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position];
        holder.participantsTextView.text = chat.id.toString(); // For tests.
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageView = itemView.findViewById<ImageView>(R.id.chatPreviewPictureImageView)
        val participantsTextView = itemView.findViewById<TextView>(R.id.chatPreviewHeaderTextView)
        val lastMessageTextView = itemView.findViewById<TextView>(R.id.chatPreviewLastMessageTextView)
        val lastMessageTimeTextView = itemView.findViewById<TextView>(R.id.chatPreviewLastMessageTimeTextView)
    }

}