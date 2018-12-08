package xyz.shmeleva.eight.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.AbsListView
import android.widget.GridView
import xyz.shmeleva.eight.R

class WrapContentGridView : GridView {
    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpec: Int
        if (layoutParams.height == AbsListView.LayoutParams.WRAP_CONTENT) {
            heightSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST)
        } else {
            heightSpec = heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, heightSpec)
    }
}
