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

package com.esri.arcgisruntime.sample.filterbydefinitionexpressionordisplayfilter

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.DisplayFilter
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.ManualDisplayFilterDefinition
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.filterbydefinitionexpressionordisplayfilter.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val featureServerURL = "https://services2.arcgis.com/ZQgQTuoyBrtmoGdP/arcgis/rest/services/SF_311_Incidents/FeatureServer/0"

    private var manualDisplayFilterDefinition: ManualDisplayFilterDefinition? = null

    private var featureLayer: FeatureLayer = FeatureLayer(ServiceFeatureTable(featureServerURL))

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val featureCountTV: TextView by lazy {
        activityMainBinding.featureCountTV
    }

    private val expressionButton: Button by lazy {
        activityMainBinding.expressionButton
    }
    private val filterButton: Button by lazy {
        activityMainBinding.filterButton
    }

    private val resetButton: Button by lazy {
        activityMainBinding.resetButton
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

        mapView.apply {
            // set the map to be displayed in the layout's MapView
            // with the BasemapType topographic
            map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)
            val viewpoint = Viewpoint(
                Point(-122.44014487516885, 37.772296660953138, SpatialReferences.getWgs84()),
                100000.0
            )
            setViewpoint(viewpoint)
            //add a feature layer to the map
            map.operationalLayers.add(featureLayer)
            map.addDoneLoadingListener {
                if (map.loadStatus == LoadStatus.LOADED) {
                    // create a display filter and display filter definition
                    // req_type here is one of the published fields
                    val damagedTrees = DisplayFilter("Damaged Trees", "req_type LIKE '%Tree Maintenance%'")
                    manualDisplayFilterDefinition = ManualDisplayFilterDefinition(damagedTrees, listOf(damagedTrees))
                }
            }

            // run countFeatures() when MapView is finished moving
            addNavigationChangedListener {
                if (!isNavigating)
                    countFeatures()
            }
        }

        featureLayer.apply {
            expressionButton.setOnClickListener {
                // Reset the display filter definition
                displayFilterDefinition = null
                // Set the definition expression to show specific features only
                definitionExpression = "req_Type = 'Tree Maintenance or Damage'"
                countFeatures()
            }

            filterButton.setOnClickListener {
                // Disable the feature layer definition expression
                definitionExpression = ""
                // Set the display filter definition on the layer
                displayFilterDefinition = manualDisplayFilterDefinition
                countFeatures()
            }

            resetButton.setOnClickListener {
                // Disable the feature layer definition expression
                definitionExpression = ""
                // Reset the display filter definition
                displayFilterDefinition = null
                countFeatures()
            }
        }
    }

    /**
     * Retrieves the totalFeatureCount from the featureTable in the [mapView]'s extent
     */
    private fun countFeatures() {
        // Get the extent of the current viewpoint, return if no valid extent.
        val extent = mapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).targetGeometry.extent ?: return

        // Update the UI with the count of features in the extent
        val queryParameters = QueryParameters()
        queryParameters.geometry = extent

        featureLayer.featureTable.apply {
            val future = queryFeatureCountAsync(queryParameters)
            future.addDoneListener {
                if (loadStatus == LoadStatus.LOADED) {
                    val totalFeatureCount = future.get()
                    Log.e("Current feature count", totalFeatureCount.toString())
                    featureCountTV.text = "Current feature count: ${totalFeatureCount}"
                } else {
                    val errorMessage = "Error receiving total feature count: ${loadError.message}"
                    Log.e(TAG, errorMessage)
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
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
