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

package com.esri.arcgisruntime.sample.createmobilegeodatabase

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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
import com.esri.arcgisruntime.sample.createmobilegeodatabase.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.createmobilegeodatabase.databinding.TableLayoutBinding
import com.esri.arcgisruntime.sample.createmobilegeodatabase.databinding.TableRowBinding
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

    // feature table created using mobile geodatabase and added to the MapView
    private var featureTable: GeodatabaseFeatureTable? = null

    // mobile geodatabase used to create and store the feature attributes (LocationHistory.geodatabase)
    private var geodatabase: Geodatabase? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is
        // required to access basemaps and other location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with topographic base map style
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        mapView.apply {
            // set the map and viewpoint to be displayed in the layout's MapView
            this.map = map
            setViewpoint(Viewpoint(34.056295, -117.195800, 10000.0))
            // handle when map is clicked by retrieving the point
            onTouchListener =
                object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
                    override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
                        motionEvent?.let { event ->
                            val mapPoint = mapView.screenToLocation(
                                android.graphics.Point(
                                    event.x.toInt(),
                                    event.y.toInt()
                                )
                            )
                            // create a point from where the user clicked
                            addFeature(mapPoint)
                        }
                        return super.onSingleTapConfirmed(motionEvent)
                    }
                }
        }

        viewTableButton.setOnClickListener {
            // displays table dialog with the values in the feature table
            displayTable()
        }

        // opens a share-sheet with the "LocationHistory.geodatabase" file
        createButton.setOnClickListener {
            try {
                // close the mobile geodatabase before sharing
                geodatabase?.close()
                // get the URI of the geodatabase file using FileProvider
                val geodatabaseURI = FileProvider.getUriForFile(
                    this, getString(R.string.file_provider_package), File(
                        geodatabase?.path.toString()
                    )
                )
                // set up the sharing intent with the geodatabase URI
                val geodatabaseIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_STREAM, geodatabaseURI)
                }
                // open the Android share sheet
                startActivity(geodatabaseIntent)
            } catch (e: Exception) {
                showError("Error sharing file: ${e.message}")
            }
        }

    }

    /**
     * Create and load a new geodatabase file with TableDescription fields
     */
    private fun createGeodatabase() {
        // define the path and name of the geodatabase file
        // note: the path defined must be non-empty, available,
        // allow read/write access, and end in ".geodatabase"
        val file = File(getExternalFilesDir(null)?.path + "/LocationHistory.geodatabase")
        if (file.exists()) file.delete()

        // create a geodatabase file at the file path
        val geodatabaseFuture = Geodatabase.createAsync(file.path)
        geodatabaseFuture.addDoneListener {
            // get the instance of the mobile geodatabase
            geodatabase = geodatabaseFuture.get()
            // construct a table description which stores features as points on map
            val tableDescription =
                TableDescription(
                    "LocationHistory",
                    SpatialReferences.getWgs84(),
                    GeometryType.POINT
                )
            // set up the fields to the table,
            // Field.Type.OID is the primary key of the SQLite
            // Field.Type.DATE is a date column used to store a Calendar date
            // FieldDescriptions can be a SHORT, INTEGER, GUID, FLOAT, DOUBLE, DATE, TEXT, OID, GLOBALID, BLOB, GEOMETRY, RASTER, or XML.
            tableDescription.fieldDescriptions.addAll(
                listOf(
                    FieldDescription("oid", Field.Type.OID),
                    FieldDescription("collection_timestamp", Field.Type.DATE)
                )
            )

            // set any properties not needed to false
            tableDescription.apply {
                setHasAttachments(false)
                setHasM(false)
                setHasZ(false)
            }

            // add the tableDescriptions to the geodatabase by creating a new table
            val tableFuture = geodatabase?.createTableAsync(tableDescription)
            if (tableFuture != null) {
                setupMapFromGeodatabase(tableFuture)
            } else {
                showError("Error adding FieldDescriptions to the mobile geodatabase")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    /**
     * Set up the MapView to display the Feature layer
     * using the loaded [tableFuture] GeodatabaseFeatureTable
     */
    private fun setupMapFromGeodatabase(tableFuture: ListenableFuture<GeodatabaseFeatureTable>) {
        tableFuture.addDoneListener {
            // find the table with the table name "LocationHistory"
            val table = geodatabase?.geodatabaseFeatureTables?.first {
                it.tableName == "LocationHistory"
            }
            // create a feature layer for the map using the GeodatabaseFeatureTable
            val featureLayer = FeatureLayer(table)
            mapView.map.operationalLayers.add(featureLayer)
            this.featureTable = table

            // display the current count of features in the FeatureTable
            featureCount.text = "Number of features added: " + (featureTable?.totalFeatureCount)
        }
    }

    /**
     * Create a feature with attributes on map click and it to the [featureTable]
     * Also, updates the TotalFeatureCount on the screen
     */
    @SuppressLint("SetTextI18n")
    private fun addFeature(mapPoint: Point) {
        // set up the feature attributes
        val featureAttributes = mutableMapOf<String, Any>()
        featureAttributes["collection_timestamp"] = Calendar.getInstance()

        // create a new feature at the mapPoint
        val feature = featureTable?.createFeature(featureAttributes, mapPoint)
        // add the feature to the feature table
        val task = featureTable?.addFeatureAsync(feature)
        task?.addDoneListener {
            if (featureTable?.loadStatus == LoadStatus.LOADED) {
                try {
                    // if feature wasn't added successfully "task.get()" will throw an exception
                    task.get()
                    // feature added successfully, update count
                    featureCount.text = "Number of features added: " + (featureTable?.totalFeatureCount)
                    // enable table button since at least 1 feature loaded on the GeodatabaseFeatureTable
                    viewTableButton.isEnabled = true
                } catch (e: Exception) {
                    showError(e.message.toString())
                }
            } else {
                // error loading the featureTable
                showError("Error adding feature to table, ${featureTable?.loadError?.message}")
            }
        }
    }

    /**
     * Displays a dialog with the table of features
     * added to the GeodatabaseFeatureTable [featureTable]
     */
    private fun displayTable() {
        // query all the features loaded to the table
        val queryResultFuture = featureTable?.queryFeaturesAsync(QueryParameters())
        queryResultFuture?.addDoneListener {
            val queryResults = queryResultFuture.get()
            // inflate the table layout
            val tableLayoutBinding = TableLayoutBinding.inflate(layoutInflater)
            // set up a dialog to be displayed
            Dialog(this).apply {
                setContentView(tableLayoutBinding.root)
                setCancelable(true)
                // grab the instance of the TableLayout
                val table = tableLayoutBinding.tableLayout
                // iterate through each feature to add to the TableLayout
                queryResults.forEach { feature ->
                    // prepare the table row
                    val tableRowBinding = TableRowBinding.inflate(layoutInflater).apply {
                        oid.text = feature.attributes["oid"].toString()
                        collectionTimestamp.text = (feature.attributes["collection_timestamp"] as Calendar).time.toString()
                    }

                    // add the row to the TableLayout
                    table.addView(tableRowBinding.root)
                }
            }.show()
        }
    }

    private fun showError(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, message.toString())
    }

    override fun onPause() {
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()

        // called on app launch or when Android share sheet is closed
        mapView.map.addDoneLoadingListener {
            if (mapView.map.loadStatus == LoadStatus.LOADED) {
                // clear any feature layers displayed on the map
                mapView.map.operationalLayers.clear()
                // create a new geodatabase file to add features into the feature table
                createGeodatabase()
            } else {
                showError("Error loading MapView: ${mapView.map.loadError.message}")
            }
        }

    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }
}
