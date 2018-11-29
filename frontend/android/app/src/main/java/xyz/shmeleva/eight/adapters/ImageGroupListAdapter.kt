package xyz.shmeleva.eight.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ListAdapter
import android.widget.TextView
import xyz.shmeleva.eight.R

/**
 * Created by shagg on 26.11.2018.
 */
class ImageGroupListAdapter(val context: Context, val imageGroups: ArrayList<Pair<String, ArrayList<String>>>, val clickListener: (String) -> Unit) : RecyclerView.Adapter<ImageGroupListAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return  imageGroups.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_group, parent, false)
        return ViewHolder(v);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageGroup = imageGroups[position]
        holder.titleTextView.text = imageGroup.first
        holder.gridView.adapter = ImageGridViewAdapter(context, imageGroup.second, clickListener)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val titleTextView = itemView.findViewById<TextView>(R.id.galleryGroupTitleTextView)
        val gridView = itemView.findViewById<GridView>(R.id.galleryGroupGridView)
    }
}