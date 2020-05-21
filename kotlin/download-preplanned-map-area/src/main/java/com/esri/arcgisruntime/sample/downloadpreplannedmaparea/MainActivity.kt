/*
 *  Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.downloadpreplannedmaparea

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.downloadpreplannedmaparea.ProgressDialogFragment.OnProgressDialogDismissListener
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseJob
import com.esri.arcgisruntime.tasks.offlinemap.DownloadPreplannedOfflineMapJob
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedMapArea
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedUpdateMode
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_offline_controls.*
import java.io.File
import java.util.ArrayList

class MainActivity : AppCompatActivity(), OnProgressDialogDismissListener {

  private val offlineMapDirectory by lazy {
    File(externalCacheDir?.path + getString(R.string.preplanned_offline_map_dir))
  }
  private var preplannedMapAreasAdapter: ArrayAdapter<String>? = null
  private val downloadedMapAreaNames by lazy { mutableListOf<String>() }
  private var downloadedMapAreasAdapter: ArrayAdapter<String>? = null
  private val downloadedMapAreas by lazy { mutableListOf<ArcGISMap>() }

  private var mSelectedPreplannedMapArea: PreplannedMapArea? = null
  private var downloadPreplannedOfflineMapJob: DownloadPreplannedOfflineMapJob? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // delete any previous instances of downloaded maps
    externalCacheDir?.deleteRecursively()

    // create up a temporary directory in the app's cache when required
    offlineMapDirectory.also {
      when {
        it.mkdirs() -> Log.i(TAG, "Created directory for offline map in " + it.path)
        it.exists() -> Log.i(TAG, "Offline map directory already exists at " + it.path)
        else -> Log.e(TAG, "Error creating offline map directory at: " + it.path)
      }
    }

    // set the authentication manager to handle challenges when accessing the portal
    // Note: The sample data is publicly available, so you shouldn't be challenged
    AuthenticationManager.setAuthenticationChallengeHandler(
      DefaultAuthenticationChallengeHandler(this)
    )

    // create a portal to ArcGIS Online
    val portal = Portal(getString(R.string.arcgis_online_url))
    // create a portal item using the portal and the item id of a map service
    val portalItem = PortalItem(portal, getString(R.string.naperville_water_network_url))
    // create an offline map task from the portal item
    val offlineMapTask = OfflineMapTask(portalItem)
    // create a map with the portal item
    val onlineMap = ArcGISMap(portalItem)

    // create a red outline to mark the areas of interest of the preplanned map areas
    val areaOfInterestRenderer = SimpleRenderer().apply {
      symbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5.0f)
    }
    // create a graphics overlay to show the preplanned map areas extents (areas of interest)
    val areasOfInterestGraphicsOverlay = GraphicsOverlay().apply {
      renderer = areaOfInterestRenderer
    }

    mapView.apply {
      // add the online map to the map view
      map = onlineMap
      // add the area of interest overlay
      graphicsOverlays.add(areasOfInterestGraphicsOverlay)
    }

    createPreplannedAreasListView(onlineMap, offlineMapTask)
    createDownloadAreasListView()

    // create download button
    downloadButton.apply {
      isEnabled = false
      setOnClickListener { downloadPreplannedArea(offlineMapTask) }
    }
  }

  /**
   * Download the selected preplanned map area from the list view to a temporary directory. The download job is tracked in another list view.
   */
  private fun downloadPreplannedArea(offlineMapTask: OfflineMapTask) {
    if (mSelectedPreplannedMapArea != null) {
      // create default download parameters from the offline map task
      val offlineMapParametersFuture =
        offlineMapTask.createDefaultDownloadPreplannedOfflineMapParametersAsync(
          mSelectedPreplannedMapArea
        )
      offlineMapParametersFuture?.addDoneListener {
        try {
          // get the offline map parameters
          val offlineMapParameters = offlineMapParametersFuture.get()
          // set the update mode to not receive updates
          offlineMapParameters.updateMode = PreplannedUpdateMode.NO_UPDATES
          // create a job to download the preplanned offline map to a temporary directory
          downloadPreplannedOfflineMapJob = offlineMapTask.downloadPreplannedOfflineMap(
            offlineMapParameters,
            offlineMapDirectory.path + File.separator + mSelectedPreplannedMapArea?.portalItem?.title
          )
          // start the job
          downloadPreplannedOfflineMapJob?.start()

          // show progress dialog for download, includes tracking progress
          createProgressDialog(downloadPreplannedOfflineMapJob)

          // when the job finishes
          downloadPreplannedOfflineMapJob?.addJobDoneListener {
            // dismiss progress dialog
            findProgressDialogFragment()?.dismiss()
            // if there's a result from the download preplanned offline map job
            if (downloadPreplannedOfflineMapJob?.status == Job.Status.SUCCEEDED) {
              downloadPreplannedOfflineMapJob?.result?.let { downloadPreplannedOfflineMapResult ->
                if (downloadPreplannedOfflineMapJob != null && !downloadPreplannedOfflineMapResult.hasErrors()) {
                  // get the offline map
                  downloadPreplannedOfflineMapResult.offlineMap?.let { offlineMap ->
                    mapView.apply {
                      // add it to the map view
                      map = offlineMap
                      // hide the area of interest graphics
                      graphicsOverlays[0].isVisible = false
                    }
                    // add the map name to the list view of downloaded map areas
                    downloadedMapAreaNames.add(offlineMap.item.title)
                    // select the downloaded map area
                    downloadedMapAreasListView.setItemChecked(downloadedMapAreaNames.size - 1, true)
                    downloadedMapAreasAdapter?.notifyDataSetChanged()
                    // de-select the area in the preplanned areas list view
                    availableAreasListView.clearChoices()
                    preplannedMapAreasAdapter?.notifyDataSetChanged()
                    // add the offline map to a list of downloaded map areas
                    downloadedMapAreas.add(offlineMap)
                    // disable the download button
                    downloadButton.isEnabled = false
                  }
                } else {
                  // collect the layer and table errors into a single alert message
                  val stringBuilder = StringBuilder("Errors: ")
                  downloadPreplannedOfflineMapResult.layerErrors?.forEach { (key, value) ->
                    stringBuilder.append("Layer: ").append(key.name).append(". Exception: ")
                      .append(value.message).append(". ")
                  }
                  downloadPreplannedOfflineMapResult.tableErrors?.forEach { (key, value) ->
                    stringBuilder.append("Table: ").append(key.tableName).append(". Exception: ")
                      .append(value.message).append(". ")
                  }
                  val error =
                    "One or more errors occurred with the Offline Map Result: $stringBuilder"
                  Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                  Log.e(TAG, error)
                }
              }
            } else {
              val error = "Job finished with an error: " + downloadPreplannedOfflineMapJob?.error
              Toast.makeText(this, error, Toast.LENGTH_LONG).show()
              Log.e(TAG, error)
            }
          }
        } catch (e: Exception) {
          val error =
            "Failed to generate default parameters for the download job: " + e.cause!!.message
          Toast.makeText(this, error, Toast.LENGTH_LONG).show()
          Log.e(
            TAG,
            error
          )
        }
      }
    }
  }

  private fun createPreplannedAreasListView(onlineMap: ArcGISMap, offlineMapTask: OfflineMapTask) {
    var preplannedMapAreas: List<PreplannedMapArea>
    val preplannedMapAreaNames: MutableList<String> = ArrayList()
    preplannedMapAreasAdapter = ArrayAdapter(this, R.layout.item_map_area, preplannedMapAreaNames)
    availableAreasListView?.adapter = preplannedMapAreasAdapter
    // get the preplanned map areas from the offline map task and show them in the list view
    val preplannedMapAreasFuture =
      offlineMapTask.preplannedMapAreasAsync
    preplannedMapAreasFuture.addDoneListener {
      try {
        // get the preplanned areas and add them to the list view
        preplannedMapAreas = preplannedMapAreasFuture.get()
        preplannedMapAreas.forEach { preplannedMapArea ->
          preplannedMapAreaNames.add(preplannedMapArea.portalItem.title)
        }
        preplannedMapAreasAdapter?.notifyDataSetChanged()
        // load each area and show a red border around their area of interest
        preplannedMapAreas.forEach { preplannedMapArea ->
          preplannedMapArea.loadAsync()
          preplannedMapArea.addDoneLoadingListener {
            if (preplannedMapArea.loadStatus == LoadStatus.LOADED) {
              // add the area of interest as a graphic
              mapView.graphicsOverlays[0].graphics.add(Graphic(preplannedMapArea.areaOfInterest))
            } else {
              val error =
                "Failed to load preplanned map area: " + preplannedMapArea.loadError.message
              Toast.makeText(this, error, Toast.LENGTH_LONG).show()
              Log.e(TAG, error)
            }
          }
        }
        // on list view click
        availableAreasListView.onItemClickListener =
          AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, l: Long ->
            mSelectedPreplannedMapArea = preplannedMapAreas[i]
            if (mSelectedPreplannedMapArea != null) {
              // clear the download jobs list view selection
              downloadedMapAreasListView.clearChoices()
              downloadedMapAreasAdapter?.notifyDataSetChanged()

              val areaOfInterest =
                GeometryEngine.buffer(mSelectedPreplannedMapArea?.areaOfInterest, 50.0).extent
              // show the online map with the areas of interest
              mapView.apply {
                map = onlineMap
                graphicsOverlays[0].isVisible = true
                // set the viewpoint to the preplanned map area's area of interest
                setViewpointAsync(Viewpoint(areaOfInterest), 1.5f)
              }
              // enable download button only for those map areas which have not been downloaded already
              File(externalCacheDir?.path + getString(R.string.preplanned_offline_map_dir)
                  + File.separator + mSelectedPreplannedMapArea?.portalItem?.title).let {
                downloadButton.isEnabled = !it.exists()
              }
            } else {
              downloadButton.isEnabled = false
            }
          }
      } catch (e: Exception) {
        val error = "Failed to get the Preplanned Map Areas from the Offline Map Task."
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
      }
    }
  }

  private fun createDownloadAreasListView() {
    // create a list view which holds downloaded map areas
    downloadedMapAreasAdapter = ArrayAdapter(this, R.layout.item_map_area, downloadedMapAreaNames)
    downloadedMapAreasListView.apply {
      adapter = downloadedMapAreasAdapter
      onItemClickListener =
        AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
          mapView.apply {
            // set the downloaded map to the map view
            map = downloadedMapAreas[i]
            // hide the graphics overlays
            graphicsOverlays[0].isVisible = false
          }
          // disable the download button
          downloadButton.isEnabled = false
          // clear the available map areas list view selection
          availableAreasListView.clearChoices()
          preplannedMapAreasAdapter?.notifyDataSetChanged()
        }
    }
  }

  /**
   * Create a progress dialog box for tracking the generate geodatabase job.
   *
   * @param generateGeodatabaseJob the generate geodatabase job progress to be tracked
   * @return an AlertDialog set with the dialog layout view
   */
  private fun createProgressDialog(downloadPreplannedOfflineMapJob: DownloadPreplannedOfflineMapJob): AlertDialog {
    val builder = AlertDialog.Builder(this@MainActivity).apply {
      setTitle("Download preplanned offline map job")
      // provide a cancel button on the dialog
      setNegativeButton("Cancel") { _, _ ->
        downloadPreplannedOfflineMapJob.cancel()
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

  companion object {
    private val TAG =
      MainActivity::class.java.simpleName
  }
}

