package xyz.shmeleva.eight.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import xyz.shmeleva.eight.R
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import xyz.shmeleva.eight.utilities.getResizedPictureUrl
import xyz.shmeleva.eight.utilities.loadFromFirebase


/**
 * Created by shagg on 26.11.2018.
 */
class ImageGridViewAdapter(private val context: Context, private val images: ArrayList<String>, val clickListener: (String) -> Unit) : BaseAdapter() {

    override fun getCount(): Int = images.size

    override fun getItem(position: Int): Any? = null

    override fun getItemId(position: Int): Long = 0L

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val imageView: ImageView
        if (convertView == null) {
            imageView = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_thumbnail, parent, false) as ImageView
        } else {
            imageView = convertView as ImageView
        }

        imageView.loadFromFirebase(images[position])
        imageView.setOnClickListener { _ -> clickListener(images[position]) }

        return imageView
    }
}