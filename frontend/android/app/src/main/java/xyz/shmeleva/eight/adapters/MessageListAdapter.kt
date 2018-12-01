package xyz.shmeleva.eight.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView

import  xyz.shmeleva.eight.R
import  xyz.shmeleva.eight.models.*

class MessageListAdapter(val messageList: ArrayList<Message>, val clickListener: (Message) -> Unit) : RecyclerView.Adapter<MessageListAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return  messageList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ViewHolder(v);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position];
        holder.incomingMessageTextView.text = "Some static text #" + position.toString()
        holder.itemView.setOnClickListener { clickListener(message)}
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val incomingMessageTextView = itemView.findViewById<TextView>(R.id.incomingMessageTextView)
    }

}