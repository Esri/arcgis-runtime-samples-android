/* Copyright 2022 Esri
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

package com.esri.arcgisruntime.sample.queryfeatureswitharcadeexpression

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.arcade.ArcadeEvaluationResult
import com.esri.arcgisruntime.arcade.ArcadeEvaluator
import com.esri.arcgisruntime.arcade.ArcadeExpression
import com.esri.arcgisruntime.arcade.ArcadeProfile
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.queryfeatureswitharcadeexpression.databinding.ActivityMainBinding
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    // hold a reference to the police beats layer for use in event handlers
    private var policeBeatsLayer: Layer? = null

    // hold a reference to the feature for use in event handlers
    private var previousFeature: ArcGISFeature? = null


    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val progressBar: ProgressBar by lazy {
        activityMainBinding.progressBar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // load the portal and create a map from the portal item
        val portal = Portal("https://www.arcgis.com/", false)
        val portalItem = PortalItem(portal, "539d93de54c7422f88f69bfac2aebf7d")

        mapView.apply {
            // set the map to be displayed in the layout's MapView
            map = ArcGISMap(portalItem)
            // set the RPD Beats layer to be visible when map is loaded
            map.addDoneLoadingListener {
                if (map.loadStatus == LoadStatus.LOADED) {
                    // find the RPD Beats layer from the map's operational layers
                    policeBeatsLayer = map.operationalLayers.find { it.name.equals("RPD Beats  - City_Beats_Border_1128-4500") }
                } else {
                    showError(map.loadError.message.toString())
                }
            }
            // add a listener on map tapped
            onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    // get the screen point of the tapped map
                    val screenPoint = Point(e.x.roundToInt(), e.y.toInt())
                    // evaluate an Arcade expression on the point
                    evaluateArcadeExpression(screenPoint)
                    return true
                }
            }
        }
    }

    /**
     * Evaluate the Arcade expression at the [tappedPoint] and use the result
     * to show a callout with the crime in the last 60 days
     */
    private fun evaluateArcadeExpression(tappedPoint: Point) {
        // get the layer based on the position tapped on the MapView.
        val identifyLayerResultFuture = mapView.identifyLayerAsync(policeBeatsLayer, tappedPoint, 12.0, false)
        identifyLayerResultFuture.addDoneListener {
            val identifyLayerResult = identifyLayerResultFuture.get()
            // if layer is not identified, then return
            if (identifyLayerResult == null || !identifyLayerResult.elements.any()) {
                return@addDoneListener
            }

            // get the tapped GeoElement as an ArcGISFeature
            val feature = identifyLayerResult.elements.first() as ArcGISFeature

            // if the previously tapped feature is null or the previous feature ID does not match the current feature ID
            // run the Arcade expression query to get the crime count for the tapped feature
            if (previousFeature == null || (feature.attributes["ID"]?.equals(previousFeature?.attributes?.get("ID")) == false)) {
                // show the loading indicator as the Arcade evaluator evaluation call can take time to complete
                progressBar.visibility = View.VISIBLE
                // instantiate a string containing the Arcade expression
                val expressionValue =
                    "var crimes = FeatureSetByName(\$map, 'Crime in the last 60 days');\n" +
                        "return Count(Intersects(\$feature, crimes));"
                // create an ArcadeExpression using the string expression
                val arcadeExpression = ArcadeExpression(expressionValue)
                // create an ArcadeEvaluator with the ArcadeExpression and an ArcadeProfile
                val evaluator = ArcadeEvaluator(arcadeExpression, ArcadeProfile.FORM_CALCULATION)
                // instantiate a list of profile variable key value pairs
                val profileVariables = mapOf<String, Any>("\$feature" to feature, "\$map" to mapView.map)
                // get the Arcade evaluation result future given the previously set profile variables
                val resultFuture = evaluator.evaluateAsync(profileVariables)
                resultFuture.addDoneListener {
                    // get the result as an ArcadeEvaluationResult
                    val arcadeEvaluationResult = resultFuture.get()
                    val crimesCount = (arcadeEvaluationResult.result as Double).toInt()
                    // set the callout content on the map using the Arcade evaluation result
                    val calloutContent = TextView(applicationContext).apply {
                        setTextColor(Color.BLACK)
                        setSingleLine()
                        text = "Crimes in the last 60 days: $crimesCount"
                    }
                    // convert the screen point to a map point
                    val mapPoint = mapView.screenToLocation(tappedPoint)
                    // display the callout at the tapped location
                    mapView.callout.apply {
                        location = mapPoint
                        content = calloutContent
                        show()
                    }
                    // center the map on the tapped map point
                    mapView.setViewpointCenterAsync(mapPoint)
                    // hide the progress bar
                    progressBar.visibility = View.GONE
                    // set the current feature as the previous feature for the next tap detection
                    previousFeature = feature
                }
            } else {
                // same feature selected, so just update the ViewPoint and callout location
                // convert the screen point to a map point
                val mapPoint = mapView.screenToLocation(tappedPoint)
                // center the map on the tapped map point
                mapView.setViewpointCenterAsync(mapPoint)
                // update the location of the callout
                mapView.callout.location = mapPoint
            }
        }
    }

    private fun showError(message: String) {
        Log.e(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
