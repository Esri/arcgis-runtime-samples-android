/*
 * Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.editandsyncfeatures

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseJob
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseParameters
import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask
import com.esri.arcgisruntime.tasks.geodatabase.SyncGeodatabaseJob
import com.esri.arcgisruntime.tasks.geodatabase.SyncGeodatabaseParameters
import com.esri.arcgisruntime.tasks.geodatabase.SyncLayerOption
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.ArrayList
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private var currentEditState: EditState = EditState.NOT_READY

  private val graphicsOverlay by lazy { GraphicsOverlay() }
  private val geodatabaseSyncTask by lazy { GeodatabaseSyncTask("https://sampleserver6.arcgisonline.com/arcgis/rest/services/Sync/WildfireSync/FeatureServer") }
  private var geodatabase: Geodatabase? = null
  private val selectedFeatures by lazy { ArrayList<Feature>() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // use local tile package for the base map
    val sanFranciscoTileCache =
      TileCache(getExternalFilesDir(null).toString() + "/SanFrancisco.tpk")
    val tiledLayer = ArcGISTiledLayer(sanFranciscoTileCache)
    val map = ArcGISMap(Basemap(tiledLayer))

    mapView.apply {
      // set the map to the map view
      this.map = map

      // add a graphics overlay to the map view
      graphicsOverlays.add(graphicsOverlay)

      // add listener to handle motion events, which only responds once a geodatabase is loaded
      onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
        override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
          when (currentEditState) {
            EditState.READY -> selectFeaturesAt(mapPointFrom(motionEvent), 25)
            EditState.EDITING -> moveSelectedFeatureTo(mapPointFrom(motionEvent))
            EditState.NOT_READY -> Toast.makeText(
              this@MainActivity,
              "Can't edit yet. The geodatabase hasn't been generated!",
              Toast.LENGTH_LONG
            ).show()
          }
          return true
        }
      }
    }

    // add listener to handle generate/sync geodatabase button
    syncButton.setOnClickListener {
      if (currentEditState == EditState.NOT_READY) {
        generateGeodatabase()
      } else if (currentEditState == EditState.READY) {
        syncGeodatabase()
      }
    }
  }

  /**
   * Generates a local geodatabase and sets it to the map.
   */
  private fun generateGeodatabase() {
    geodatabaseSyncTask.loadAsync()
    geodatabaseSyncTask.addDoneLoadingListener {
      val boundarySymbol =
        SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5f)
      // show the extent used as a graphic
      val extent: Envelope = mapView.visibleArea.extent
      val boundary = Graphic(extent, boundarySymbol)
      graphicsOverlay.graphics.add(boundary)
      // create generate geodatabase parameters for the current extent
      val defaultParameters: ListenableFuture<GenerateGeodatabaseParameters> = geodatabaseSyncTask
        .createDefaultGenerateGeodatabaseParametersAsync(extent)
      defaultParameters.addDoneListener {
        try {
          // set parameters and don't include attachments
          val parameters = defaultParameters.get()
          parameters.isReturnAttachments = false
          // define the local path where the geodatabase will be stored
          val localGeodatabasePath = externalCacheDir?.path + "/wildfire.geodatabase"
          // create and start the job
          val generateGeodatabaseJob: GenerateGeodatabaseJob = geodatabaseSyncTask
            .generateGeodatabase(parameters, localGeodatabasePath)
          generateGeodatabaseJob.start()
          createProgressDialog(generateGeodatabaseJob)
          // get geodatabase when done
          generateGeodatabaseJob.addJobDoneListener {
            if (generateGeodatabaseJob.status == Job.Status.SUCCEEDED) {
              val geodatabase = generateGeodatabaseJob.result
              geodatabase?.addDoneLoadingListener {
                if (geodatabase.loadStatus == LoadStatus.LOADED) {
                  // get only the first table which, contains points
                  val pointsGeodatabaseFeatureTable: GeodatabaseFeatureTable =
                    geodatabase.geodatabaseFeatureTables[0]
                  val geodatabaseFeatureLayer = FeatureLayer(pointsGeodatabaseFeatureTable)
                  // add geodatabase layer to the map as a feature layer and make it selectable
                  mapView.map.operationalLayers.add(geodatabaseFeatureLayer)
                  syncButton.visibility = View.GONE
                  Log.i(TAG, "Local geodatabase stored at: $localGeodatabasePath")
                  // set edit state to ready
                  currentEditState = EditState.READY
                } else {
                  Log.e(TAG, "Error loading geodatabase: " + geodatabase.loadError.message)
                }
              }
              geodatabase?.loadAsync()


            } else if (generateGeodatabaseJob.error != null) {
              Log.e(TAG, "Error generating geodatabase: " + generateGeodatabaseJob.error.message)
              Toast.makeText(
                this,
                "Error generating geodatabase: " + generateGeodatabaseJob.error.message,
                Toast.LENGTH_LONG
              ).show()
            } else {
              Log.e(TAG, "Unknown Error generating geodatabase")
              Toast.makeText(this, "Unknown Error generating geodatabase", Toast.LENGTH_LONG).show()
            }
          }
        } catch (e: InterruptedException) {
          Log.e(TAG, "Error generating geodatabase parameters : " + e.message)
          Toast.makeText(
            this,
            "Error generating geodatabase parameters: " + e.message,
            Toast.LENGTH_LONG
          ).show()
        } catch (e: ExecutionException) {
          Log.e(TAG, "Error generating geodatabase parameters : " + e.message)
          Toast.makeText(
            this,
            "Error generating geodatabase parameters: " + e.message,
            Toast.LENGTH_LONG
          ).show()
        }
      }
    }
  }

  /**
   * Syncs changes made on either the local or web service geodatabase with each other.
   */
  private fun syncGeodatabase() {
    // create parameters for the sync task
    val syncGeodatabaseParameters = SyncGeodatabaseParameters()
    syncGeodatabaseParameters.syncDirection = SyncGeodatabaseParameters.SyncDirection.BIDIRECTIONAL
    syncGeodatabaseParameters.isRollbackOnFailure = false
    geodatabase?.let { geodatabase ->
      // get the layer ID for each feature table in the geodatabase, then add to the sync job
      geodatabase.geodatabaseFeatureTables.forEach { geodatabaseFeatureTable ->
        val serviceLayerId = geodatabaseFeatureTable.serviceLayerId
        val syncLayerOption = SyncLayerOption(serviceLayerId)
        syncGeodatabaseParameters.layerOptions.add(syncLayerOption)
      }
      val syncGeodatabaseJob: SyncGeodatabaseJob = geodatabaseSyncTask
        .syncGeodatabase(syncGeodatabaseParameters, geodatabase)
      syncGeodatabaseJob.start()
      createProgressDialog(syncGeodatabaseJob)
      syncGeodatabaseJob.addJobDoneListener {
        if (syncGeodatabaseJob.status == Job.Status.SUCCEEDED) {
          Toast.makeText(this, "Sync complete", Toast.LENGTH_SHORT).show()
          syncButton.visibility = View.INVISIBLE
        } else {
          Log.e(TAG, "Database did not sync correctly!")
          Toast.makeText(this, "Database did not sync correctly!", Toast.LENGTH_LONG).show()
        }
      }
    }
  }

  /**
   * Create a progress dialog to show sync state
   */
  private fun createProgressDialog(job: Job) {
    val syncProgressDialog = ProgressDialog(this)
    syncProgressDialog.setTitle("Sync geodatabase job")
    syncProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
    syncProgressDialog.setCanceledOnTouchOutside(false)
    syncProgressDialog.show()
    job.addProgressChangedListener { syncProgressDialog.progress = job.progress }
    job.addJobDoneListener { syncProgressDialog.dismiss() }
  }

  /**
   * Queries the features at the tapped point within a certain tolerance.
   *
   * @param point     contains an ArcGIS map point
   * @param tolerance distance from point within which features will be selected
   */
  private fun selectFeaturesAt(
    point: Point,
    tolerance: Int
  ) {
    // define the tolerance for identifying the feature
    val mapTolerance: Double = tolerance * mapView.unitsPerDensityIndependentPixel
    // create objects required to do a selection with a query
    val envelope =
      Envelope(
        point.x - mapTolerance, point.y - mapTolerance,
        point.x + mapTolerance, point.y + mapTolerance, mapView.spatialReference
      )
    val query = QueryParameters()
    query.geometry = envelope
    // select features within the envelope for all features on the map
    for (layer in mapView.map.operationalLayers) {
      val featureLayer = layer as FeatureLayer
      val featureQueryResultFuture = featureLayer
        .selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
      // add done loading listener to fire when the selection returns
      featureQueryResultFuture.addDoneListener {

        // Get the selected features
        val featureQueryResultFuture1 =
          featureLayer.selectedFeaturesAsync
        featureQueryResultFuture1.addDoneListener {
          try {
            val layerFeatures = featureQueryResultFuture1.get()
            for (feature in layerFeatures) {
              // Only select points for editing
              if (feature.geometry.geometryType == GeometryType.POINT) {
                selectedFeatures.add(feature)
              }
            }
          } catch (e: Exception) {
            Log.e(TAG, "Select feature failed: " + e.message)
          }
        }
        // set current edit state to editing
        currentEditState = EditState.EDITING
      }
    }
  }

  /**
   * Moves selected features to the given point.
   *
   * @param point contains an ArcGIS map point
   */
  private fun moveSelectedFeatureTo(point: Point) {
    selectedFeatures.forEach { feature ->
      feature.geometry = point
      feature.featureTable.updateFeatureAsync(feature)
    }
    selectedFeatures.clear()
    currentEditState = EditState.READY
    syncButton.text = "Sync geodatabase"
    syncButton.visibility = View.VISIBLE
  }

  /**
   * Converts motion event to an ArcGIS map point.
   *
   * @param motionEvent containing coordinates of an Android screen point
   * @return a corresponding map point in the place
   */
  private fun mapPointFrom(motionEvent: MotionEvent): Point {
    // get the screen point
    val screenPoint = android.graphics.Point(motionEvent.x.roundToInt(), motionEvent.y.roundToInt())
    // return the point that was clicked in map coordinates
    return mapView.screenToLocation(screenPoint)
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

  // enumeration to track editing of points
  internal enum class EditState {
    NOT_READY,  // Geodatabase has not yet been generated
    EDITING,  // A feature is in the process of being moved
    READY // The geodatabase is ready for synchronization or further edits
  }

  companion object {
    private val TAG: String? = MainActivity::class.simpleName
  }
}
