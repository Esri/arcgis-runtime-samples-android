/*
 * Copyright 2020 Esri
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

package com.esri.arcgisruntime.sample.displaylayerviewstate

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.LayerViewStatus
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import kotlinx.android.synthetic.main.activity_main.*
import java.util.EnumSet

class MainActivity : AppCompatActivity() {

  private var featureLayer: FeatureLayer? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // authentication with an API key or named user is required to access basemaps and other 
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    mapView.apply {
      // create a map with a topographic basemap
      map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)
      // zoom to custom viewpoint
      setViewpoint(Viewpoint(Point(-11e6, 45e5, SpatialReferences.getWebMercator()), 40_000_000.0))
    }

    mapView.addLayerViewStateChangedListener { layerViewStateChangedEvent ->
      // get the layer which changed its state
      val layer = layerViewStateChangedEvent.layer
      // we only want to check the view state of the image layer
      if (layer != featureLayer) {
        return@addLayerViewStateChangedListener
      }

      val layerViewStatus = layerViewStateChangedEvent.layerViewStatus
      // if there is an error or warning, display it in a toast
      layerViewStateChangedEvent.error?.let { error ->
        val message = error.cause?.toString() ?: error.message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
      }
      displayViewStateText(layerViewStatus)
    }

    loadButton.setOnClickListener {
      if (featureLayer != null) {
        return@setOnClickListener
      }
      // load a feature layer from a portal item
      val portalItem = PortalItem(
        Portal("https://runtime.maps.arcgis.com/"),
        "b8f4033069f141729ffb298b7418b653"
      )
      featureLayer = FeatureLayer(portalItem, 0).apply {
        // set the scales at which this layer can be viewed
        minScale = 400_000_000.0
        maxScale = minScale / 10
      }
      // add the layer on the map to load it
      mapView.map.operationalLayers.add(featureLayer)
      // hide the button
      loadButton.apply {
        isEnabled = false
        visibility = View.GONE
      }
      // show the view state UI and the hide layer button
      statesContainer.visibility = View.VISIBLE
      hideButton.visibility = View.VISIBLE
    }

    hideButton.setOnClickListener {
      featureLayer?.apply {
        // toggle visibility
        isVisible = !isVisible
        // update button text
        hideButton.text = when {
          isVisible -> getString(R.string.show_layer)
          else -> getString(R.string.hide_layer)
        }
      }
    }
  }

  /**
   * Formats and displays the layer view status flags in a text view.
   *
   * @param layerViewStatus to display
   */
  private fun displayViewStateText(layerViewStatus: EnumSet<LayerViewStatus>) {
    // for each view state property that's active,
    // add it to a list and display the states as a comma-separated string
    val stringList = mutableListOf<String>()
    if (layerViewStatus.contains(LayerViewStatus.ACTIVE)) {
      stringList.add(getString(R.string.active_state))
    }
    if (layerViewStatus.contains(LayerViewStatus.ERROR)) {
      stringList.add(getString(R.string.error_state))
    }
    if (layerViewStatus.contains(LayerViewStatus.LOADING)) {
      stringList.add(getString(R.string.loading_state))
    }
    if (layerViewStatus.contains(LayerViewStatus.NOT_VISIBLE)) {
      stringList.add(getString(R.string.not_visible_state))
    }
    if (layerViewStatus.contains(LayerViewStatus.OUT_OF_SCALE)) {
      stringList.add(getString(R.string.out_of_scale_state))
    }
    if (layerViewStatus.contains(LayerViewStatus.WARNING)) {
      stringList.add(getString(R.string.warning_state))
    }
    activeStateTextView.text = stringList.joinToString(", ")
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
