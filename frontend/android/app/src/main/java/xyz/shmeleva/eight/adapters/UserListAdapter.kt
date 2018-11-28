package xyz.shmeleva.eight.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.stfalcon.multiimageview.MultiImageView

import  xyz.shmeleva.eight.R
import  xyz.shmeleva.eight.models.*

/**
 * Created by shagg on 19.11.2018.
 */
//
class UserListAdapter(val userList: ArrayList<User>,
                      val clickListener: (User) -> Unit,
                      val selectListener: (User, Boolean) -> Unit,
                      val selectionEnabled: Boolean) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return  userList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(v);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position];
        holder.imageView.shape = MultiImageView.Shape.CIRCLE
        holder.textView.text = user.username;

        if (selectionEnabled) {
            holder.checkBox.visibility = View.VISIBLE
            holder.itemView.setOnClickListener { holder.checkBox.isChecked = !holder.checkBox.isChecked }
            holder.checkBox.setOnCheckedChangeListener { _, isChecked -> selectListener(user, isChecked) }
        }
        else {
            holder.checkBox.visibility = View.GONE
            holder.itemView.setOnClickListener { clickListener(user)}
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageView = itemView.findViewById<MultiImageView>(R.id.userImageView)
        val textView = itemView.findViewById<TextView>(R.id.userTextView)
        val checkBox = itemView.findViewById<CheckBox>(R.id.userCheckBox)
    }
}