package me.seebrock3r.elevationtester

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.parseAsHtml
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.seebrock3r.elevationtester.databinding.DialogElevationTintingInfoBinding
import kotlin.coroutines.CoroutineContext

class ElevationTintingInfoBottomSheet : BottomSheetDialogFragment(), CoroutineScope {

    private var _binding: DialogElevationTintingInfoBinding? = null
    private val binding get() = _binding!!
    private var job: Job? = null

    override val coroutineContext: CoroutineContext
        get() = job!! + Dispatchers.Main

    override fun getTheme() = R.style.Theme_Uplift_BottomSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.Theme_Uplift)
        _binding = DialogElevationTintingInfoBinding.inflate(inflater.cloneInContext(contextThemeWrapper), container, false)
        binding.closeButton.setOnClickListener { dismiss() }
        binding.learnMoreButton.setOnClickListener { openLearnMore() }
        return binding.root
    }

    private fun openLearnMore() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tips.seebrock3r.me/playing-with-elevation-in-android-part-1-36b901287249"))
        startActivity(intent)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()

        val blurbHtml = getString(R.string.elevation_tinting_blurb)
        val codeTypeface = ResourcesCompat.getFont(requireContext(), R.font.overpass_mono)!!
        val codeColor = ResourcesCompat.getColor(resources, R.color.colorAccent, requireContext().theme)
        launch {
            val parsedHtml = blurbHtml.parseHtml(codeTypeface, codeColor)
            launch(Dispatchers.Main) {
                binding.tintingInfoBlurb.text = parsedHtml
            }
        }
    }

    private suspend fun String.parseHtml(codeTypeface: Typeface, @ColorInt codeColor: Int): CharSequence =
        withContext(Dispatchers.Default) {
            // Using KTX's String.parseAsHtml
            parseAsHtml(tagHandler = CodeSpanHandler(codeTypeface, codeColor))
        }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        job = null
    }
}
