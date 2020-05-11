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
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
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
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask
import java.io.File
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {
  private val TAG =
    MainActivity::class.java.simpleName
  private var mMapView: MapView? = null
  private var mProgressTextView: TextView? = null
  private var mProgressLayout: RelativeLayout? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // use local tile package for the base map
    val sanFrancisco =
      TileCache(getExternalFilesDir(null).toString() + getString(R.string.san_francisco_tpk))
    val tiledLayer = ArcGISTiledLayer(sanFrancisco)
    // create a map view and add a map
    mMapView =
      findViewById<View>(R.id.mapView) as MapView
    val map = ArcGISMap(Basemap(tiledLayer))
    mMapView!!.map = map
    // create a graphics overlay and symbol to mark the extent
    val graphicsOverlay = GraphicsOverlay()
    mMapView!!.graphicsOverlays.add(graphicsOverlay)
    val boundarySymbol =
      SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5)
    // inflate button and progress layout
    val genGeodatabaseButton =
      findViewById<View>(R.id.genGeodatabaseButton) as Button
    mProgressLayout = findViewById<View>(R.id.progressLayout) as RelativeLayout
    val progressBar =
      findViewById<View>(R.id.taskProgressBar) as ProgressBar
    mProgressTextView = findViewById<View>(R.id.progressTextView) as TextView
    // create a geodatabase sync task
    val geodatabaseSyncTask =
      GeodatabaseSyncTask(getString(R.string.wildfire_sync))
    geodatabaseSyncTask.loadAsync()
    geodatabaseSyncTask.addDoneLoadingListener {
      // generate the geodatabase sync task
      genGeodatabaseButton.setOnClickListener {
        // show the progress layout
        progressBar.progress = 0
        mProgressLayout!!.visibility = View.VISIBLE
        // clear any previous operational layers and graphics if button clicked more than once
        map.operationalLayers.clear()
        graphicsOverlay.graphics.clear()
        // show the extent used as a graphic
        val extent =
          mMapView!!.visibleArea.extent
        val boundary = Graphic(extent, boundarySymbol)
        graphicsOverlay.graphics.add(boundary)
        // create generate geodatabase parameters for the current extent
        val defaultParameters =
          geodatabaseSyncTask
            .createDefaultGenerateGeodatabaseParametersAsync(extent)
        defaultParameters.addDoneListener {
          try { // set parameters and don't include attachments
            val parameters = defaultParameters.get()
            parameters.isReturnAttachments = false
            // define the local path where the geodatabase will be stored
            val localGeodatabasePath =
              cacheDir.toString() + File.separator + getString(R.string.wildfire_geodatabase)
            // create and start the job
            val generateGeodatabaseJob = geodatabaseSyncTask
              .generateGeodatabase(parameters, localGeodatabasePath)
            generateGeodatabaseJob.start()
            mProgressTextView!!.text = getString(R.string.progress_started)
            // update progress
            generateGeodatabaseJob.addProgressChangedListener {
              progressBar.progress = generateGeodatabaseJob.progress
              mProgressTextView!!.text = getString(R.string.progress_fetching)
            }
            // get geodatabase when done
            generateGeodatabaseJob.addJobDoneListener {
              mProgressLayout!!.visibility = View.INVISIBLE
              if (generateGeodatabaseJob.status == Job.Status.SUCCEEDED) {
                val geodatabase = generateGeodatabaseJob.result
                geodatabase.loadAsync()
                geodatabase.addDoneLoadingListener {
                  if (geodatabase.loadStatus == LoadStatus.LOADED) {
                    mProgressTextView!!.text = getString(R.string.progress_done)
                    for (geodatabaseFeatureTable in geodatabase
                      .geodatabaseFeatureTables) {
                      geodatabaseFeatureTable.loadAsync()
                      map.operationalLayers.add(FeatureLayer(geodatabaseFeatureTable))
                    }
                    genGeodatabaseButton.visibility = View.GONE
                    Log.i(
                      TAG,
                      "Local geodatabase stored at: $localGeodatabasePath"
                    )
                  } else {
                    Log.e(
                      TAG,
                      "Error loading geodatabase: " + geodatabase.loadError.message
                    )
                  }
                }
                // unregister since we're not syncing
                val unregisterGeodatabase: ListenableFuture<*> = geodatabaseSyncTask
                  .unregisterGeodatabaseAsync(geodatabase)
                unregisterGeodatabase.addDoneListener {
                  Log.i(
                    TAG,
                    "Geodatabase unregistered since we wont be editing it in this sample."
                  )
                  Toast.makeText(
                    this@MainActivity,
                    "Geodatabase unregistered since we wont be editing it in this sample.",
                    Toast.LENGTH_LONG
                  ).show()
                }
              } else if (generateGeodatabaseJob.error != null) {
                Log.e(
                  TAG,
                  "Error generating geodatabase: " + generateGeodatabaseJob.error.message
                )
              } else {
                Log.e(TAG, "Unknown Error generating geodatabase")
              }
            }
          } catch (e: InterruptedException) {
            Log.e(TAG, "Error generating geodatabase parameters : " + e.message)
          } catch (e: ExecutionException) {
            Log.e(TAG, "Error generating geodatabase parameters : " + e.message)
          }
        }
      }
    }
  }

  override fun onPause() {
    super.onPause()
    mMapView!!.pause()
  }

  override fun onResume() {
    super.onResume()
    mMapView!!.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mMapView!!.dispose()
  }
}