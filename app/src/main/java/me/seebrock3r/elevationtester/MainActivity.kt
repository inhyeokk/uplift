package me.seebrock3r.elevationtester

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import android.widget.SeekBar
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import androidx.transition.TransitionManager
import it.sephiroth.android.library.xtooltip.ClosePolicy
import it.sephiroth.android.library.xtooltip.Tooltip
import me.seebrock3r.elevationtester.databinding.ActivityMainBinding
import me.seebrock3r.elevationtester.databinding.IncludeHeaderBinding
import me.seebrock3r.elevationtester.databinding.IncludePanelControlsBinding
import me.seebrock3r.elevationtester.widget.BetterSeekListener
import me.seebrock3r.elevationtester.widget.ColorView
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private const val REQUEST_AMBIENT_COLOR = 7367
private const val REQUEST_SPOT_COLOR = 7368

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var headerBinding: IncludeHeaderBinding
    private lateinit var panelControlsBinding: IncludePanelControlsBinding
    private lateinit var outlineProvider: TweakableOutlineProvider

    @Px
    private var buttonVerticalMarginPixel = 0

    private val hitRect = Rect()
    private var panelExpanded = false

    private var dragYOffset = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        headerBinding = IncludeHeaderBinding.bind(binding.root)
        panelControlsBinding = IncludePanelControlsBinding.bind(binding.root)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val cornerRadius = resources.getDimensionPixelSize(R.dimen.control_corner_material).toFloat()
        outlineProvider = TweakableOutlineProvider(cornerRadius = cornerRadius, scaleX = 1f, scaleY = 1f, yShift = 0)
        binding.mainButton.outlineProvider = outlineProvider

        setupPanelHeaderControls()
        setupElevationControls()
        setupScaleXYControls()
        setupYShiftControls()
        setupColorPickersOnAndroidPAndLater()

        setupDragYToMove()

        panelCollapsed()

        val initialButtonElevationDp = resources.getDimensionDpSize(R.dimen.main_button_initial_elevation).roundToInt()
        panelControlsBinding.elevationBar.progress = initialButtonElevationDp
    }

    private fun setupPanelHeaderControls() {
        binding.rootContainer.setTransitionListener(object : MotionLayout.TransitionListener {

            override fun onTransitionTrigger(view: MotionLayout, triggerId: Int, positive: Boolean, progress: Float) {
                // No-op
            }

            override fun onTransitionStarted(view: MotionLayout, startId: Int, endId: Int) {
                // No-op
            }

            override fun onTransitionChange(view: MotionLayout, startState: Int, endState: Int, progress: Float) {
                // No-op
            }

            override fun onTransitionCompleted(view: MotionLayout, state: Int) {
                panelExpanded = state == R.id.constraints_main_expanded
                TransitionManager.beginDelayedTransition(view)
                if (panelExpanded) panelExpanded() else panelCollapsed()
            }
        })
    }

    private fun panelCollapsed() {
        headerBinding.expandCollapseImage.isChecked = false
        binding.mainButton.text = getString(R.string.drag_up_and_down)
    }

    private fun panelExpanded() {
        headerBinding.expandCollapseImage.isChecked = true
        binding.mainButton.text = getString(R.string.use_controls_below)
    }

    private fun setupElevationControls() {
        panelControlsBinding.elevationBar.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    setElevationDp(progress)
                }
            }
        )
        panelControlsBinding.elevationValue.text = getString(R.string.elevation_value, 0)
    }

    private fun setElevationDp(elevationDp: Int) {
        val elevationPixel = elevationDp * resources.displayMetrics.density
        binding.mainButton.elevation = elevationPixel
        panelControlsBinding.elevationValue.text = getString(R.string.elevation_value, elevationDp)
    }

    private fun setupScaleXYControls() {
        panelControlsBinding.xScaleBar.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    setScaleX(progress)
                }
            }
        )

        panelControlsBinding.yScaleBar.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    setScaleY(progress)
                }
            }
        )

        setScaleX(0)
        panelControlsBinding.xScaleValue.setOnClickListener { panelControlsBinding.xScaleBar.progress = panelControlsBinding.xScaleBar.max / 2 }
        panelControlsBinding.yScaleValue.text = getString(R.string.y_scale_value, 0)
        panelControlsBinding.yScaleBar.progress = panelControlsBinding.yScaleBar.max / 2
        panelControlsBinding.xScaleBar.progress = panelControlsBinding.xScaleBar.max / 2
    }

    private fun setScaleX(scaleXPercent: Int) {
        val scale = scaleXPercent - panelControlsBinding.xScaleBar.max / 2
        outlineProvider.scaleX = 1 + scale / 100f
        binding.mainButton.invalidateOutline()
        panelControlsBinding.xScaleValue.text = getString(R.string.x_scale_value, scale + 100)
    }

    private fun setScaleY(scaleYPercent: Int) {
        val scale = scaleYPercent - panelControlsBinding.yScaleBar.max / 2
        outlineProvider.scaleY = 1 + scale / 100f
        binding.mainButton.invalidateOutline()
        panelControlsBinding.yScaleValue.text = getString(R.string.y_scale_value, scale + 100)
    }

    private fun setupYShiftControls() {
        panelControlsBinding.yShiftBar.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    setShiftY(progress)
                }
            }
        )
        panelControlsBinding.yShiftValue.text = getString(R.string.y_shift_value, 0)
        panelControlsBinding.yShiftBar.progress = panelControlsBinding.yShiftBar.max / 2
    }

    private fun setShiftY(shiftYDp: Int) {
        val adjustedShiftYDp = shiftYDp - panelControlsBinding.yShiftBar.max / 2
        val adjustedShiftYPixel = adjustedShiftYDp * resources.displayMetrics.density
        outlineProvider.yShift = adjustedShiftYPixel.roundToInt()
        binding.mainButton.invalidateOutline()
        panelControlsBinding.yShiftValue.text = getString(R.string.y_shift_value, adjustedShiftYDp)
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun setupColorPickersOnAndroidPAndLater() {
        if (isAndroidPOrLater) {
            panelControlsBinding.ambientColor.setOnClickListener { onColorPickerClicked(panelControlsBinding.ambientColor) }
            panelControlsBinding.spotColor.setOnClickListener { onColorPickerClicked(panelControlsBinding.spotColor) }
            panelControlsBinding.ambientColor.onColorChangedListener = ::onColorChanged
            panelControlsBinding.spotColor.onColorChangedListener = ::onColorChanged

            panelControlsBinding.ambientColor.color = Color.BLACK.setAlphaTo((0.039f * 255).toInt())
            panelControlsBinding.spotColor.color = Color.BLACK.setAlphaTo((0.19f * 255).toInt())
        } else {
            panelControlsBinding.ambientColor.isEnabled = false
            panelControlsBinding.spotColor.isEnabled = false
        }

        panelControlsBinding.infoButton.setOnClickListener { showInfoDialog() }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun onColorPickerClicked(colorView: ColorView) {
        val title = getString(
            if (colorView.id == R.id.ambientColor) R.string.color_picker_title_ambient else R.string.color_picker_title_spot
        )
        val boundsOnScreen = colorView.boundsOnScreen()
        val intent = ColorPickerActivity.createIntent(this, title, colorView.color, boundsOnScreen)

        val requestCode = if (colorView.id == R.id.ambientColor) REQUEST_AMBIENT_COLOR else REQUEST_SPOT_COLOR

        startActivityForResult(intent, requestCode)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun onColorChanged(colorView: ColorView) {
        when (colorView.id) {
            R.id.ambientColor -> binding.mainButton.outlineAmbientShadowColor = colorView.color
            R.id.spotColor -> binding.mainButton.outlineSpotShadowColor = colorView.color
        }
    }

    private fun showInfoDialog() {
        ElevationTintingInfoBottomSheet().show(supportFragmentManager, "elevation-info")
    }

    @SuppressLint("ClickableViewAccessibility") // Shut up Lint, I need a draggy thing
    private fun setupDragYToMove() {
        buttonVerticalMarginPixel = resources.getDimensionPixelSize(R.dimen.main_button_vertical_margin)

        binding.buttonContainer.setOnTouchListener { _, motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> handleDragStart(motionEvent)
                MotionEvent.ACTION_MOVE -> handleDrag(motionEvent)
                MotionEvent.ACTION_UP -> handleDragEnd()
                else -> false
            }
        }
    }

    private fun handleDragStart(motionEvent: MotionEvent): Boolean {
        if (panelExpanded) {
            return false // Only draggable when the panel is collapsed
        }

        binding.mainButton.getHitRect(hitRect)
        dragYOffset = (binding.mainButton.y + binding.mainButton.height / 2F) - motionEvent.y
        val dragOnButton = hitRect.contains(motionEvent.getX(0).roundToInt(), motionEvent.getY(0).roundToInt())

        if (dragOnButton) {
            binding.mainButton.animate()
                .scaleX(1.04F)
                .scaleY(1.04F)
                .duration = resources.getInteger(R.integer.animation_duration_drag_start).toLong()
        }
        return dragOnButton
    }

    private fun handleDrag(motionEvent: MotionEvent): Boolean {
        val minY = binding.buttonContainer.paddingTop.toFloat() + binding.mainButton.height / 2F
        val maxY = binding.buttonContainer.height - binding.buttonContainer.paddingBottom - binding.mainButton.height / 2F
        val availableHeight = maxY - minY

        val coercedY = (motionEvent.y + dragYOffset).coerceIn(minY, maxY)
        val newBias = (coercedY - minY) / availableHeight

        binding.mainButton.layoutParams = (binding.mainButton.layoutParams as ConstraintLayout.LayoutParams)
            .apply { verticalBias = newBias }

        return true
    }

    private fun handleDragEnd(): Boolean {
        binding.mainButton.animate()
            .scaleX(1F)
            .scaleY(1F)
            .duration = resources.getInteger(R.integer.animation_duration_drag_end).toLong()

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data)
        }

        when (requestCode) {
            REQUEST_AMBIENT_COLOR -> ColorPickerActivity.extractResultFrom(data)?.let { selectedColor ->
                panelControlsBinding.ambientColor.color = selectedColor
            }
            REQUEST_SPOT_COLOR -> ColorPickerActivity.extractResultFrom(data)?.let { selectedColor ->
                panelControlsBinding.spotColor.color = selectedColor
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.menu_reset) {
            return super.onOptionsItemSelected(item)
        }

        panelControlsBinding.elevationBar.progress = 8
        panelControlsBinding.xScaleBar.progress = panelControlsBinding.xScaleBar.max / 2
        panelControlsBinding.yScaleBar.progress = panelControlsBinding.yScaleBar.max / 2
        panelControlsBinding.yShiftBar.progress = panelControlsBinding.yShiftBar.max / 2
        panelControlsBinding.ambientColor.color = Color.BLACK.setAlphaTo((0.039f * 255).toInt())
        panelControlsBinding.spotColor.color = Color.BLACK.setAlphaTo((0.19f * 255).toInt())
        return true
    }

    override fun onStart() {
        super.onStart()

        val onboardingPreferences = OnboardingPreferences(this)
        if (onboardingPreferences.shouldShowOnboarding) {
            showOnboarding()
            onboardingPreferences.storeOnboardingShown()
        }
    }

    private fun showOnboarding() {
        val bounceDeltaY = resources.getDimensionPixelSize(R.dimen.onboarding_icon_nudge_y_delta).toFloat()
        val peekMotionProgress = resources.getFloatValue(R.dimen.onboarding_icon_peek_motion_progress)

        val animationDuration = resources.getInteger(R.integer.onboarding_anim_entry_duration).toLong()
        val delayDuration = resources.getInteger(R.integer.onboarding_anim_delay_duration).toLong()

        binding.rootContainer.isEnabled = false

        AnimatorSet().apply {
            val caretBounce = ObjectAnimator.ofFloat(headerBinding.expandCollapseImage, "translationY", 0F, -bounceDeltaY).apply {
                interpolator = MyBounceInterpolator()
                duration = animationDuration
            }

            val panelPeek = ObjectAnimator.ofFloat(binding.rootContainer, "interpolatedProgress", 0F, peekMotionProgress).apply {
                interpolator = MyBounceInterpolator()
                duration = animationDuration
                startDelay = delayDuration
            }

            play(caretBounce)
                .after(delayDuration)
                .with(panelPeek)

            doOnEnd {
                binding.rootContainer.isEnabled = true
                Tooltip.Builder(this@MainActivity)
                    .anchor(headerBinding.panelHeader, yoff = -headerBinding.panelHeader.height / 2)
                    .text(R.string.onboarding_tooltip)
                    .typeface(ResourcesCompat.getFont(this@MainActivity, R.font.arvo))
                    .arrow(true)
                    .closePolicy(ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME)
                    .create()
                    .show(headerBinding.panelHeader, Tooltip.Gravity.TOP, fitToScreen = true)
            }

            start()
        }
    }
}

private fun Resources.getFloatValue(@DimenRes resourceId: Int): Float {
    val outValue = TypedValue()
    getValue(resourceId, outValue, true)
    return outValue.float
}

class MyBounceInterpolator(private val frequency: Double = PI) : Interpolator {

    override fun getInterpolation(time: Float): Float {
        return (sin(frequency * time)).toFloat()
    }
}

private fun View.boundsOnScreen(): Rect {
    val locationOnScreen = IntArray(2)
    getLocationOnScreen(locationOnScreen)

    return Rect(
        locationOnScreen[0],
        locationOnScreen[1],
        locationOnScreen[0] + width,
        locationOnScreen[1] + height
    )
}

private fun Resources.getDimensionDpSize(@DimenRes dimensionResId: Int): Float =
    getDimensionPixelSize(dimensionResId) / displayMetrics.density
