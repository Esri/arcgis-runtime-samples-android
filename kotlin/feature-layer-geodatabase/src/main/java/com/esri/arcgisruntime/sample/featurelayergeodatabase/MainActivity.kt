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
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.featurelayergeodatabase.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private val TAG =
    MainActivity::class.java.simpleName

  private val activityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  private val mapView: MapView by lazy {
    activityMainBinding.mapView
  }

  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private val geodatabase: Geodatabase by lazy {
    Geodatabase(getExternalFilesDir(null)?.path + getString(R.string.geodb_name))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activityMainBinding.root)

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create map and add to map view
    val map = ArcGISMap(BasemapStyle.ARCGIS_STREETS)
    mapView.map = map

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
