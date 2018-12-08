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


/**
 * Created by shagg on 26.11.2018.
 */
class ImageGridViewAdapter(private val context: Context, private val images: ArrayList<String>, val clickListener: (String) -> Unit) : BaseAdapter() {

    val storageRef = FirebaseStorage.getInstance().reference

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

        val resizedImageUrl = imageView.context.getResizedPictureUrl(images[position])
        val ref = storageRef.child(resizedImageUrl)
        val fallbackRef = storageRef.child(images[position])
        Glide
                .with(imageView.context)
                .load(ref)
                .error(Glide
                        .with(imageView.context)
                        .load(fallbackRef))
                .into(imageView)

        imageView.setOnClickListener { _ -> clickListener(images[position]) }

        return imageView
    }
}