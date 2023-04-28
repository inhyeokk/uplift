package me.seebrock3r.elevationtester

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.alpha
import me.seebrock3r.elevationtester.databinding.ActivityColorPickerBinding
import me.seebrock3r.elevationtester.widget.BetterSeekListener

class ColorPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityColorPickerBinding
    private val selectedColor: Int
        get() = binding.dialogColorWheel.selectedColor.setAlphaTo(binding.dialogColorAlpha.progress)

    private val initialColor: Int
        get() = intent.getIntExtra(EXTRA_COLOR, Color.BLACK)

    private var changingBrightnessFromCode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColorPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setResult(Activity.RESULT_CANCELED)

        DialogLayoutParameters.wrapHeight(this)
            .applyTo(window)

        binding.dialogTitle.text = intent.getStringExtra(EXTRA_TITLE)

        val color = initialColor
        setupAlphaControls(color)
        setupBrightnessControls(color)
        setupColorWheel()

        val cornerRadius = resources.getDimensionPixelSize(R.dimen.control_corner_material).toFloat()
        binding.dialogColorPreviewCheckerboard.outlineProvider = TweakableOutlineProvider(cornerRadius = cornerRadius)
        binding.dialogColorPreviewCheckerboard.clipToOutline = true

        binding.dialogColorWheel.setColor(color)

        binding.dialogClose.setOnClickListener { finish() }
    }

    private fun setupAlphaControls(color: Int) {
        binding.dialogColorAlpha.progress = color.alpha
        binding.dialogAlphaValue.text = formatAsTwoPlacesDecimal(color.alpha.toFloat() / 255F)

        binding.dialogColorAlpha.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val newSelectedColor = binding.dialogColorWheel.selectedColor.setAlphaTo(progress)
                    binding.dialogColorPreview.backgroundTintList = ColorStateList.valueOf(newSelectedColor)
                    val alpha = progress / binding.dialogColorAlpha.max.toFloat()
                    binding.dialogAlphaValue.text = formatAsTwoPlacesDecimal(alpha)
                    binding.dialogColorWheel.onColorChangedListener?.invoke(newSelectedColor)
                }
            }
        )
    }

    private fun setupBrightnessControls(color: Int) {
        binding.dialogBrightnessValue.text = formatAsTwoPlacesDecimal(color.brightness)

        binding.dialogColorBrightness.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (changingBrightnessFromCode) {
                        return
                    }

                    val brightness = progress / binding.dialogColorBrightness.max.toFloat()
                    binding.dialogColorWheel.setBrightness(brightness)
                    binding.dialogBrightnessValue.text = formatAsTwoPlacesDecimal(brightness)
                }
            }
        )
    }

    private fun formatAsTwoPlacesDecimal(@FloatRange value: Float) = "%.2f".format(value)

    private fun setupColorWheel() {
        binding.dialogColorWheel.onColorChangedListener = { _ ->
            changingBrightnessFromCode = true
            binding.dialogColorBrightness.progress = (selectedColor.brightness * binding.dialogColorBrightness.max).toInt()
            changingBrightnessFromCode = false

            binding.dialogColorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)

            setResult(Activity.RESULT_OK, Intent().apply { putExtra(EXTRA_COLOR, selectedColor) })
        }
    }

    companion object {

        private const val EXTRA_TITLE = "ColorPickerActivity_title"
        private const val EXTRA_COLOR = "ColorPickerActivity_color"
        private const val EXTRA_ORIGIN_BOUNDS = "ColorPickerActivity_origin_bounds"

        fun createIntent(context: Context, title: String, @ColorInt color: Int, originBounds: Rect) =
            Intent(context, ColorPickerActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_COLOR, color)
                putExtra(EXTRA_ORIGIN_BOUNDS, originBounds)
            }

        @ColorInt
        fun extractResultFrom(resultData: Intent?) =
            resultData?.getIntExtra(EXTRA_COLOR, Color.BLACK)
    }
}
