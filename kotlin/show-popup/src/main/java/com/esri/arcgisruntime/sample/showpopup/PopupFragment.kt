package com.esri.arcgisruntime.sample.showpopup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.popup.Popup
import com.esri.arcgisruntime.mapping.popup.PopupManager
import com.esri.arcgisruntime.sample.showpopup.databinding.FragmentPopupBinding
import kotlinx.android.synthetic.main.fragment_popup.view.*

class PopupFragment(
    private val mainActivity: MainActivity,
    private val popup: Popup,
    private val featureLayer: FeatureLayer
) : DialogFragment() {

    private lateinit var binding: FragmentPopupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {x
        // Bind inflater to the layout view.
        //return inflater.inflate(R.layout.fragment_popup, container, false)
        binding = FragmentPopupBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.apply {
            popupView.popupManager = PopupManager(mainActivity, popup)
            val queryParameters = QueryParameters()
            queryParameters.geometry = Envelope(popup.geoElement.geometry as Point?, 6.0,6.0)
            featureLayer.selectFeaturesAsync(queryParameters,FeatureLayer.SelectionMode.NEW)
        }
    }

    override fun onStart() {
        super.onStart()
        //Set the width of the Dialog Fragment
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}