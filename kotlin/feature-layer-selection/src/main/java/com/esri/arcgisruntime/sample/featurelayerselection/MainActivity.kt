/*
 * Copyright 2020 Esri
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

package com.esri.arcgisruntime.sample.featurelayerselection

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val TAG: String = MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create service feature table and a feature layer from it
    val serviceFeatureTable = ServiceFeatureTable(getString(R.string.gdp_per_capita_url))
    val featureLayer = FeatureLayer(serviceFeatureTable)

    // create a map with the streets base map type
    val streetsMap = ArcGISMap(BasemapStyle.ARCGIS_STREETS).apply {
      // add the feature layer to the map's operational layers
      operationalLayers.add(featureLayer)
    }

    mapView.let { it ->
      // set the map to be displayed in the layout's map view
      it.map = streetsMap
      // set an initial view point
      it.setViewpoint(
        Viewpoint(
          Envelope(
            -1131596.019761,
            3893114.069099,
            3926705.982140,
            7977912.461790,
            SpatialReferences.getWebMercator()
          )
        )
      )
      // give any item selected on the map view a red selection halo
      it.selectionProperties.color = Color.RED
      // set an on touch listener on the map view
      it.onTouchListener = object : DefaultMapViewOnTouchListener(this, it) {
        override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
          // clear the previous selection
          featureLayer.clearSelection()
          // get the point that was tapped and convert it to a point in map coordinates
          val tappedPoint =
            android.graphics.Point(motionEvent.x.roundToInt(), motionEvent.y.roundToInt())
          // set a tolerance for accuracy of returned selections from point tapped
          val tolerance = 25.0

          val identifyLayerResultFuture =
            mapView.identifyLayerAsync(featureLayer, tappedPoint, tolerance, false, -1)
          identifyLayerResultFuture.addDoneListener {
            try {
              val identifyLayerResult = identifyLayerResultFuture.get()
              // get the elements in the selection that are features
              val features = identifyLayerResult.elements.filterIsInstance<Feature>()
              // select each feature
              featureLayer.selectFeatures(features)
              // make a toast to show the number of features selected
              Toast.makeText(
                applicationContext,
                "${features.size} features selected",
                Toast.LENGTH_SHORT
              ).show()
            } catch (e: Exception) {
              val errorMessage = "Select feature failed: " + e.message
              Log.e(TAG, errorMessage)
              Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
            }
          }
          return super.onSingleTapConfirmed(motionEvent)
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
