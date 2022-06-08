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

package com.esri.arcgisruntime.sample.createandeditmobilegeodatabases

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.createandeditmobilegeodatabases.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.DictionarySymbolStyle
import java.io.File
import java.time.LocalDateTime
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private var featureTable: FeatureTable? = null
    private var geodatabase: Geodatabase? = null
    private var sequence = 1


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION_NIGHT)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        mapView.setViewpoint(Viewpoint(34.056295, -117.195800, 10000.0))

        // handle when map is clicked by retrieving the point
        mapView.onTouchListener =
            object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
                override fun onSingleTapConfirmed(event: MotionEvent?): Boolean {
                    if (event != null) {
                        Log.e(TAG, "Event")
                        // create a point from where the user clicked
                        val screenPoint = Point(event.x.toInt(), event.y.toInt())
                        createGeodatabase()
                    }
                    return super.onSingleTapConfirmed(event)
                }
            }
    }

    private fun createGeodatabase() {
        // define the name of the geodatabase file
        val file = File(getExternalFilesDir(null)?.path + "/locationHistory.geodatabase")
        // create the geodatabase file
        val geodatabaseFuture = Geodatabase.createAsync(file.path)
        geodatabaseFuture.addDoneListener {
            // construct a table description
            val tableDescription =
                TableDescription(
                    "Location History",
                    SpatialReferences.getWgs84(),
                    GeometryType.POINT
                )
            // add fields to the table
            tableDescription.fieldDescriptions.addAll(
                listOf(
                    FieldDescription("oid", Field.Type.OID),
                    FieldDescription("name", Field.Type.TEXT),
                    FieldDescription("sequence", Field.Type.INTEGER),
                    FieldDescription("collection_timestamp", Field.Type.DATE)
                )
            )

            // set some table properties
            tableDescription.setHasAttachments(false)
            tableDescription.setHasM(false)
            tableDescription.setHasZ(false)

            val geodatabase = geodatabaseFuture.get()
            geodatabase.createTableAsync(tableDescription)
            setupMapFromGeodatabase(geodatabase)
        }
    }

    private fun setupMapFromGeodatabase(geodatabase: Geodatabase) {
        geodatabase.addDoneLoadingListener {
            if (geodatabase.loadStatus == LoadStatus.LOADED) {
                Log.e(TAG, "Geodatabase created")
                val table =
                    geodatabase.geodatabaseFeatureTables.first { (it.tableName == "LocationHistory") }
                val featureLayer = FeatureLayer(table)
                mapView.map.operationalLayers.add(featureLayer)
                this.featureTable = table
                this.geodatabase = geodatabase

            } else {
                val message = "Geodatabase failed to load"
                Log.e(TAG, message)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addFeature(point: com.esri.arcgisruntime.geometry.Point) {
        val featureAttributes = mapOf<String, Any>(
            "name" to "Shubham's location",
            "sequence" to sequence,
            "collection_timestamp" to Date()
        )
        // create a new feature
        val feature = featureTable?.createFeature(featureAttributes,point)
        // add the feature to the feature table
        featureTable?.addFeatureAsync(feature)
        featureTable?.addDoneLoadingListener {
            // Feature done loading
        }
        sequence++
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
