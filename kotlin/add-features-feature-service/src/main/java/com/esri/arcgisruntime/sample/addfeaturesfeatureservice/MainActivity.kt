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
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  private lateinit var featureTable: ServiceFeatureTable

  companion object {
    private val TAG = MainActivity::class.java.simpleName
  }

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

    mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
      override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
        motionEvent?.let { event ->
          // create a point from where the user clicked
          val point = android.graphics.Point(event.x.toInt(), event.y.toInt())

          // create a map point from a point
          val mapPoint = mapView.screenToLocation(point)

          // for a wrapped around map, the point coordinates include the wrapped around value
          // for a service in projected coordinate system, this wrapped around value has to be normalized
          val normalizedMapPoint = GeometryEngine.normalizeCentralMeridian(mapPoint) as Point

          // add a new feature to the service feature table
          addFeature(normalizedMapPoint, featureTable)
        }
        return super.onSingleTapConfirmed(motionEvent)
      }
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
    val attributes = HashMap<String, Any>()
    attributes["typdamage"] = "Destroyed"
    attributes["primcause"] = "Earthquake"

    // creates a new feature using default attributes and point
    val feature = featureTable.createFeature(attributes, mapPoint)

    // check if feature can be added to feature table
    if (featureTable.canAdd()) {
      // add the new feature to the feature table and to server
      featureTable.addFeatureAsync(feature).addDoneListener { applyEdits(featureTable) }
    } else {
      logToUser(getString(R.string.error_cannot_add_to_feature_table))
    }
  }

  /**
   * Sends any edits on the ServiceFeatureTable to the server.
   *
   * @param featureTable service feature table
   */
  private fun applyEdits(featureTable: ServiceFeatureTable) {

    // apply the changes to the server
    val editResult = featureTable.applyEditsAsync()
    editResult.addDoneListener {
      try {
        val edits = editResult.get()
        // check if the server edit was successful
        if (edits != null && edits.size > 0) {
          if (!edits[0].hasCompletedWithErrors()) {
            logToUser(getString(R.string.feature_added))
          } else {
            throw edits[0].error
          }
        }
      } catch (e: InterruptedException) {
        logToUser(getString(R.string.error_applying_edits, e.cause?.message))
      } catch (e: Exception) {

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
fun AppCompatActivity.logToUser(message: String) {
  Toast.makeText(this, message, Toast.LENGTH_LONG).show()
  Log.d(this::class.java.simpleName, message)
}