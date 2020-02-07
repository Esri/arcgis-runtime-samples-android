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

import java.util.concurrent.ExecutionException

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.Toast

import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.FeatureCollectionTable
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingFeatures
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingJob
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingParameters
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingTask
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val  geoprocessingTask: GeoprocessingTask by lazy { GeoprocessingTask (getString(R.string.viewshed_service)) }
  private var geoprocessingJob: GeoprocessingJob? = null

  private val inputGraphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
  private val resultGraphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with the BasemapType topographic
    val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 45.3790902612337, 6.84905317262762, 12)
    // set the map to be displayed in this view
    mapView.map = map

    // renderer for graphics overlays
    val pointSymbol = SimpleMarkerSymbol(
      SimpleMarkerSymbol.Style.CIRCLE,
      Color.RED,
      10F
    )
    inputGraphicsOverlay.renderer = SimpleRenderer(pointSymbol)

    val fillColor = Color.argb(120, 226, 119, 40)
    val fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, fillColor, null)
    resultGraphicsOverlay.renderer = SimpleRenderer(fillSymbol)

    // add graphics overlays to the map view
    mapView.graphicsOverlays.apply {
      add(resultGraphicsOverlay)
      add(inputGraphicsOverlay)
    }

    mapView.onTouchListener = object : DefaultMapViewOnTouchListener(
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
        calculateViewshedAt(mapPoint)
        return super.onSingleTapConfirmed(e)
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
  private fun calculateViewshedAt(point: Point) {
    // remove previous graphics
    resultGraphicsOverlay.graphics.clear()

    // cancel any previous job
    geoprocessingJob?.cancel()

    // TODO: Double check this is okay.
    // create field with same alias as name
    val field = Field.createString("observer", "", 8)
    val fields = listOf(field)

    // create feature collection table for point geometry
    val featureCollectionTable =
      FeatureCollectionTable(fields, GeometryType.POINT, point.spatialReference)
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
   * @param featureCollectionTable containing the observation point.
   */
  private fun performGeoprocessing(featureCollectionTable: FeatureCollectionTable) {

    // geoprocessing parameters
    val parameterFuture: ListenableFuture<GeoprocessingParameters> = geoprocessingTask.createDefaultParametersAsync()
    parameterFuture.addDoneListener {
      try {
        val parameters = parameterFuture.get().apply {
          processSpatialReference = featureCollectionTable.spatialReference
          outputSpatialReference = featureCollectionTable.spatialReference

          // use the feature collection table to create the required GeoprocessingFeatures input
          inputs["Input_Observation_Point"] = GeoprocessingFeatures(featureCollectionTable)
        }

        // initialize job from geoprocessingTask
        geoprocessingJob = geoprocessingTask.createJob(parameters)

        // start the job
        geoprocessingJob?.start()

        // listen for job success
        geoprocessingJob?.addJobDoneListener {
          if (geoprocessingJob?.status == Job.Status.SUCCEEDED) {
            val geoprocessingResult = geoprocessingJob?.result
            // get the viewshed from geoprocessingResult
            // UNSURE: geoprocessingJob is nullable, so all of this has to be nullable as well. But I don't like all the question marks, it feels unnecessary.
            val resultFeatures = geoprocessingResult?.outputs?.get("Viewshed_Result") as GeoprocessingFeatures
            val featureSet = resultFeatures.features
            for (feature in featureSet) {
              val graphic = Graphic(feature.geometry)
              resultGraphicsOverlay.graphics.add(graphic)
            }
          } else {
            Toast.makeText(
              applicationContext,
              "Geoprocessing result failed!",
              Toast.LENGTH_LONG
            ).show()
          }
        }
      }
      // NOTE: https://stackoverflow.com/questions/36760489/how-to-catch-many-exceptions-at-the-same-time-in-kotlin
      catch (e: Exception) {
        when (e) {
          is InterruptedException, is ExecutionException -> e.printStackTrace()
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
