package xyz.shmeleva.eight.utilities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import com.squareup.picasso.Picasso
import com.stfalcon.multiimageview.MultiImageView
import kotlinx.android.synthetic.main.fragment_chat.*
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import xyz.shmeleva.eight.R


/**
 * Created by shagg on 22.11.2018.
 */

// Load user profile pictures:
fun MultiImageView.loadImages(urls: ArrayList<String>, width: Int, height: Int) {
    val imageView = this
    imageView.shape = MultiImageView.Shape.CIRCLE
    imageView.clear()

    urls.forEach {url ->
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
    }
}

fun MultiImageView.loadUsername(username: String) {
    val imageView = this
    imageView.shape = MultiImageView.Shape.CIRCLE
    imageView.clear()

    val generator = ColorGenerator.MATERIAL;
    val colour = generator.getColor(username)

    val drawable = TextDrawable.builder().buildRect(username.substring(0, 1).toUpperCase(), colour)
    val canvas = Canvas()
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    canvas.setBitmap(bitmap)
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    drawable.draw(canvas)

    imageView.addImage(bitmap)
}