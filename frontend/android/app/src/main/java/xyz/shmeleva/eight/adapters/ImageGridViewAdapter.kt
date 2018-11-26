package xyz.shmeleva.eight.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.squareup.picasso.Picasso

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
            imageView = ImageView(context)
            imageView.layoutParams = ViewGroup.LayoutParams(85, 85)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(8, 8, 8, 8)
        } else {
            imageView = convertView as ImageView
        }

        Picasso.get().load(images[position]).into(imageView);

        return imageView
    }
}