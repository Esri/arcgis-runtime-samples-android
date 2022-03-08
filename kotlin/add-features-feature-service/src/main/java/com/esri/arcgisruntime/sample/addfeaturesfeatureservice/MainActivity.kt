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
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.addfeaturesfeatureservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private val activityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  private val mapView: MapView by lazy {
    activityMainBinding.mapView
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activityMainBinding.root)

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create a map with streets basemap
    ArcGISMap(BasemapStyle.ARCGIS_STREETS).let { map ->

      // create service feature table from URL
      // create and load the service geodatabase
      val serviceGeodatabase = ServiceGeodatabase(getString(R.string.service_layer_url))
      serviceGeodatabase.loadAsync()
      serviceGeodatabase.addDoneLoadingListener {
        // create a feature layer using the first layer in the ServiceFeatureTable
        val serviceFeatureTable = serviceGeodatabase.getTable(0)

        // add the  feature layer from table to the map
        map.operationalLayers.add(FeatureLayer(serviceFeatureTable))

        // add a listener to the MapView to detect when a user has performed a single tap to add a new feature to
        // the service feature table
        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
          override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
            motionEvent?.let { event ->
              // create a point from where the user clicked
              android.graphics.Point(event.x.toInt(), event.y.toInt()).let { point ->
                // create a map point from a point
                mapView.screenToLocation(point)
              }.let { mapPoint ->
                // add a new feature to the service feature table
                addFeature(mapPoint, serviceFeatureTable)
              }
            }
            return super.onSingleTapConfirmed(motionEvent)
          }
        }
      }
      // set map to be displayed in map view
      mapView.map = map
      // set an initial view point
      mapView.setViewpoint(Viewpoint(40.0, -95.0, 10000000.0))
    }
  }

  /**
   * Adds a new Feature to a ServiceFeatureTable and applies the changes to the
   * server.
   *
   * @param mapPoint location to add feature
   * @param featureTable service feature table to add feature
   */
  private fun addFeature(mapPoint: Point, featureTable: ServiceFeatureTable) {

    // create default attributes for the feature
    hashMapOf<String, Any>(
      "typdamage" to "Destroyed",
      "primcause" to "Earthquake"
    ).let { attributes ->
      // creates a new feature using default attributes and point
      featureTable.createFeature(attributes, mapPoint)
    }.let { feature ->
      // check if feature can be added to feature table
      if (featureTable.canAdd()) {
        // add the new feature to the feature table and to server
        featureTable.addFeatureAsync(feature).addDoneListener { applyEdits(featureTable) }
      } else {
        logToUser(true, getString(R.string.error_cannot_add_to_feature_table))
      }
    }

  }

  /**
   * Sends any edits on the ServiceFeatureTable to the server.
   *
   * @param featureTable service feature table
   */
  private fun applyEdits(featureTable: ServiceFeatureTable) {

    // apply the changes to the server
    featureTable.serviceGeodatabase.applyEditsAsync().let { editResult ->
      editResult.addDoneListener {
        try {
          editResult.get()?.let { edits ->
            logToUser(false, getString(R.string.feature_added))
          }
        } catch (e: ArcGISRuntimeException) {
          logToUser(true, getString(R.string.error_applying_edits, e.cause?.message))
        }
      }
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

/*
* AppCompatActivity Extensions
*/

/**
 * Shows a Toast to user and logs to logcat.
 *
 * @param isError whether message is an error. Determines log level.
 * @param message message to display
 */
fun AppCompatActivity.logToUser(isError: Boolean, message: String) {
  Toast.makeText(this, message, Toast.LENGTH_LONG).show()
  if (isError) {
    Log.e(this::class.java.simpleName, message)
  } else {
    Log.d(this::class.java.simpleName, message)
  }
}
