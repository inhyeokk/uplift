package me.seebrock3r.elevationtester.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import me.seebrock3r.elevationtester.Argb
import me.seebrock3r.elevationtester.R
import me.seebrock3r.elevationtester.databinding.ViewColorBinding
import kotlin.properties.Delegates

class ColorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.colorViewStyle
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewColorBinding.inflate(LayoutInflater.from(context), this)

    var text: String? = null
        set(newText) {
            field = newText
            binding.colorViewLabel.text = text
        }

    var argb: Argb by Delegates.observable(Argb.DEFAULT) { _, _, _ ->
        onColorChanged()
    }

    @get:ColorInt
    val color: Int
        get() = argb.toColor()

    var onColorChangedListener: ((view: ColorView) -> Unit)? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.ColorView, defStyleAttr) {
            if (hasValue(R.styleable.ColorView_android_text)) text = getString(R.styleable.ColorView_android_text)
//            if (hasValue(R.styleable.ColorView_color)) color = getColor(R.styleable.ColorView_color, color)
        }

        isClickable = true
        isFocusable = true
        onColorChanged()
        binding.colorViewLabel.text = text
    }

    @SuppressLint("SetTextI18n") // This doesn't require i18n, it's a hex integer representation
    private fun onColorChanged() {
        binding.colorViewColor.backgroundTintList = ColorStateList.valueOf(color)
        binding.colorViewValue.text = String.format("#%08X", 0xFFFFFFFF and color.toLong())

        onColorChangedListener?.invoke(this)
    }
}
