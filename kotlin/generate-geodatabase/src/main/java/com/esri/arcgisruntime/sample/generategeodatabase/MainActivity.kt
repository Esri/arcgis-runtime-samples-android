/* Copyright 2020 Esri
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
package com.esri.arcgisruntime.sample.generategeodatabase

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.layers.AnnotationLayer
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseJob
import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_layout.*

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  // define the local path where the geodatabase will be stored
  private val localGeodatabasePath: String by lazy { externalCacheDir?.path + getString(R.string.wildfire_geodatabase) }

  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private val geodatabaseSyncTask: GeodatabaseSyncTask by lazy { GeodatabaseSyncTask("https://rt-server1081.esri.com/server/rest/services/Loudoun_Sample_2/FeatureServer") }
  private lateinit var geodatabase: Geodatabase

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    AuthenticationManager.setTrustAllSigners(true)

    // add the map and graphics overlay to the map view
    mapView.apply {
      // create a map with the tile package basemap
      map = ArcGISMap(Basemap.createOpenStreetMap())
      // create a graphics overlay to display the boundaries
      graphicsOverlays.add(GraphicsOverlay())
    }


    mapView.map.operationalLayers.add(AnnotationLayer(ServiceFeatureTable("https://rt-server1081.esri.com/server/rest/services/Loudoun_Sample_2/FeatureServer/0")))
    mapView.map.operationalLayers.get(0).addDoneLoadingListener {
      mapView.setViewpoint(Viewpoint(mapView.map.operationalLayers[0].fullExtent))
    }

  }



  /**
   * Creates a GenerateGeodatabaseJob and runs it.
   *
   * @param view the button which calls this function
   */
  fun generateGeodatabase(view: View) {
    // load the geodatabase sync task
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
        .apply { isReturnAttachments = true }

      // create the generate geodatabase job
      val generateGeodatabaseJob =
        geodatabaseSyncTask.generateGeodatabase(parameters, localGeodatabasePath)

      // show the job's progress in a dialog
      val dialog = createProgressDialog(generateGeodatabaseJob)
      dialog.show()
      // define progress and done behaviours and start the job
      generateGeodatabaseJob.apply {
        // update progress
        addProgressChangedListener {
          dialog.progressBar.progress = this.progress
          dialog.progressTextView.text = "${this.progress}%"
        }
        // get geodatabase when done
        addJobDoneListener {
          // close the progress dialog
          dialog.dismiss()
          // load the geodatabase and display its feature tables on the map
          loadGeodatabase(generateGeodatabaseJob, geodatabaseSyncTask)
        }
      }.start()
    }
  }

  /**
   * Loads the geodatabase from a GenerateGeodatabaseJob and displays its feature tables on the map.
   *
   * @param generateGeodatabaseJob the job which generated this geodatabase
   * @param geodatabaseSyncTask the GeodatabaseSyncTask which created the job
   */
  private fun loadGeodatabase(
    generateGeodatabaseJob: GenerateGeodatabaseJob,
    geodatabaseSyncTask: GeodatabaseSyncTask
  ) {
    // return if the job failed
    if (generateGeodatabaseJob.status != Job.Status.SUCCEEDED) {
      val error = generateGeodatabaseJob.error?.message ?: "Unknown error generating geodatabase"
      Log.e(TAG, error)
      Toast.makeText(this, error, Toast.LENGTH_LONG).show()
      return
    }
    // if the job succeeded, load the resulting geodatabase
    geodatabase = generateGeodatabaseJob.result
    geodatabase.loadAsync()
    geodatabase.addDoneLoadingListener {
      // return if the geodatabase failed to load
      if (geodatabase.loadStatus != LoadStatus.LOADED) {
        val error = "Error loading geodatabase: " + geodatabase.loadError.message
        Log.e(TAG, error)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        return@addDoneLoadingListener
      }
      // if the geodatabase loaded, hide the generate button
      genGeodatabaseButton.visibility = View.GONE
      // add all of the geodatabase feature tables to the map as feature layers
      val featureLayers =
        geodatabase.geodatabaseFeatureTables.map { featureTable -> FeatureLayer(featureTable) }
      mapView.map.operationalLayers.addAll(featureLayers)

      val message = "Local geodatabase stored at: $localGeodatabasePath"
      Log.i(TAG, message)
      Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    // unregister since we're not syncing
    geodatabaseSyncTask.unregisterGeodatabaseAsync(geodatabase).addDoneListener {
      val message = "Geodatabase unregistered since we wont be editing it in this sample."
      Log.i(TAG, message)
      Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
  }

  /**
   * Create a progress dialog box for tracking the generate geodatabase job.
   *
   * @param generateGeodatabaseJob the generate geodatabase job progress to be tracked
   * @return an AlertDialog set with the dialog layout view
   */
  private fun createProgressDialog(generateGeodatabaseJob: GenerateGeodatabaseJob): AlertDialog {
    val builder = AlertDialog.Builder(this@MainActivity).apply {
      setTitle(getString(R.string.progress_fetching))
      // provide a cancel button on the dialog
      setNeutralButton("Cancel") { _, _ ->
        generateGeodatabaseJob.cancel()
      }
      setCancelable(false)
      setView(
        LayoutInflater.from(this@MainActivity)
          .inflate(R.layout.dialog_layout, null)
      )
    }
    return builder.create()
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
