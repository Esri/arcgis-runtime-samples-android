/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.displayannotation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.AnnotationLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with a topographic basemap
    mapView.map = ArcGISMap(Basemap.Type.LIGHT_GRAY_CANVAS, 55.882436, -2.725610, 13).apply {
      // add a feature layer from a feature service
      operationalLayers.add(
        FeatureLayer(ServiceFeatureTable(getString(R.string.river_feature_service_url)))
      )
      // add an annotation layer from a feature service
      operationalLayers.add(
        AnnotationLayer(getString(R.string.river_annotation_feature_service_url))
      )
    }
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
