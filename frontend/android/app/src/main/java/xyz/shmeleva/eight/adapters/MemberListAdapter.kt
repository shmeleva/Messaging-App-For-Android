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
class MemberListAdapter(val userList: ArrayList<String>) : RecyclerView.Adapter<MemberListAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return  userList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(v);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val username = userList[position];
        holder.imageView.shape = MultiImageView.Shape.CIRCLE
        holder.textView.text = username;

        holder.checkBox.visibility = View.GONE
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageView = itemView.findViewById<MultiImageView>(R.id.userImageView)
        val textView = itemView.findViewById<TextView>(R.id.userTextView)
        val checkBox = itemView.findViewById<CheckBox>(R.id.userCheckBox)
    }
}