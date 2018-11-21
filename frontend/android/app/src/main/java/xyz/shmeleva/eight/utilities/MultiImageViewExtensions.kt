package xyz.shmeleva.eight.utilities

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.squareup.picasso.Picasso
import com.stfalcon.multiimageview.MultiImageView
import kotlinx.android.synthetic.main.fragment_chat.*

/**
 * Created by shagg on 22.11.2018.
 */

// Load user profile pictures:
fun MultiImageView.loadImages(urls: ArrayList<String>, width: Int, height: Int) {
    val imageView = this
    imageView.shape = MultiImageView.Shape.CIRCLE
    imageView.clear()

    urls.forEach({url ->
        Picasso
                .get()
                .load(url)
                .resize(width, height)
                .into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        if (bitmap != null) {
                            imageView.addImage(bitmap)
                        }
                    }
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                })
    })
}