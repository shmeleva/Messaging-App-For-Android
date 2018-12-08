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
import com.bumptech.glide.request.target.SimpleTarget
import android.R.attr.path
import android.media.Image
import android.transition.Transition
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import jp.wasabeef.glide.transformations.MaskTransformation
import xyz.shmeleva.eight.models.User


/**
 * Created by shagg on 22.11.2018.
 */

// Load user profile pictures:
fun MultiImageView.loadImages(urls: ArrayList<String>, width: Int, height: Int) {
    val imageView = this
    imageView.shape = MultiImageView.Shape.CIRCLE
    imageView.clear()

    urls.forEach { url ->
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
        /*Glide.with(this)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap> (100, 100) {
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        callback.onReady(createMarkerIcon(resource, iconId))
                    }
                })*/
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

fun ImageView.loadProfilePictureFromFirebase(user: User?) {
    if (user?.profilePicUrl != null && user.profilePicUrl.isNotEmpty()) {
        val ref = FirebaseStorage.getInstance().reference.child(user.profilePicUrl)
        val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
        Glide.with(context)
                .load(ref)
                .apply(RequestOptions.bitmapTransform(MultiTransformation<Bitmap>(CenterCrop(),
                        MaskTransformation(R.drawable.shape_circle_small))))
                .apply(requestOptions)
                .into(this)
    }
    else {
        val generator = ColorGenerator.MATERIAL
        val username = if (user?.username != null && user.username.isNotEmpty()) user.username else "?"
        val colour = generator.getColor(username)
        val drawable = TextDrawable.builder().buildRound(username.substring(0, 1).toUpperCase(), colour)
        setImageDrawable(drawable)
    }
}

fun ImageView.loadFromFirebase(url: String) {
    val storageRef = FirebaseStorage.getInstance().reference
    val resizedImageUrl = context.getResizedPictureUrl(url)
    val ref = storageRef.child(resizedImageUrl)
    val fallbackRef = storageRef.child(url)
    Glide
            .with(context)
            .load(ref)
            .error(Glide
                    .with(context)
                    .load(fallbackRef))
            .into(this)
}

fun ImageView.loadFullProfilePictureFromFirebase(user: User?) {
    if (user?.profilePicUrl != null && user.profilePicUrl.isNotEmpty()) {
        val storageRef = FirebaseStorage.getInstance().reference
        val resizedImageUrl = context.getResizedPictureUrl(user.profilePicUrl)
        val ref = storageRef.child(resizedImageUrl)
        val fallbackRef = storageRef.child(user.profilePicUrl)
        val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)// because file name is always same
        Glide.with(context)
                .load(ref)
                .error(Glide
                        .with(context)
                        .load(fallbackRef)
                        .apply(requestOptions))
                .apply(requestOptions)
                .into(this)
    }
    else {
        val generator = ColorGenerator.MATERIAL
        val username = if (user?.username != null && user.username.isNotEmpty()) user.username else "?"
        val colour = generator.getColor(username)
        val drawable = TextDrawable.builder().buildRect(username.substring(0, 1).toUpperCase(), colour)
        setImageDrawable(drawable)
    }
}

fun ImageView.loadAndReshapeFromFirebase(url: String, shapeResId: Int) {
    val storageRef = FirebaseStorage.getInstance().reference
    val resizedImageUrl = context.getResizedPictureUrl(url)
    val ref = storageRef.child(resizedImageUrl)
    val fallbackRef = storageRef.child(url)
    Glide
            .with(context)
            .load(ref)
            .error(Glide
                    .with(context)
                    .load(fallbackRef)
                    .apply(RequestOptions.bitmapTransform(MultiTransformation<Bitmap>(CenterCrop(),
                            MaskTransformation(shapeResId)))))
            .apply(RequestOptions.bitmapTransform(MultiTransformation<Bitmap>(CenterCrop(),
                    MaskTransformation(shapeResId))))
            .into(this)
}