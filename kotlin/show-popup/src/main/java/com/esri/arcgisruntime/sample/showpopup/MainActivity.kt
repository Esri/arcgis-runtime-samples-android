/* Copyright 2021 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.showpopup

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.showpopup.databinding.ActivityMainBinding
import com.esri.arcgisruntime.toolkit.popup.PopupViewModel
import com.esri.arcgisruntime.toolkit.util.observeEvent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.simpleName

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mapView: MapView
    private lateinit var progressBar: ProgressBar
    private lateinit var map: ArcGISMap
    private val popupViewModel: PopupViewModel by viewModels()

    /**
     * getter function to retrieve the first available feature layer
     * [featureLayer] updates with every map click
     */
    private val featureLayer: FeatureLayer?
        get() {
            return map.operationalLayers?.filterIsInstance<FeatureLayer>()?.first {
                (it.featureTable?.geometryType == GeometryType.POINT)
                    .and(it.isVisible)
                    .and(it.isPopupEnabled && it.popupDefinition != null)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.lifecycleOwner = this

        val portal = Portal("https://arcgisruntime.maps.arcgis.com/")
        val portalItem = PortalItem(portal, "fb788308ea2e4d8682b9c05ef641f273")
        map = ArcGISMap(portalItem)

        // set up binding and UI behaviour
        mapView = binding.mapView
        mapView.map = map
        progressBar = binding.progressBar
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // reset the IdentifyResult on a sheet close
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    // Clear the selected features from the feature layer
                    resetIdentifyResult()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        popupViewModel.dismissPopupEvent.observeEvent(this) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            // Clear the selected features from the feature layer
            resetIdentifyResult()
        }

        // set the progressBar visibility
        progressBar.visibility = View.GONE

        mapView.onTouchListener =
            object : DefaultMapViewOnTouchListener(this, mapView) {
                override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                    // set the progressBar visibility
                    progressBar.visibility = View.VISIBLE
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    val screenPoint = android.graphics.Point(
                        event.x.roundToInt(),
                        event.y.roundToInt()
                    )
                    // setup identifiable layer at the given screen point.
                    identifyLayer(screenPoint)
                    return true
                }
            }
    }

    /**
     * Performs an identify on the feature layer at the given screen point.
     * [screenPoint] in Android graphic coordinates.
     */
    private fun identifyLayer(screenPoint: android.graphics.Point) {

        featureLayer?.let {
            // clear the selected features from the feature layer
            resetIdentifyResult()

            val identifyLayerResultsFuture = mapView
                .identifyLayerAsync(featureLayer, screenPoint, 12.0, true)

            identifyLayerResultsFuture.addDoneListener {
                try {
                    val identifyLayerResult = identifyLayerResultsFuture.get()

                    if (identifyLayerResult.popups.isNotEmpty()) {
                        popupViewModel.setPopup(identifyLayerResult.popups.first())
                        val featureLayer: FeatureLayer? =
                            identifyLayerResult.layerContent as? FeatureLayer
                        featureLayer?.selectFeature(identifyLayerResult.popups.first().geoElement as Feature)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                    }
                } catch (e: Exception) {
                    val error = "Error identifying results ${e.message}"
                    Log.e(TAG, error)
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }

                // set the progressBar visibility
                progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * Resets the Identify Result.
     */
    private fun resetIdentifyResult() {
        featureLayer?.clearSelection()
        popupViewModel.clearPopup()
    }

    override fun onPause() {
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }
}
