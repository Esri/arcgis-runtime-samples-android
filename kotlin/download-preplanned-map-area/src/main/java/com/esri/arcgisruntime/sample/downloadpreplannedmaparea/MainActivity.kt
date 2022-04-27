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
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.downloadpreplannedmaparea.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.downloadpreplannedmaparea.databinding.DialogLayoutBinding
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.tasks.offlinemap.DownloadPreplannedOfflineMapJob
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedMapArea
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedUpdateMode
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val offlineMapDirectory by lazy { File(externalCacheDir?.path + getString(R.string.preplanned_offline_map_dir)) }
    private var preplannedMapAreasAdapter: ArrayAdapter<String>? = null
    private val downloadedMapAreaNames by lazy { mutableListOf<String>() }
    private var downloadedMapAreasAdapter: ArrayAdapter<String>? = null
    private val downloadedMapAreas by lazy { mutableListOf<ArcGISMap>() }
    private var dialog: AlertDialog? = null

    private var selectedPreplannedMapArea: PreplannedMapArea? = null
    private var downloadPreplannedOfflineMapJob: DownloadPreplannedOfflineMapJob? = null

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val availableAreasListView: ListView by lazy {
        activityMainBinding.include.availableAreasListView
    }

    private val downloadedMapAreasListView: ListView by lazy {
        activityMainBinding.include.downloadedMapAreasListView
    }

    private val downloadButton: Button by lazy {
        activityMainBinding.include.downloadButton
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // delete any previous instances of downloaded maps
        externalCacheDir?.deleteRecursively()

        // create a temporary directory in the app's cache when required
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
        val portalItem = PortalItem(portal, getString(R.string.naperville_water_network_item_id))
        // create an offline map task from the portal item
        val offlineMapTask = OfflineMapTask(portalItem)
        // create a map with the portal item
        val onlineMap = ArcGISMap(portalItem)

        // create a red outline to mark the areas of interest of the preplanned map areas
        val areaOfInterestRenderer =
            SimpleRenderer(SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5.0f))

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
     * Download the selected preplanned map area from the list view to a temporary directory. The
     * download job is tracked in another list view.
     *
     * @param offlineMapTask used to take preplanned map areas offline
     */
    private fun downloadPreplannedArea(offlineMapTask: OfflineMapTask) {
        if (selectedPreplannedMapArea == null) {
            return
        }
        // create default download parameters from the offline map task
        val offlineMapParametersFuture =
            offlineMapTask.createDefaultDownloadPreplannedOfflineMapParametersAsync(
                selectedPreplannedMapArea
            )
        offlineMapParametersFuture.addDoneListener {
            try {
                // get the offline map parameters
                val offlineMapParameters = offlineMapParametersFuture.get().apply {
                    // set the update mode to not receive updates
                    updateMode = PreplannedUpdateMode.NO_UPDATES
                }
                // create a job to download the preplanned offline map to a temporary directory
                downloadPreplannedOfflineMapJob = offlineMapTask.downloadPreplannedOfflineMap(
                    offlineMapParameters,
                    offlineMapDirectory.path + File.separator + selectedPreplannedMapArea?.portalItem?.title
                ).also {
                    // create and update a progress dialog for the job
                    val progressDialogLayoutBinding = DialogLayoutBinding.inflate(layoutInflater)
                    dialog = createProgressDialog(it)
                    dialog?.setView(progressDialogLayoutBinding.root)
                    dialog?.show()
                    it.addProgressChangedListener {
                        progressDialogLayoutBinding.progressBar.progress = it.progress
                        progressDialogLayoutBinding.progressTextView.text = "${it.progress}%"
                    }
                    // start the job
                    it.start()
                }

                // when the job finishes
                downloadPreplannedOfflineMapJob?.addJobDoneListener {
                    // dismiss progress dialog
                    dialog?.dismiss()
                    // if there's a result from the download preplanned offline map job
                    if (downloadPreplannedOfflineMapJob?.status != Job.Status.SUCCEEDED) {
                        val error =
                            "Job finished with an error: " + downloadPreplannedOfflineMapJob?.error?.message
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        Log.e(TAG, error)
                        return@addJobDoneListener
                    }
                    downloadPreplannedOfflineMapJob?.result?.let { downloadPreplannedOfflineMapResult ->
                        if (downloadPreplannedOfflineMapResult.hasErrors()) {
                            // collect the layer and table errors into a single alert message
                            val stringBuilder = StringBuilder("Errors: ")
                            downloadPreplannedOfflineMapResult.layerErrors?.forEach { (key, value) ->
                                stringBuilder.append("Layer: ${key.name}. Exception: ${value.message}. ")
                            }
                            downloadPreplannedOfflineMapResult.tableErrors?.forEach { (key, value) ->
                                stringBuilder.append("Table: ${key.tableName}. Exception: ${value.message}. ")
                            }
                            val error =
                                "One or more errors occurred with the Offline Map Result: $stringBuilder"
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                            Log.e(TAG, error)
                            return@addJobDoneListener
                        }
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
                            downloadedMapAreasListView.setItemChecked(
                                downloadedMapAreaNames.size - 1,
                                true
                            )
                            downloadedMapAreasAdapter?.notifyDataSetChanged()
                            // de-select the area in the preplanned areas list view
                            availableAreasListView.clearChoices()
                            preplannedMapAreasAdapter?.notifyDataSetChanged()
                            // add the offline map to a list of downloaded map areas
                            downloadedMapAreas.add(offlineMap)
                            // disable the download button
                            downloadButton.isEnabled = false
                        }
                    }
                }
            } catch (e: Exception) {
                val error =
                    "Failed to generate default parameters for the download job: " + e.cause?.message
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                Log.e(TAG, error)
            }
        }
    }

    /**
     * Creates a list view showing available preplanned map areas. Graphics are drawn on the map view
     * showing the preplanned map areas available. Tapping on a preplanned map area in the list view
     * will set the map view's viewpoint to the area and, if a map has not yet been downloaded for
     * this area, the download button will be enabled.
     *
     * @param onlineMap used as the background for showing available preplanned map areas
     * @param offlineMapTask used to take preplanned map areas offline
     */
    private fun createPreplannedAreasListView(
        onlineMap: ArcGISMap,
        offlineMapTask: OfflineMapTask
    ) {
        var preplannedMapAreas: List<PreplannedMapArea>
        val preplannedMapAreaNames: MutableList<String> = ArrayList()
        preplannedMapAreasAdapter =
            ArrayAdapter(this, R.layout.item_map_area, preplannedMapAreaNames)
        availableAreasListView.adapter = preplannedMapAreasAdapter
        // get the preplanned map areas from the offline map task and show them in the list view
        val preplannedMapAreasFuture =
            offlineMapTask.preplannedMapAreasAsync
        preplannedMapAreasFuture.addDoneListener {
            try {
                // get the preplanned areas
                preplannedMapAreas = preplannedMapAreasFuture.get().onEach { preplannedMapArea ->
                    // add the preplanned map area name to the list view
                    preplannedMapAreaNames.add(preplannedMapArea.portalItem.title)
                    // load each area and show a red border around their area of interest
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
                // notify the adapter that the list of preplanned map area names has changed
                preplannedMapAreasAdapter?.notifyDataSetChanged()
                // on list view click
                availableAreasListView.onItemClickListener =
                    AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                        selectedPreplannedMapArea = preplannedMapAreas[i]
                        if (selectedPreplannedMapArea == null) {
                            downloadButton.isEnabled = false
                            return@OnItemClickListener
                        }
                        // clear the download jobs list view selection
                        downloadedMapAreasListView.clearChoices()
                        downloadedMapAreasAdapter?.notifyDataSetChanged()

                        val areaOfInterest =
                            GeometryEngine.buffer(
                                selectedPreplannedMapArea?.areaOfInterest,
                                50.0
                            ).extent
                        // show the online map with the areas of interest
                        mapView.apply {
                            map = onlineMap
                            graphicsOverlays[0].isVisible = true
                            // set the viewpoint to the preplanned map area's area of interest
                            setViewpointAsync(Viewpoint(areaOfInterest), 1.5f)
                        }
                        // enable download button only for those map areas which have not been downloaded already
                        File(
                            externalCacheDir?.path + getString(R.string.preplanned_offline_map_dir) +
                                File.separator + selectedPreplannedMapArea?.portalItem?.title
                        ).also {
                            downloadButton.isEnabled = !it.exists()
                        }
                    }
            } catch (e: Exception) {
                val error = "Failed to get the preplanned map areas from the offline map task."
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                Log.e(TAG, error)
            }
        }
    }

    /**
     * Create a list view which holds downloaded map areas.
     *
     */
    private fun createDownloadAreasListView() {
        downloadedMapAreasAdapter =
            ArrayAdapter(this, R.layout.item_map_area, downloadedMapAreaNames)
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
     * @param downloadPreplannedOfflineMapJob  to be tracked
     * @return an AlertDialog set with the dialog layout view
     */
    private fun createProgressDialog(downloadPreplannedOfflineMapJob: DownloadPreplannedOfflineMapJob): AlertDialog {
        val dialogBuilder = AlertDialog.Builder(this@MainActivity).apply {
            setTitle("Download preplanned offline map")
            // provide a cancel button on the dialog
            setNegativeButton("Cancel") { _, _ ->
                downloadPreplannedOfflineMapJob.cancel()
            }
            setCancelable(false)
            val dialogLayoutBinding = DialogLayoutBinding.inflate(layoutInflater)
            setView(dialogLayoutBinding.root)
        }
        return dialogBuilder.create()
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
