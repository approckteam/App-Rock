package com.mankirat.approck.lib.admob

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.mankirat.approck.lib.R
import kotlin.math.roundToInt

class NativeAdStyle {

    var background: Drawable? = null
    var backgroundTint: Int = Color.BLUE
    fun getBackground(context: Context): Drawable? {
        return if (background == null) {
            val drawable = ContextCompat.getDrawable(context, R.drawable.bg_corner_10) as GradientDrawable
            val strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics).roundToInt()
            drawable.setStroke(strokeWidth, backgroundTint)
            drawable
        } else {
            background
        }
    }

    var headlineTextColor: Int = Color.BLUE
    var advertiserTextColor: Int = Color.GRAY
    var starTint: Int = Color.BLUE
    var adTextColor: Int = Color.WHITE
    var adBackColor: Int = Color.BLUE

    var bodyTextColor: Int = Color.GRAY

    var priceTextColor: Int = Color.BLUE
    var storeTextColor: Int = Color.BLUE
    var actionTextColor: Int = Color.WHITE
    var actionBackColor: Int = Color.BLUE

    fun setColorTheme(color: Int) {
        backgroundTint = color

        headlineTextColor = color
        starTint = color
        adBackColor = color

        priceTextColor = color
        storeTextColor = color
        actionBackColor = color
    }
}