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

import android.R
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Button
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.createandeditmobilegeodatabases.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.createandeditmobilegeodatabases.databinding.TableLayoutBinding
import com.esri.arcgisruntime.sample.createandeditmobilegeodatabases.databinding.TableRowBinding
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val createButton: Button by lazy {
        activityMainBinding.createButton
    }

    private val viewTableButton: Button by lazy {
        activityMainBinding.viewTableButton
    }

    private val featureCount: TextView by lazy {
        activityMainBinding.featureCount
    }

    private var featureTable: FeatureTable? = null
    private var geodatabase: Geodatabase? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        mapView.setViewpoint(Viewpoint(34.056295, -117.195800, 10000.0))
        // handle when map is clicked by retrieving the point
        mapView.onTouchListener =
            object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
                override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
                    motionEvent?.let { event ->
                        // create a point from where the user clicked
                        android.graphics.Point(event.x.toInt(), event.y.toInt()).let { point ->
                            addFeature(mapView.screenToLocation(point))
                        }
                    }
                    return super.onSingleTapConfirmed(motionEvent)
                }
            }

        mapView.map.addDoneLoadingListener {
            createGeodatabase()
        }

        createButton.setOnClickListener {
            try {
                val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(geodatabase?.path))
                }
                startActivity(Intent.createChooser(sharingIntent, "Share using"))
                //createGeodatabase()

            } catch (e: Exception) {
                showError(e.message)
            }
        }

        viewTableButton.setOnClickListener {
            displayTable()
        }
    }

    private fun displayTable() {
        val queryResultFuture = featureTable?.queryFeaturesAsync(QueryParameters())
        queryResultFuture?.addDoneListener {
            val queryResults = queryResultFuture.get()

            val tableLayoutBinding = TableLayoutBinding.inflate(layoutInflater)
            // create custom dialog
            Dialog(this).apply {
                setContentView(tableLayoutBinding.root)
                setCancelable(true)

                val table = tableLayoutBinding.tableLayout
                queryResults.forEach { feature ->
                    val tableRowBinding = TableRowBinding.inflate(layoutInflater)
                    tableRowBinding.oid.text = feature.attributes["oid"].toString()
                    tableRowBinding.collectionTimestamp.text =
                        (feature.attributes["collection_timestamp"] as Calendar).time.toString()
                    table.addView(tableRowBinding.root)
                }
                table.requestLayout()

            }.show()
        }
    }

    private fun showError(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, message.toString())
    }

    private fun createGeodatabase() {
        // define the name of the geodatabase file
        val file = File(getExternalFilesDir(null)?.path + "/locationHistory.geodatabase")
        if (file.exists()) file.delete()
        // create the geodatabase file
        val geodatabaseFuture = Geodatabase.createAsync(file.path)
        geodatabaseFuture.addDoneListener {
            Log.e(TAG, "Geodatabase created")
            geodatabase = geodatabaseFuture.get()

            // construct a table description
            val tableDescription =
                TableDescription(
                    "LocationHistory",
                    SpatialReferences.getWgs84(),
                    GeometryType.POINT
                )
            // add fields to the table
            tableDescription.fieldDescriptions.addAll(
                listOf(
                    FieldDescription("oid", Field.Type.OID),
                    FieldDescription("collection_timestamp", Field.Type.DATE)
                )
            )

            // set some table properties
            tableDescription.setHasAttachments(false)
            tableDescription.setHasM(false)
            tableDescription.setHasZ(false)

            val tableFuture = geodatabaseFuture.get().createTableAsync(tableDescription)
            setupMapFromGeodatabase(tableFuture)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupMapFromGeodatabase(tableFuture: ListenableFuture<GeodatabaseFeatureTable>) {
        tableFuture.addDoneListener {
            val table = geodatabase?.geodatabaseFeatureTables?.first {
                it.tableName == "LocationHistory"
            }
            val featureLayer = FeatureLayer(table)
            mapView.map.operationalLayers.add(featureLayer)
            this.featureTable = table
            featureCount.text = "Number of features added: " + (featureTable?.totalFeatureCount)
        }
    }

    /**
     * Adds a feature to the feature table on map click
     */
    @SuppressLint("SetTextI18n")
    private fun addFeature(mapPoint: Point) {
        // set up the feature attributes
        val featureAttributes = mutableMapOf<String, Any>()
        featureAttributes["collection_timestamp"] = Calendar.getInstance()
        // create a new feature
        val feature = featureTable?.createFeature(featureAttributes, mapPoint)
        // add the feature to the feature table
        val task = featureTable?.addFeatureAsync(feature)
        task?.addDoneListener {
            if (featureTable?.loadStatus == LoadStatus.LOADED) {
                try {
                    task.get()
                    featureCount.text =
                        "Number of features added: " + (featureTable?.totalFeatureCount)
                    viewTableButton.isEnabled = true
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                }
            } else {
                // error loading the featureTable
                Log.e(TAG, "Error adding feature to table, ${featureTable?.loadError?.message}")
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
