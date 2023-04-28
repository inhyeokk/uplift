package me.seebrock3r.elevationtester

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.TransitionManager
import me.seebrock3r.elevationtester.databinding.ActivityMainBinding
import me.seebrock3r.elevationtester.databinding.IncludeHeaderBinding
import me.seebrock3r.elevationtester.databinding.IncludePanelControlsBinding
import me.seebrock3r.elevationtester.widget.BetterSeekListener
import me.seebrock3r.elevationtester.widget.ColorView
import kotlin.math.roundToInt

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
    private var panelExpanded = true

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
        setupColorPickersOnAndroidPAndLater()

        setupDragYToMove()

        panelExpanded()

        setupInitialValue(ServiceType.A)
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
        binding.mainText.text = getString(R.string.drag_up_and_down)
    }

    private fun panelExpanded() {
        headerBinding.expandCollapseImage.isChecked = true
        binding.mainText.text = getString(R.string.use_controls_below)
    }

    private fun setupElevationControls() {
        panelControlsBinding.elevationBar.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    setElevationDp(progress)
                }
            }
        )
    }

    private fun setElevationDp(elevationDp: Int) {
        val elevationPixel = elevationDp * resources.displayMetrics.density
        binding.mainButton.cardElevation = elevationPixel
        panelControlsBinding.elevationValue.text = getString(R.string.elevation_value, elevationDp)
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun setupColorPickersOnAndroidPAndLater() {
        if (isAndroidPOrLater) {
            panelControlsBinding.ambientColor.setOnClickListener { onColorPickerClicked(panelControlsBinding.ambientColor) }
            panelControlsBinding.spotColor.setOnClickListener { onColorPickerClicked(panelControlsBinding.spotColor) }
            panelControlsBinding.ambientColor.onColorChangedListener = ::onColorChanged
            panelControlsBinding.spotColor.onColorChangedListener = ::onColorChanged
        } else {
            panelControlsBinding.ambientColor.isEnabled = false
            panelControlsBinding.spotColor.isEnabled = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun onColorPickerClicked(colorView: ColorView) {
        val title = getString(
            if (colorView.id == R.id.ambientColor) R.string.color_picker_title_ambient else R.string.color_picker_title_spot
        )
        val boundsOnScreen = colorView.boundsOnScreen()
        val intent = ColorPickerActivity.createIntent(this, title, colorView.argb, boundsOnScreen)

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

    private fun setupInitialValue(serviceType: ServiceType) {
        setElevationDp(serviceType.elevation)
        panelControlsBinding.elevationBar.progress = serviceType.elevation
        panelControlsBinding.ambientColor.argb = Argb.fromAlpha(serviceType.ambient)
        panelControlsBinding.spotColor.argb = Argb.fromAlpha(serviceType.spot)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data)
        }

        when (requestCode) {
            REQUEST_AMBIENT_COLOR -> panelControlsBinding.ambientColor.argb = ColorPickerActivity.extractResultFrom(data)
            REQUEST_SPOT_COLOR -> panelControlsBinding.spotColor.argb = ColorPickerActivity.extractResultFrom(data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_a -> {
                setupInitialValue(ServiceType.A)
                true
            }
            R.id.menu_b -> {
                setupInitialValue(ServiceType.B)
                true
            }
            R.id.menu_c -> {
                setupInitialValue(ServiceType.C)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
