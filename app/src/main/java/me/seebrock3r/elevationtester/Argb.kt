package me.seebrock3r.elevationtester

import android.graphics.Color
import android.os.Build
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlinx.parcelize.Parcelize

@Parcelize
data class Argb(
    @FloatRange(from = 0.0, to = 255.0) var a: Float,
    @FloatRange(from = 0.0, to = 255.0) val r: Float,
    @FloatRange(from = 0.0, to = 255.0) val g: Float,
    @FloatRange(from = 0.0, to = 255.0) val b: Float
) : Parcelable {

    @ColorInt
    fun toColor(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Color.argb(a / 255, r / 255, g / 255, b / 255)
        } else {
            Color.argb(a.toInt() / 255, r.toInt() / 255, g.toInt() / 255, b.toInt() / 255)
        }
    }

    companion object {

        val DEFAULT = Argb(0.08f * 255, 0f, 0f, 0f)
        fun fromAlpha(alpha: Float) = Argb(alpha * 255, 0f, 0f, 0f)
        fun fromRGB(@ColorInt color: Int) = Argb(0f, color.red.toFloat(), color.green.toFloat(), color.blue.toFloat())
    }
}
