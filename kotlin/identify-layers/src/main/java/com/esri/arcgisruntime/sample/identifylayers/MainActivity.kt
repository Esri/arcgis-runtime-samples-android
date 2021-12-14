/*
 * Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package com.esri.arcgisruntime.sample.identifylayers

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.identifylayers.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val activityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  private val mapView: MapView by lazy {
    activityMainBinding.mapView
  }

  private companion object {
    private val TAG: String = MainActivity::class.java.simpleName
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activityMainBinding.root)

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create a feature layer of damaged property data
    val featureTable = ServiceFeatureTable(getString(R.string.damage_assessment))
    val featureLayer = FeatureLayer(featureTable)

    // create a layer with world cities data
    val mapImageLayer = ArcGISMapImageLayer(getString(R.string.world_cities)).apply {
      addDoneLoadingListener {
        // hide continent and world layers
        subLayerContents[1].isVisible = false
        subLayerContents[2].isVisible = false
      }
    }

    // create a topographic map
    val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC).apply {
      // add world cities layer
      operationalLayers.add(mapImageLayer)

      // add damaged property data
      operationalLayers.add(featureLayer)
    }

    mapView.apply {
      // assign the map to the map view
      this.map = map

      // set the map's initial viewpoint
      setViewpoint(Viewpoint(
        Point(-10977012.785807, 4514257.550369, SpatialReference.create(3857)),
        68015210.0
      ))
      // add a listener to detect taps on the map view
      onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, this) {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
          e?.let {
            val screenPoint = android.graphics.Point(
              it.x.roundToInt(),
              it.y.roundToInt()
            )
            identifyResult(screenPoint)
          }
          return true
        }
      }
    }
  }

  /**
   * Performs an identify on layers at the given screenpoint and calls handleIdentifyResults(...) to process them.
   *
   * @param screenPoint in Android graphic coordinates.
   */
  private fun identifyResult(screenPoint: android.graphics.Point) {

    val identifyLayerResultsFuture = mapView
      .identifyLayersAsync(screenPoint, 12.0, false, 10)

    identifyLayerResultsFuture.addDoneListener {
      try {
        val identifyLayerResults = identifyLayerResultsFuture.get()
        handleIdentifyResults(identifyLayerResults)
      } catch (e: Exception) {
        logError("Error identifying results ${e.message}")
      }
    }
  }

  /**
   * Processes identify results into a string which is passed to showAlertDialog(...).
   *
   * @param identifyLayerResults a list of identify results generated in identifyResult().
   */
  private fun handleIdentifyResults(identifyLayerResults: List<IdentifyLayerResult>) {
    val message = StringBuilder()
    var totalCount = 0
    for (identifyLayerResult in identifyLayerResults) {
      val count = recursivelyCountIdentifyResultsForSublayers(identifyLayerResult)
      val layerName = identifyLayerResult.layerContent.name
      message.append(layerName).append(": ").append(count)

      // add new line character if not the final element in array
      if (identifyLayerResult != identifyLayerResults[identifyLayerResults.size - 1]) {
        message.append("\n")
      }
      totalCount += count
    }

    // if any elements were found show the results, else notify user that no elements were found
    if (totalCount > 0) {
      showAlertDialog(message)
    } else {
      logError("No element found")
    }
  }

  /**
   * Gets a count of the GeoElements in the passed result layer.
   * This method recursively calls itself to descend into sublayers and count their results.
   * @param result from a single layer.
   * @return the total count of GeoElements.
   */
  private fun recursivelyCountIdentifyResultsForSublayers(result: IdentifyLayerResult): Int {
    var subLayerGeoElementCount = 0

    for (sublayerResult in result.sublayerResults) {
      // recursively call this function to accumulate elements from all sublayers
      subLayerGeoElementCount += recursivelyCountIdentifyResultsForSublayers(sublayerResult)
    }

    return subLayerGeoElementCount + result.elements.size
  }

  /**
   * Shows message in an AlertDialog.
   *
   * @param message contains identify results processed into a string.
   */
  private fun showAlertDialog(message: StringBuilder) {
    val alertDialogBuilder = Builder(this)

    // set title
    alertDialogBuilder.setTitle("Number of elements found")

    // set dialog message
    alertDialogBuilder
      .setMessage(message)
      .setCancelable(false)
      .setPositiveButton(getString(R.string.ok)) { dialog, which -> }

    // create alert dialog
    val alertDialog = alertDialogBuilder.create()

    // show the alert dialog
    alertDialog.show()
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

  /**
   * Log an error to logcat and to the screen via Toast.
   * @param message the text to log.
   */
  private fun logError(message: String?) {
    message?.let {
      Log.e(
        TAG,
        message
      )
      Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
  }

}
