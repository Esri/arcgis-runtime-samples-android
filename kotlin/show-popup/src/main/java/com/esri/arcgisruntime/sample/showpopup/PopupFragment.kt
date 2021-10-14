package com.esri.arcgisruntime.sample.showpopup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.esri.arcgisruntime.sample.showpopup.databinding.FragmentPopupBinding
import com.esri.arcgisruntime.toolkit.popup.PopupViewModel

/**
 * Fragment for displaying the PopupView using the [popupViewModel].
 */
class PopupFragment : Fragment() {

    private val popupViewModel: PopupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPopupBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_popup,
            container,
            false
        )

        // sets up databinding for the view model
        binding.popupViewModel = popupViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

}
