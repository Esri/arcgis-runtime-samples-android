/*
 * Copyright 2020 Esri
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

package com.esri.arcgisruntime.sample.generateofflinemap

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapJob
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapParameters
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_layout.*
import java.io.File

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  private val graphicsOverlay: GraphicsOverlay by lazy {
    GraphicsOverlay().also {
      // add the graphics overlay to the map view when it is created
      mapView.graphicsOverlays.add(it)
    }
  }

  private val downloadArea: Graphic by lazy {
    Graphic().also {
      // create a symbol to show a box around the extent we want to download
      it.symbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2F)
      // add the graphic to the graphics overlay when it is created
      graphicsOverlay.graphics.add(it)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // handle authentication with the portal
    AuthenticationManager.setAuthenticationChallengeHandler(
      DefaultAuthenticationChallengeHandler(this)
    )

    // disable the button until the map is loaded
    takeMapOfflineButton.isEnabled = false

    // create a portal item with the itemId of the web map
    val portal = Portal(getString(R.string.portal_url), false)
    val portalItem = PortalItem(portal, getString(R.string.item_id))
    // create a map with the portal item
    val map = ArcGISMap(portalItem)

    map.addDoneLoadingListener {
      if (map.loadStatus == LoadStatus.LOADED) {
        // limit the map scale to the largest layer scale
        map.apply {
          maxScale = operationalLayers[6].maxScale
          minScale = operationalLayers[6].minScale
        }
      } else {
        val error = "Map failed to load: " + map.loadError.message
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
      }
    }

    // set the map to the map view
    mapView.map = map

    // update the download area box whenever the viewpoint changes
    mapView.addViewpointChangedListener {
      if (map.loadStatus == LoadStatus.LOADED) {
        // enable the map offline button only after the map is loaded
        if (!takeMapOfflineButton.isEnabled) takeMapOfflineButton.isEnabled = true
        // upper left corner of the area to take offline
        val minScreenPoint = Point(200, 200)
        // lower right corner of the downloaded area
        val maxScreenPoint = Point(
          mapView.width - 200,
          mapView.height - 200
        )
        // convert screen points to map points
        val minPoint = mapView.screenToLocation(minScreenPoint)
        val maxPoint = mapView.screenToLocation(maxScreenPoint)
        // use the points to define and return an envelope
        if (minPoint != null && maxPoint != null) {
          val envelope = Envelope(minPoint, maxPoint)
          downloadArea.geometry = envelope
        }
      }
    }

    // when the button is clicked, start the offline map task job
    takeMapOfflineButton.setOnClickListener { generateOfflineMap() }
  }

  /**
   * Use the generate offline map job to generate an offline map.
   */
  private fun generateOfflineMap() {
    // delete any offline map already in the cache
    val tempDirectoryPath: String =
      cacheDir.toString() + File.separator.toString() + "offlineMap"
    File(tempDirectoryPath).deleteRecursively()

    // specify the extent, min scale, and max scale as parameters
    var minScale: Double = mapView.mapScale
    val maxScale: Double = mapView.map.maxScale
    // minScale must always be larger than maxScale
    if (minScale <= maxScale) {
      minScale = maxScale + 1
    }

    val generateOfflineMapParameters = GenerateOfflineMapParameters(
      downloadArea.geometry, minScale, maxScale
    )
    // create an offline map offlineMapTask with the map
    val offlineMapTask = OfflineMapTask(mapView.map)

    // create an offline map job with the download directory path and parameters and start the job
    val offlineMapJob =
      offlineMapTask.generateOfflineMap(generateOfflineMapParameters, tempDirectoryPath)

    // create an alert dialog to show the download progress
    val progressDialog = createProgressDialog(offlineMapJob)
    progressDialog.show()

    offlineMapJob.apply {
      // link the progress bar to the job's progress
      addProgressChangedListener {
        progressDialog.progressBar.progress = progress
        progressDialog.progressTextView.text = "${progress}%"
      }

      // replace the current map with the result offline map when the job finishes
      addJobDoneListener {
        if (status == Job.Status.SUCCEEDED) {
          val result = result
          mapView.map = result.offlineMap
          graphicsOverlay.graphics.clear()

          // disable and remove the button to take the map offline once the offline map is showing
          takeMapOfflineButton.isEnabled = false
          takeMapOfflineButton.visibility = View.GONE

          Toast.makeText(this@MainActivity, "Now displaying offline map.", Toast.LENGTH_LONG).show()
        } else {
          val error =
            "Error in generate offline map job: " + offlineMapJob.error.additionalMessage
          Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
          Log.e(TAG, error)
        }
        // close the progress dialog
        progressDialog.dismiss()
      }
      // start the job
      start()
    }
  }

  /**
   * Create a progress dialog box for tracking the generate offline map job.
   *
   * @param job the generate offline map job progress to be tracked
   * @return an AlertDialog set with the dialog layout view
   */
  private fun createProgressDialog(job: GenerateOfflineMapJob): AlertDialog {
    val builder = AlertDialog.Builder(this@MainActivity).apply {
      setTitle("Generating offline map...")
      // provide a cancel button on the dialog
      setNeutralButton("Cancel") { _, _ ->
        job.cancel()
      }
      setCancelable(false)
      setView(
        LayoutInflater.from(this@MainActivity)
          .inflate(R.layout.dialog_layout, null)
      )
    }
    return builder.create()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    mapView.pause()
    // delete app cache when the app loses focus
    cacheDir.deleteRecursively()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
