/* Copyright 2020 Esri
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
package com.esri.arcgisruntime.sample.rasterlayerfile

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.raster.Raster
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // create a map with imagery basemap
    val map = ArcGISMap(Basemap.createImagery())
    // add the map to a map view
    mapView.map = map
    // create a raster from a local raster filepath
    val raster = Raster(getExternalFilesDir(null)?.path + getString(R.string.raster_file_path))
    // create a raster layer
    val rasterLayer = RasterLayer(raster)
    // add the raster as an operational layer
    map.operationalLayers.add(rasterLayer)
    // set viewpoint on the raster
    rasterLayer.addDoneLoadingListener {
      if (rasterLayer.loadStatus == LoadStatus.LOADED) {
        mapView.setViewpointGeometryAsync(
          rasterLayer.fullExtent,
          50.0
        )
      } else {
        Log.e(TAG, "Error loading raster layer: ${rasterLayer.loadError.message}")
        Toast.makeText(this, "Failed to load raster file from storage", Toast.LENGTH_LONG).show()
      }
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