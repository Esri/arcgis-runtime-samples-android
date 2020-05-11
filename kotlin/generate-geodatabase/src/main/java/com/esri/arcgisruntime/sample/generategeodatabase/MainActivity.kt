/* Copyright 2017 Esri
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
import com.esri.arcgisruntime.concurrent.ListenableFuture
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
  private val TAG =
    MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // use local tile package for the base map
    val sanFrancisco =
      TileCache(getExternalFilesDir(null).toString() + getString(R.string.san_francisco_tpk))
    val tiledLayer = ArcGISTiledLayer(sanFrancisco)
    // create a map with the tile package basemap
    val map = ArcGISMap(Basemap(tiledLayer))
    // create a graphics overlay and symbol to mark the extent
    val graphicsOverlay = GraphicsOverlay()
    val boundarySymbol =
      SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5f)
    // add the map and graphics overlay to the map view
    mapView.apply {
      this.map = map
      graphicsOverlays.add(graphicsOverlay)
    }
    // create a geodatabase sync task
    val geodatabaseSyncTask =
      GeodatabaseSyncTask(getString(R.string.wildfire_sync))
    geodatabaseSyncTask.loadAsync()
    geodatabaseSyncTask.addDoneLoadingListener {
      // generate the geodatabase sync task
      genGeodatabaseButton.setOnClickListener {
        // show the progress layout
        taskProgressBar.progress = 0
        progressLayout.visibility = View.VISIBLE
        // clear any previous operational layers and graphics if button clicked more than once
        map.operationalLayers.clear()
        graphicsOverlay.graphics.clear()
        // show the extent used as a graphic
        val extent =
          mapView.visibleArea.extent
        val boundary = Graphic(extent, boundarySymbol)
        graphicsOverlay.graphics.add(boundary)
        val parameters =
          geodatabaseSyncTask.createDefaultGenerateGeodatabaseParametersAsync(extent).get()
        parameters.isReturnAttachments = false
        // define the local path where the geodatabase will be stored
        val localGeodatabasePath =
          cacheDir.toString() + File.separator + getString(R.string.wildfire_geodatabase)
        // create and start the job
        val generateGeodatabaseJob = geodatabaseSyncTask
          .generateGeodatabase(parameters, localGeodatabasePath)
        generateGeodatabaseJob.apply {
          start().also { progressTextView.text = getString(R.string.progress_started) }
          // update progress
          addProgressChangedListener {
            taskProgressBar.progress = progress
            progressTextView.text = getString(R.string.progress_fetching)
          }
          // get geodatabase when done
          addJobDoneListener {
            progressLayout.visibility = View.INVISIBLE
            if (status == Job.Status.SUCCEEDED) {
              val geodatabase = result.apply { loadAsync() }
              geodatabase.addDoneLoadingListener {
                if (geodatabase.loadStatus == LoadStatus.LOADED) {
                  progressTextView.text = getString(R.string.progress_done)
                  for (geodatabaseFeatureTable in geodatabase.geodatabaseFeatureTables) {
                    map.operationalLayers.add(FeatureLayer(geodatabaseFeatureTable))
                  }
                  genGeodatabaseButton.visibility = View.GONE
                  Log.i(TAG, "Local geodatabase stored at: $localGeodatabasePath")
                } else {
                  val error = "Error loading geodatabase: " + geodatabase.loadError.message
                  Log.e(TAG, error)
                  Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                }
              }
              // unregister since we're not syncing
              val unregisterGeodatabase: ListenableFuture<*> =
                geodatabaseSyncTask.unregisterGeodatabaseAsync(geodatabase)
              unregisterGeodatabase.addDoneListener {
                val message =
                  "Geodatabase unregistered since we wont be editing it in this sample."
                Log.i(TAG, message)
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
              }
            } else {
              val error =
                generateGeodatabaseJob.error?.message ?: "Unknown error generating geodatabase"
              Log.e(TAG, error)
              Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
            }
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