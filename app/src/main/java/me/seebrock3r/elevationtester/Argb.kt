package me.seebrock3r.elevationtester

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import kotlinx.parcelize.Parcelize

@Parcelize
data class Argb(
    @FloatRange(from = 0.0, to = 1.0) val a: Float,
    @FloatRange(from = 0.0, to = 1.0) val r: Float,
    @FloatRange(from = 0.0, to = 1.0) val g: Float,
    @FloatRange(from = 0.0, to = 1.0) val b: Float
) : Parcelable {

    @ColorInt
    fun toColor() = Color.argb(a, r, g, b)

    companion object {

        val DEFAULT = Argb(0.08f, 0f, 0f, 0f)
        fun fromAlpha(alpha: Float) = Argb(alpha, 0f, 0f, 0f)
    }
}
