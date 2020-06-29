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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.LayerViewStatus
import kotlinx.android.synthetic.main.activity_main.*
import java.util.EnumSet


class MainActivity : AppCompatActivity() {

  private var imageLayer: ArcGISMapImageLayer? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with the BasemapType topographic
    val map = ArcGISMap(Basemap.createTopographic())
    // set the map to be displayed in this view
    mapView.map = map

    // zoom to custom ViewPoint
    mapView.setViewpoint(
      Viewpoint(
        Point(-11e6, 45e5, SpatialReferences.getWebMercator()),
        40_000_000.0
      )
    )

    mapView.addLayerViewStateChangedListener { layerViewStateChangedEvent ->
      // get the layer which changed its state
      val layer = layerViewStateChangedEvent.layer
      // we only want to check the view state of the image layer
      if (layer != imageLayer) { return@addLayerViewStateChangedListener }

      val layerViewStatus = layerViewStateChangedEvent.layerViewStatus

      populateViewStateStrings(layerViewStatus)
    }

    button.setOnClickListener {
      if (imageLayer != null) {return@setOnClickListener}
      imageLayer =
        ArcGISMapImageLayer("https://sampleserver6.arcgisonline.com/arcgis/rest/services/Census/MapServer").apply {
          // setting the scales at which this layer can be viewed
          minScale = 40_000_000.0
          maxScale = minScale / 10
        }
      // add the layer on the map to load it
      map.operationalLayers.add(imageLayer)
      button.isEnabled = false
      button.visibility = View.GONE
    }
  }

  private fun populateViewStateStrings(layerViewStatus: EnumSet<LayerViewStatus>) {
    activeStateTextView.text = getString(R.string.activeStateTextViewString, layerViewStatus.contains(LayerViewStatus.ACTIVE).toString())
    errorStateTextView.text = getString(R.string.errorStateTextViewString, layerViewStatus.contains(LayerViewStatus.ERROR).toString())
    loadingStateTextView.text = getString(R.string.loadingStateTextViewString, layerViewStatus.contains(LayerViewStatus.LOADING).toString())
    notVisibleStateTextView.text = getString(R.string.notVisibleStateTextViewString, layerViewStatus.contains(LayerViewStatus.NOT_VISIBLE).toString())
    outOfScaleStateTextView.text = getString(R.string.outOfScaleStateTextViewString, layerViewStatus.contains(LayerViewStatus.OUT_OF_SCALE).toString())
    warningStateTextView.text = getString(R.string.warningStateTextViewString, layerViewStatus.contains(LayerViewStatus.WARNING).toString())
  }
}
