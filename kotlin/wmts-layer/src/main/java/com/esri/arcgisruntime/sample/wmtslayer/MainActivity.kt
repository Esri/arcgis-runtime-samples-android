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

package com.esri.arcgisruntime.sample.wmtslayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.esri.arcgisruntime.layers.WmtsLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.ogc.wmts.WmtsService

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // set a map to be displayed in this view
    mapView.map = ArcGISMap()
    // create wmts service from url string
    val wmtsService = WmtsService(getString(R.string.wmts_url))
    wmtsService.addDoneLoadingListener({
      when {
        wmtsService.loadStatus == LoadStatus.LOADED -> {
          // create WMTS layer from the first layer in layer info
          val wmtsLayer = WmtsLayer(wmtsService.serviceInfo.layerInfos[0])
          // set the basemap of the map with WMTS layer
          mapView.map.basemap = Basemap(wmtsLayer)
        }
      }
    })
    wmtsService.loadAsync()
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
