package com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers.databinding.DialogFragmentBinding

class GardenDescriptionFragment(gardenSection: GardenSection) : DialogFragment() {

    //
    private val mGardenSection = gardenSection

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dialogFragmentBinding = DialogFragmentBinding.inflate(inflater)

        dialogFragmentBinding.apply {
            gardenContentTitle.text = mGardenSection.title
            gardenContentTextView.text =
                HtmlCompat.fromHtml(mGardenSection.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
            val bitmap = BitmapFactory.decodeFile(mGardenSection.imageURI)
            gardenContentImageView.setImageBitmap(bitmap)
        }

        return dialogFragmentBinding.root
    }

    override fun onStart() {
        super.onStart()
        //Set the width of the Dialog Fragment
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}