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
package com.esri.arcgisruntime.sample.featurelayergeodatabase

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
  private val TAG =
    MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // create map and add to map view
    val map = ArcGISMap(Basemap.createStreets())
    mapView.map = map

    val path: String = getExternalFilesDir(null)?.path + getString(R.string.geodb_name)
    val geodatabase = Geodatabase(path)
    geodatabase.loadAsync()
    geodatabase.addDoneLoadingListener {
      if (geodatabase.loadStatus == LoadStatus.LOADED) {
        // access the geodatabase's feature table Trailheads
        val geodatabaseFeatureTable = geodatabase.getGeodatabaseFeatureTable("Trailheads")
        geodatabaseFeatureTable.loadAsync()
        // create a layer from the geodatabase feature table and add to map
        val featureLayer = FeatureLayer(geodatabaseFeatureTable)
        featureLayer.addDoneLoadingListener {
          if (featureLayer.loadStatus == LoadStatus.LOADED) {
            // set viewpoint to the feature layer's extent
            mapView.setViewpointAsync(Viewpoint(featureLayer.fullExtent))
          } else {
            Toast.makeText(this, "Feature Layer failed to load!", Toast.LENGTH_LONG)
              .show()
            Log.e(TAG, "Feature Layer failed to load!")
          }
        }
        // add feature layer to the map
        map.operationalLayers.add(featureLayer)
      } else {
        Toast.makeText(this, "Geodatabase failed to load!", Toast.LENGTH_LONG).show()
        Log.e(TAG, "Geodatabase failed to load!" + geodatabase.loadError)
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