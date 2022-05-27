/* Copyright 2020 Esri
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

package com.esri.arcgisruntime.sample.viewshedgeoprocessing

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.FeatureCollectionTable
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.viewshedgeoprocessing.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingFeatures
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingJob
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingParameters
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingTask
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val TAG: String = this::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val loadingView: View by lazy {
        activityMainBinding.loadingView
    }

    private val geoprocessingTask: GeoprocessingTask by lazy { GeoprocessingTask(getString(R.string.viewshed_service)) }
    private var geoprocessingJob: GeoprocessingJob? = null

    private val inputGraphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
    private val resultGraphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }

    // objects that implement Loadable must be class fields to prevent being garbage collected before loading
    private lateinit var featureCollectionTable: FeatureCollectionTable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create renderers for graphics overlays
        val fillColor = Color.argb(120, 226, 119, 40)
        val fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, fillColor, null)
        resultGraphicsOverlay.renderer = SimpleRenderer(fillSymbol)

        val pointSymbol = SimpleMarkerSymbol(
            SimpleMarkerSymbol.Style.CIRCLE,
            Color.RED,
            10F
        )
        inputGraphicsOverlay.renderer = SimpleRenderer(pointSymbol)

        mapView.apply {
            // create a map with the Basemap type topographic
            map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)
            // set the viewpoint
            setViewpoint(Viewpoint(45.3790902612337, 6.84905317262762, 100000.0))
            // add graphics overlays to the map view
            graphicsOverlays.addAll(listOf(resultGraphicsOverlay, inputGraphicsOverlay))
            // add onTouchListener for calculating the new viewshed
            onTouchListener = object : DefaultMapViewOnTouchListener(
                applicationContext,
                mapView
            ) {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    val screenPoint = android.graphics.Point(
                        e.x.roundToInt(),
                        e.y.roundToInt()
                    )
                    val mapPoint = mMapView.screenToLocation(screenPoint)
                    addGraphicForPoint(mapPoint)
                    calculateViewshedFrom(mapPoint)
                    return super.onSingleTapConfirmed(e)
                }
            }
        }
    }

    /**
     * Adds a graphic at the chosen mapPoint.
     *
     * @param point in MapView coordinates.
     */
    private fun addGraphicForPoint(point: Point) {
        // remove existing graphics
        inputGraphicsOverlay.graphics.clear()

        // add new graphic to the graphics overlay
        inputGraphicsOverlay.graphics.add(Graphic(point))
    }

    /**
     * Uses the given point to create a FeatureCollectionTable which is passed to performGeoprocessing.
     *
     * @param point in MapView coordinates.
     */
    private fun calculateViewshedFrom(point: Point) {
        // display the LoadingView while calculating the Viewshed
        loadingView!!.visibility = View.VISIBLE

        // remove previous graphics
        resultGraphicsOverlay.graphics.clear()

        // cancel any previous job
        geoprocessingJob?.cancel()

        // create field with same alias as name
        val field = Field.createString("observer", "", 8)

        // create feature collection table for point geometry
        featureCollectionTable =
            FeatureCollectionTable(listOf(field), GeometryType.POINT, point.spatialReference)
        featureCollectionTable.loadAsync()

        // create a new feature and assign the geometry
        val newFeature = featureCollectionTable.createFeature().apply {
            geometry = point
        }

        // add newFeature and call perform Geoprocessing on done loading
        featureCollectionTable.addFeatureAsync(newFeature)
        featureCollectionTable.addDoneLoadingListener {
            if (featureCollectionTable.loadStatus == LoadStatus.LOADED) {
                performGeoprocessing(featureCollectionTable)
            }
        }
    }

    /**
     * Creates a GeoprocessingJob from the GeoprocessingTask. Displays the resulting viewshed on the map.
     *
     * @param featureCollectionTable the feature collection table containing the observation point.
     */
    private fun performGeoprocessing(featureCollectionTable: FeatureCollectionTable) {

        // geoprocessing parameters
        val parameterFuture: ListenableFuture<GeoprocessingParameters> =
            geoprocessingTask.createDefaultParametersAsync()
        parameterFuture.addDoneListener {
            try {
                val parameters = parameterFuture.get().apply {
                    processSpatialReference = featureCollectionTable.spatialReference
                    outputSpatialReference = featureCollectionTable.spatialReference

                    // use the feature collection table to create the required GeoprocessingFeatures input
                    inputs["Input_Observation_Point"] =
                        GeoprocessingFeatures(featureCollectionTable)
                }

                // initialize job from geoprocessingTask
                geoprocessingJob = geoprocessingTask.createJob(parameters)

                // start the job
                geoprocessingJob?.start()

                // listen for job success
                geoprocessingJob?.addJobDoneListener {
                    // hide the LoadingView when the geoprocessing job is done
                    loadingView!!.visibility = View.GONE

                    if (geoprocessingJob?.status == Job.Status.SUCCEEDED) {
                        // get the viewshed from geoprocessingResult
                        (geoprocessingJob?.result?.outputs?.get("Viewshed_Result") as? GeoprocessingFeatures)?.let { viewshedResult ->
                            // for each feature in the result
                            for (feature in viewshedResult.features) {
                                // add the feature as a graphic
                                resultGraphicsOverlay.graphics.add(Graphic(feature.geometry))
                            }
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Geoprocessing result failed!",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e(TAG, geoprocessingJob?.error?.cause.toString())
                    }
                }
            } catch (e: Exception) {
                when (e) {
                    is InterruptedException, is ExecutionException -> ("Error getting geoprocessing result: " + e.message).also {
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                        Log.e(TAG, it)
                    }
                    else -> throw e
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
