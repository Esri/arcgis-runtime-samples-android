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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
  // permission to read external storage
  private val reqPermission =
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // create map and add to map view
    mapView.map = ArcGISMap(Basemap.createStreets())
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(
        this@MainActivity,
        reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      loadGeodatabase()
    } else { // request permission
      val requestCode = 2
      ActivityCompat.requestPermissions(this@MainActivity, reqPermission, requestCode)
    }
  }

  /**
   * Load a local geodatabase file and add it to the map
   */
  private fun loadGeodatabase() { // create path to local geodatabase
    val path: String =
      (getExternalFilesDir(null)?.path + getString(R.string.geodb_name))
    // create a new geodatabase from local path
    val geodatabase = Geodatabase(path)
    // load the geodatabase
    geodatabase.loadAsync()
    // create feature layer from geodatabase and add to the map
    geodatabase.addDoneLoadingListener {
      if (geodatabase.loadStatus == LoadStatus.LOADED) { // access the geodatabase's feature table Trailheads
        val geodatabaseFeatureTable =
          geodatabase.getGeodatabaseFeatureTable("Trailheads")
        geodatabaseFeatureTable.loadAsync()
        // create a layer from the geodatabase feature table and add to map
        val featureLayer = FeatureLayer(geodatabaseFeatureTable)
        featureLayer.addDoneLoadingListener {
          if (featureLayer.loadStatus == LoadStatus.LOADED) { // set viewpoint to the feature layer's extent
            mapView.setViewpointAsync(Viewpoint(featureLayer.fullExtent))
          } else {
            Toast.makeText(this@MainActivity, "Feature Layer failed to load!", Toast.LENGTH_LONG)
              .show()
            Log.e(TAG, "Feature Layer failed to load!")
          }
        }
        // add feature layer to the map
        mapView.map.operationalLayers.add(featureLayer)
      } else {
        Toast.makeText(this@MainActivity, "Geodatabase failed to load!", Toast.LENGTH_LONG).show()
        Log.e(TAG, "Geodatabase failed to load!" + geodatabase.loadError)
      }
    }
  }

  /**
   * Handle the permissions request response
   */
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      loadGeodatabase()
    } else { // report to user that permission was denied
      Toast.makeText(
        this@MainActivity,
        resources.getString(R.string.write_permission_denied),
        Toast.LENGTH_SHORT
      ).show()
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