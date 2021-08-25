package com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_fragment.*

/**
 * Class to display a dialog with the title, image and description of the [GardenSection]
 */
class GardenDescriptionFragment(
    // Garden section to display
    private val gardenSection: GardenSection,
    private val mainActivity: MainActivity
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Bind inflater to the layout view.
        return inflater.inflate(R.layout.dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set title, description and image view of the mGardenSection
        view.apply {
            gardenContentTitle.text = gardenSection.title
            gardenContentTextView.text =
                HtmlCompat.fromHtml(gardenSection.description, HtmlCompat.FROM_HTML_MODE_LEGACY)

            // Retrieves the image using a callback from [MainActivity]
            mainActivity.retrieveImage(gardenSection) {
                val bitmap = BitmapFactory.decodeFile(it)
                gardenContentImageView.setImageBitmap(bitmap)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //Set the width of the Dialog Fragment
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}