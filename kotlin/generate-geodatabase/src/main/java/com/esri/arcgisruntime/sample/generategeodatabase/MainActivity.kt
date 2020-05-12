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
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // use local tile package for the base map
    val sanFrancisco =
      TileCache(getExternalFilesDir(null).toString() + getString(R.string.san_francisco_tpk))
    val tiledLayer = ArcGISTiledLayer(sanFrancisco)

    // add the map and graphics overlay to the map view
    mapView.apply {
      // create a map with the tile package basemap
      this.map = ArcGISMap(Basemap(tiledLayer))
      // create a graphics overlay to display the boundaries
      graphicsOverlays.add(GraphicsOverlay())
    }

    // generate geodatabase when the button is clicked
    genGeodatabaseButton.setOnClickListener {
      generateAndDisplayGeodatabase()
    }
  }

  /**
   * Creates a GenerateGeodatabaseJob and displays its results on the map.
   */
  private fun generateAndDisplayGeodatabase() {
    // create a geodatabase sync task and load it
    val geodatabaseSyncTask =
      GeodatabaseSyncTask(getString(R.string.wildfire_sync)).apply { loadAsync() }

    geodatabaseSyncTask.addDoneLoadingListener onTaskLoaded@{
      // show the progress layout
      taskProgressBar.progress = 0
      progressLayout.visibility = View.VISIBLE

      mapView.apply {
        // clear any previous operational layers and graphics
        map.operationalLayers.clear()
        graphicsOverlays[0].graphics.clear()
        // show the extent used as a graphic
        mapView.graphicsOverlays[0].graphics.add(
          Graphic(
            visibleArea.extent,
            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5f)
          )
        )
      }

      val parameters = geodatabaseSyncTask
        .createDefaultGenerateGeodatabaseParametersAsync(mapView.visibleArea.extent).get()
        .apply { isReturnAttachments = false }
      // define the local path where the geodatabase will be stored
      val localGeodatabasePath =
        cacheDir.toString() + File.separator + getString(R.string.wildfire_geodatabase)

      // notify the user the the job's progress has started
      progressTextView.text = getString(R.string.progress_started)
      // create the generate geodatabase job
      geodatabaseSyncTask.generateGeodatabase(parameters, localGeodatabasePath).apply {
        // start the job
        start()
        // update progress
        addProgressChangedListener {
          taskProgressBar.progress = progress
          progressTextView.text = getString(R.string.progress_fetching)
        }
        // get geodatabase when done
        addJobDoneListener {
          // hide the progress dialog
          progressLayout.visibility = View.INVISIBLE
          // return if the job failed
          if (status != Job.Status.SUCCEEDED) {
            val errorMessage = error?.message ?: "Unknown error generating geodatabase"
            Log.e(TAG, errorMessage)
            Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
            return@addJobDoneListener
          }
          // if the job succeeded, load the resulting geodatabase
          val geodatabase = result.apply { loadAsync() }
          geodatabase.addDoneLoadingListener {
            // return if the geodatabase failed to load
            if (geodatabase.loadStatus != LoadStatus.LOADED) {
              val errorMessage = "Error loading geodatabase: " + geodatabase.loadError.message
              Log.e(TAG, errorMessage)
              Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
              return@addDoneLoadingListener
            }
            // if the geodatabase loaded, set the progress text to done and hide the generate button
            progressTextView.text = getString(R.string.progress_done)
            genGeodatabaseButton.visibility = View.GONE
            // add all of the geodatabase feature tables to the map as feature layers
            val featureLayers =
              geodatabase.geodatabaseFeatureTables.map { featureTable -> FeatureLayer(featureTable) }
            mapView.map.operationalLayers.addAll(featureLayers)

            Log.i(TAG, "Local geodatabase stored at: $localGeodatabasePath")
          }
          // unregister since we're not syncing
          geodatabaseSyncTask.unregisterGeodatabaseAsync(geodatabase).addDoneListener {
            val message = "Geodatabase unregistered since we wont be editing it in this sample."
            Log.i(TAG, message)
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
          }
        }
      }
    }
  }

  override fun onPause() {
    super.onPause()
    mapView.pause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.dispose()
  }
}