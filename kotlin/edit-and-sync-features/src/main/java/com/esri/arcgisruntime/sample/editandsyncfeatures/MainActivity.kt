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

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.editandsyncfeatures.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.editandsyncfeatures.databinding.DialogLayoutBinding
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.geodatabase.*
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val TAG: String? = MainActivity::class.simpleName

  private var currentEditState: EditState = EditState.NOT_READY

  private val graphicsOverlay by lazy { GraphicsOverlay() }
  private val geodatabaseSyncTask by lazy { GeodatabaseSyncTask("https://sampleserver6.arcgisonline.com/arcgis/rest/services/Sync/WildfireSync/FeatureServer") }
  private var geodatabase: Geodatabase? = null
  private val selectedFeatures by lazy { ArrayList<Feature>() }

  private val activityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  private val mapView: MapView by lazy {
    activityMainBinding.mapView
  }

  private val syncButton: Button by lazy {
    activityMainBinding.syncButton
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activityMainBinding.root)

    // use local tile package for the base map
    val sanFranciscoTileCache =
      TileCache(getExternalFilesDir(null).toString() + "/SanFrancisco.tpkx")
    val tiledLayer = ArcGISTiledLayer(sanFranciscoTileCache)
    val map = ArcGISMap(Basemap(tiledLayer))

    mapView.apply {
      // set the map to the map view
      this.map = map

      // add a graphics overlay to the map view
      graphicsOverlays.add(graphicsOverlay)

      // add listener to handle motion events, which only responds once a geodatabase is loaded
      onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
          when (currentEditState) {
            EditState.READY -> selectFeaturesAt(
              android.graphics.Point(e.x.toInt(), e.y.toInt()),
              10.0
            )
            EditState.EDITING -> moveSelectedFeatureTo(mapPointFrom(e))
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
      when (currentEditState) {
        EditState.NOT_READY -> generateGeodatabase()
        EditState.READY -> syncGeodatabase()
        EditState.EDITING -> Log.e(TAG, "Unexpected edit state!")
      }
    }
  }

  /**
   * Creates a GenerateGeodatabaseJob and runs it.
   */
  private fun generateGeodatabase() {
    // create a geodatabase sync task and load it
    geodatabaseSyncTask.loadAsync()
    geodatabaseSyncTask.addDoneLoadingListener {
      // draw a box around the extent
      mapView.apply {
        // clear any previous operational layers and graphics
        map.operationalLayers.clear()
        graphicsOverlays[0].graphics.clear()
        // show the extent used as a graphic
        graphicsOverlays[0].graphics.add(
          Graphic(
            visibleArea.extent,
            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5f)
          )
        )
      }

      // create parameters for the job with the return attachments option set to false
      val parameters = geodatabaseSyncTask
        .createDefaultGenerateGeodatabaseParametersAsync(mapView.visibleArea.extent).get()
        .apply { isReturnAttachments = false }

      // create the generate geodatabase job
      val generateGeodatabaseJob =
        geodatabaseSyncTask.generateGeodatabase(
          parameters,
          externalCacheDir?.path + "/wildfire.geodatabase"
        )

      // show the job's progress in a dialog
      val progressDialogLayoutBinding = DialogLayoutBinding.inflate(layoutInflater)
      val generateGeodatabaseDialog = createProgressDialog(generateGeodatabaseJob)
      generateGeodatabaseDialog.setView(progressDialogLayoutBinding.root)

      generateGeodatabaseDialog.show()
      // define progress and done behaviours and start the job
      generateGeodatabaseJob.apply {
        // update progress
        addProgressChangedListener {
          progressDialogLayoutBinding.progressBar.progress = this.progress
          progressDialogLayoutBinding.progressTextView.text = "${this.progress}%"
        }
        // get geodatabase when done
        addJobDoneListener {
          // close the progress dialog
          generateGeodatabaseDialog.dismiss()
          // load the geodatabase and display its feature tables on the map
          loadGeodatabase(generateGeodatabaseJob)
          // set edit state to ready
          currentEditState = EditState.READY
        }
      }.start()
    }
  }

  /**
   * Loads the geodatabase from a GenerateGeodatabaseJob and displays its feature layers on the map.
   *
   * @param generateGeodatabaseJob the job which generated this geodatabase
   */
  private fun loadGeodatabase(generateGeodatabaseJob: GenerateGeodatabaseJob) {
    // return if the job failed
    if (generateGeodatabaseJob.status != Job.Status.SUCCEEDED) {
      val error =
        generateGeodatabaseJob.error?.message ?: "Unknown error generating geodatabase"
      Log.e(TAG, error)
      Toast.makeText(this, error, Toast.LENGTH_LONG).show()
      return
    }
    // if the job succeeded, load the resulting geodatabase
    geodatabase = generateGeodatabaseJob.result
    geodatabase?.let { geodatabase ->
      geodatabase.loadAsync()
      geodatabase.addDoneLoadingListener {
        // return if the geodatabase failed to load
        if (geodatabase.loadStatus != LoadStatus.LOADED) {
          val error = "Error loading geodatabase: " + geodatabase.loadError?.message
          Log.e(TAG, error)
          Toast.makeText(this, error, Toast.LENGTH_LONG).show()
          return@addDoneLoadingListener
        }

        // add all of the geodatabase feature tables to the map as feature layers
        val featureLayers =
          geodatabase.geodatabaseFeatureTables.map { featureTable ->
            FeatureLayer(
              featureTable
            )
          }
        mapView.map.operationalLayers.addAll(featureLayers)
        syncButton.isEnabled = false
      }
    }
  }

  /**
   * Syncs changes made on either the local or web service geodatabase with each other.
   */
  private fun syncGeodatabase() {
    // create parameters for the sync task
    val syncGeodatabaseParameters = SyncGeodatabaseParameters()
    syncGeodatabaseParameters.syncDirection =
      SyncGeodatabaseParameters.SyncDirection.BIDIRECTIONAL
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

      val progressDialogLayoutBinding = DialogLayoutBinding.inflate(layoutInflater)
      val syncDialog = createProgressDialog(syncGeodatabaseJob)
      syncDialog.setView(progressDialogLayoutBinding.root)
      syncDialog.show()

      syncGeodatabaseJob.apply {

        addProgressChangedListener {
          progressDialogLayoutBinding.progressBar.progress = this.progress
          progressDialogLayoutBinding.progressTextView.text = "${this.progress}%"
        }

        addJobDoneListener {
          if (syncGeodatabaseJob.status == Job.Status.SUCCEEDED) {
            // close the progress dialog
            syncDialog.dismiss()
            syncButton.isEnabled = false
            currentEditState = EditState.READY

            Toast.makeText(this@MainActivity, "Sync complete", Toast.LENGTH_SHORT)
              .show()
          } else {
            Log.e(TAG, "Database did not sync correctly!")
            Toast.makeText(
              this@MainActivity,
              "Database did not sync correctly!",
              Toast.LENGTH_LONG
            )
              .show()
          }
        }
      }
    }
  }

  /**
   * Create a progress dialog box for tracking the generate geodatabase job.
   *
   * @param job to be tracked
   * @return an AlertDialog set with the dialog layout view
   */
  private fun createProgressDialog(job: Job): AlertDialog {
    val builder = AlertDialog.Builder(this@MainActivity).apply {
      when (job) {
        is GenerateGeodatabaseJob -> setTitle("Generating geodatabase")
        is SyncGeodatabaseJob -> setTitle("Syncing geodatabase")
      }
      // provide a cancel button on the dialog
      setNegativeButton("Cancel") { _, _ -> job.cancel() }
      setCancelable(false)
      val dialogLayoutBinding = DialogLayoutBinding.inflate(layoutInflater)
      setView(dialogLayoutBinding.root)
    }
    return builder.create()
  }

  /**
   * Queries the features at the tapped point within a certain tolerance.
   *
   * @param point     contains an ArcGIS map point
   * @param tolerance distance from point within which features will be selected
   */
  private fun selectFeaturesAt(point: android.graphics.Point, tolerance: Double) {

    mapView.map.operationalLayers.filterIsInstance<FeatureLayer>().forEach { featureLayer ->
      val identifyLayerResultFuture =
        mapView.identifyLayerAsync(featureLayer, point, tolerance, false)
      identifyLayerResultFuture.addDoneListener {
        val identifyLayerResult = identifyLayerResultFuture.get()

        val identifiedFeatures = identifyLayerResult.elements.filterIsInstance<Feature>()

        featureLayer.selectFeatures(identifiedFeatures)
        selectedFeatures.addAll(identifiedFeatures)

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

    // clear the list of selected features
    selectedFeatures.clear()

    // clear selection indicator on the map view
    mapView.map.operationalLayers.filterIsInstance<FeatureLayer>().forEach {
      it.clearSelection()
    }

    currentEditState = EditState.READY
    syncButton.text = getString(R.string.sync_geodatabase)
    syncButton.isEnabled = true
  }

  /**
   * Converts motion event to an ArcGIS map point.
   *
   * @param motionEvent containing coordinates of an Android screen point
   * @return a corresponding map point in the place
   */
  private fun mapPointFrom(motionEvent: MotionEvent): Point {
    // get the screen point
    val screenPoint =
      android.graphics.Point(motionEvent.x.roundToInt(), motionEvent.y.roundToInt())
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
}
