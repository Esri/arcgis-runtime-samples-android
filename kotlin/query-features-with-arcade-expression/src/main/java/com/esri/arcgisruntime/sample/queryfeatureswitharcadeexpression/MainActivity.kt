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

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
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
    // hold a reference to the layer for use in event handlers
    private var layer: Layer? = null
    // hold a reference to the feature for use in event handlers
    private var previousFeature: ArcGISFeature?= null


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
        val portalItem = PortalItem(portal, "14562fced3474190b52d315bc19127f6")
        val map = ArcGISMap(portalItem)


        // set the map to be displayed in the layout's MapView
        mapView.map = map

        mapView.map.addDoneLoadingListener {
            if(mapView.map.loadStatus == LoadStatus.LOADED){
                // Set the visibility of all but the RDT Beats layer to false to avoid cluttering the UI
                mapView.map.operationalLayers.forEach { layer ->
                    layer.isVisible = layer.name == "RPD Beats  - City_Beats_Border_1128-4500"
                }
                layer = map.operationalLayers.find { it.name.equals("RPD Beats  - City_Beats_Border_1128-4500") }
            }
        }
        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                val screenPoint = Point(e.x.roundToInt(), e.y.toInt())
                evaluateArcadeExpression(screenPoint)
                return true
            }
        }
    }

    private fun evaluateArcadeExpression(tappedPoint: Point) {
        // get the layer based on the position tapped on the MapView.
        val identifyLayerResultFuture = mapView.identifyLayerAsync(layer,tappedPoint,12.0,false)
        identifyLayerResultFuture.addDoneListener {
            val identifyLayerResult = identifyLayerResultFuture.get()
            if(identifyLayerResult == null || !identifyLayerResult.elements.any()){
                return@addDoneListener
            }

            // get the tapped GeoElement as an ArcGISFeature
            val element = identifyLayerResult.elements.first()
            val feature = element as ArcGISFeature

            // if the previously clicked feature is null or the previous feature ID does not match the current feature ID
            // run the arcade expression query to get the crime count for a given feature
            if(previousFeature == null || !(feature.attributes["ID"]?.equals(previousFeature?.attributes?.get("ID")))!!){
                // show the loading indicator as the arcade evaluator evaluation call can take time to complete
                progressBar.visibility = View.VISIBLE

                // instantiate a string containing the arcade expression
                val expressionValue =
                    "var crimes = FeatureSetByName(\$map, 'Crime in the last 60 days');\n" +
                    "return Count(Intersects(\$feature, crimes));"
                // create an ArcadeExpression using the string expression
                val arcadeExpression = ArcadeExpression(expressionValue)
                // create an ArcadeEvaluator with the ArcadeExpression and an ArcadeProfile enum
                val evaluator = ArcadeEvaluator(arcadeExpression,ArcadeProfile.FORM_CALCULATION)

                // instantiate a list of profile variable key value pairs
                val profileVariables = mapOf<String, Any>("\$feature" to feature, "\$map" to mapView.map)

                // get the arcade evaluation result given the previously set profile variables
                val resultFuture = evaluator.evaluateAsync(profileVariables)
                resultFuture.addDoneListener {
                    val arcadeEvaluationResult = resultFuture.get() as ArcadeEvaluationResult
                    Toast.makeText(this, "Crimes in the last 60 days: ${arcadeEvaluationResult.result}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
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
