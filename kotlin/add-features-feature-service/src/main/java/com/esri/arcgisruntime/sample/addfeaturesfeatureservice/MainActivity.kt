/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.addfeaturesfeatureservice

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  private lateinit var featureTable: ServiceFeatureTable

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with streets basemap
    with(ArcGISMap(Basemap.Type.STREETS, 40.0, -95.0, 4)) {
      // create service feature table from URL
      featureTable = ServiceFeatureTable(getString(R.string.service_layer_url))

      // create a feature layer from table
      val featureLayer = FeatureLayer(featureTable)

      // add the layer to the ArcGISMap
      this.operationalLayers.add(featureLayer)

      // set ArcGISMap to be displayed in map view
      mapView.map = this
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