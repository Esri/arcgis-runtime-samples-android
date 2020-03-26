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
package com.esri.arcgisruntime.sample.featurelayerdefinitionexpression

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private lateinit var featureLayer: FeatureLayer
  var applyActive: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // set up the bottom toolbar
    createBottomToolbar()

    // create a service feature table from a URL
    val serviceFeatureTable = ServiceFeatureTable(getString(R.string.sample_service_url))
    // create a feature layer using the service feature table
    featureLayer = FeatureLayer(serviceFeatureTable)

    // create a map with a topographic base map
    ArcGISMap(Basemap.createTopographic()).let {
      // add the feature layer to the map
      it.operationalLayers.add(featureLayer)
      // set the map to be displayed in the layout's map view
      mapView.map = it
    }

    // set a viewpoint on the map view to center on San Francisco
    mapView.setViewpointCenterAsync(
      Point(-13630845.0, 4544861.0, SpatialReferences.getWebMercator()), 600000.0
    )
  }

  /**
   * Create and configure bottom tool bar for applying the definition expression to the feature layer.
   */
  private fun createBottomToolbar() {

    bottomToolbar.inflateMenu(R.menu.menu_main)
    // handle action bar item clicks
    bottomToolbar.setOnMenuItemClickListener { item ->
      val itemId = item.itemId
      // if statement is used because this sample is used elsewhere as a Library module
      if (itemId == R.id.action_def_exp) {
        // check the state of the menu item
        if (!applyActive) {
          // apply a definition expression on the feature layer
          // if this is called before the layer is loaded, it will be applied to the loaded layer
          featureLayer.definitionExpression = "req_Type = 'Tree Maintenance or Damage'"
          // change the text to reset
          applyActive = true
          item.setTitle(R.string.action_reset)
        } else {
          // set the definition expression to nothing (empty string, null also works)
          featureLayer.definitionExpression = ""
          // change the text to apply
          applyActive = false
          item.setTitle(R.string.action_def_exp)
        }
      }
      true
    }
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
