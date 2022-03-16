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

package com.esri.arcgisruntime.sample.addfeatureswithcontingentvalues

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.addfeatureswithcontingentvalues.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.addfeatureswithcontingentvalues.databinding.AddFeatureLayoutBinding
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.google.android.material.bottomsheet.BottomSheetDialog


class MainActivity : AppCompatActivity() {


    private val TAG: String = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val graphicsOverlay = GraphicsOverlay()

    private lateinit var featureTable: FeatureTable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // Use the vector tiled layer as a basemap
        val fillmoreVectorTiledLayer =
            ArcGISVectorTiledLayer(getExternalFilesDir(null)?.path + getString(R.string.topographic_map))
        mapView.map = ArcGISMap(Basemap(fillmoreVectorTiledLayer))
        mapView.graphicsOverlays.add(graphicsOverlay)

        // add a listener to the MapView to detect when a user has performed a single tap to add a new feature
        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
            override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
                motionEvent?.let { event ->
                    // create a point from where the user clicked
                    android.graphics.Point(event.x.toInt(), event.y.toInt()).let { point ->
                        // create a map point and add a new feature to the service feature table
                        openBottomSheetView(mapView.screenToLocation(point))
                    }
                }
                mapView.performClick()
                return super.onSingleTapConfirmed(motionEvent)
            }
        }

        val geoDatabase =
            Geodatabase(getExternalFilesDir(null)?.path + getString(R.string.bird_nests))
        geoDatabase.loadAsync()
        geoDatabase.addDoneLoadingListener {
            if (geoDatabase.loadStatus == LoadStatus.LOADED) {
                // Get and load the first feature table in the geodatabase
                featureTable = geoDatabase.geodatabaseFeatureTables[0] as FeatureTable
                featureTable.loadAsync()
                featureTable.addDoneLoadingListener {
                    // Create and load the feature layer from the feature table
                    val featureLayer = FeatureLayer(featureTable)
                    // Add the feature layer to the map
                    mapView.map.operationalLayers.add(featureLayer)
                    // Set the map's viewpoint to the feature layer's full extent
                    val extent = featureLayer.fullExtent
                    mapView.setViewpoint(Viewpoint(extent))
                    // Add buffer graphics for the feature layer
                    queryFeatures()
                }

            } else {
                val error = "Error loading GeoDatabase: " + geoDatabase.loadError.message
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                Log.e(TAG, error)
            }
        }
    }

    // Create buffer graphics for the features
    private fun queryFeatures() {
        // Create buffer graphics for the features
        val queryParameters = QueryParameters()
        // Set the where clause to filter for buffer sizes greater than 0
        queryParameters.whereClause = "BufferSize > 0"
        // Create an array of graphics to add to the graphics overlay
        val graphics = mutableListOf<Graphic>()
        val queryFeaturesFuture = featureTable.queryFeaturesAsync(queryParameters)
        queryFeaturesFuture.addDoneListener {
            try {
                // clear the existing graphics
                graphicsOverlay.graphics.clear()
                // call get on the future to get the result
                val result = queryFeaturesFuture.get()
                val resultIterator = result.iterator()
                if (resultIterator.hasNext()) {
                    while (resultIterator.hasNext())
                        graphics.add(createGraphic(resultIterator.next()))
                } else {
                    "No features found with BufferSize > 0".also {
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                        Log.d(TAG, it)
                        return@addDoneListener
                    }
                }
                // Add the graphics to the graphics overlay
                graphicsOverlay.graphics.addAll(graphics)
            } catch (e: Exception) {
                e.printStackTrace()
                val message = "Error querying features: " + e.message
                Log.e(TAG, message)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Create a graphic for the given feature
    private fun createGraphic(feature: Feature): Graphic {
        // Get the feature's buffer size
        val bufferSize = feature.attributes["BufferSize"] as Int
        // Get a polygon using the feature's buffer size and geometry
        val polygon = GeometryEngine.buffer(feature.geometry, bufferSize.toDouble())
        // Create the outline for the buffers
        val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2F)
        // Create the buffer symbol
        val bufferSymbol =
            SimpleFillSymbol(SimpleFillSymbol.Style.FORWARD_DIAGONAL, Color.RED, lineSymbol)
        // Create an a graphic and add it to the array.
        return Graphic(polygon, bufferSymbol)
    }

    // Add a single feature to the map
    private fun openBottomSheetView(mapPoint: Point) {
        // Create a symbol to represent a bird's nest
        val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLACK, 11F)
        // Add the graphic to the graphics overlay
        graphicsOverlay.graphics.add(Graphic(mapPoint, symbol))
        // Creates a new BottomSheetDialog
        val dialog = BottomSheetDialog(this)

        // Inflates layout file
        val bottomSheetBinding = AddFeatureLayoutBinding.inflate(layoutInflater)

        bottomSheetBinding.apply {
            
            protectionView.setOnClickListener {
                dialog.dismiss()
            }

            bufferSizeView.setOnClickListener {
                dialog.dismiss()
            }

            applyTv.setOnClickListener { dialog.dismiss() }
            cancelTv.setOnClickListener { dialog.dismiss() }
        }

        dialog.setCancelable(false)
        // Sets bottom sheet content view to layout
        dialog.setContentView(bottomSheetBinding.root)
        // Displays bottom sheet view
        dialog.show()
    }

    fun Spinner.selected(action: (position:Int) -> Unit) {
        this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                action(position)
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
