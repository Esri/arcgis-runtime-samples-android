/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.mapimagelayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.mapping.ArcGISMap

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    mapView.apply {
      map = ArcGISMap().apply {
        // create a MapImageLayer with dynamically generated map images and add it to the map's operational layers
        operationalLayers.add(ArcGISMapImageLayer(resources.getString(R.string.world_elevation_service)))
      }
    }
  }

  override fun onPause() {
    super.onPause()
    mapView.pause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.dispose()
  }
}
