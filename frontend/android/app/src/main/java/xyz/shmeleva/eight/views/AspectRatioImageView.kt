package xyz.shmeleva.eight.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

import xyz.shmeleva.eight.R

enum class DominantMeasurement(val value: Int) {
    WIDTH(0),
    HEIGHT(1);

    companion object {
        fun from(findValue: Int): DominantMeasurement = DominantMeasurement.values().first { it.value == findValue }
    }
}

class AspectRatioImageView : ImageView {
    private var aspectRatio: Float = 1.0f
    private var dominantMeasurement: DominantMeasurement = DominantMeasurement.WIDTH

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.AspectRatioImageView, defStyle, 0)

        aspectRatio = a.getFloat(
                R.styleable.AspectRatioImageView_aspectRatio,
                aspectRatio)

        dominantMeasurement = DominantMeasurement.from(a.getInt(
                R.styleable.AspectRatioImageView_dominantMeasurement,
                dominantMeasurement.value))

        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (dominantMeasurement == DominantMeasurement.WIDTH)
        {
            var width = measuredWidth
            var height = (width / aspectRatio).toInt()
            setMeasuredDimension(width, height)
        }
        else
        {
            var height = measuredHeight;
            var width = (height / aspectRatio).toInt()
            setMeasuredDimension(width, height)
        }
    }
}
