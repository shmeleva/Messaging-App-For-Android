package xyz.shmeleva.eight.adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.squareup.picasso.Picasso
import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.utilities.RoundedTransformation

/**
 * Created by shagg on 26.11.2018.
 */
class ImageGridViewAdapter(private val context: Context, private val images: ArrayList<String>, val clickListener: (String) -> Unit) : BaseAdapter() {

    override fun getCount(): Int = images.size

    override fun getItem(position: Int): Any? = null

    override fun getItemId(position: Int): Long = 0L

    // create a new ImageView for each item referenced by the Adapter
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val imageView: ImageView
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_thumbnail, parent, false) as ImageView
        } else {
            imageView = convertView as ImageView
        }

        val builder = Picasso.Builder(context)
        builder.listener(object : Picasso.Listener {
            override fun onImageLoadFailed(picasso: Picasso, uri: Uri, exception: Exception) {
                exception.printStackTrace()
            }
        })
        builder.build().load(images[position]).into(imageView)

        imageView.setOnClickListener { _ -> clickListener(images[position]) }

        return imageView
    }
}