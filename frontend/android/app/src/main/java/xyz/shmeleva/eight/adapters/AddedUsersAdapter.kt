package xyz.shmeleva.eight.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.models.User

class AddedUsersAdapter(val userList: ArrayList<User>) : RecyclerView.Adapter<AddedUsersAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return  userList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_added_user, parent, false)
        return ViewHolder(v);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position];
        holder.textView.text = user.username;
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textView = itemView.findViewById<TextView>(R.id.addedUserTextView)
    }
}