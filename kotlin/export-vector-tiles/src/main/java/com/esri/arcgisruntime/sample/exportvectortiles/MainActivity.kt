/* Copyright 2022 Esri
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

package com.esri.arcgisruntime.sample.exportvectortiles

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.exportvectortiles.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.exportvectortiles.databinding.ProgressDialogLayoutBinding
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.vectortilecache.ExportVectorTilesJob
import com.esri.arcgisruntime.tasks.vectortilecache.ExportVectorTilesParameters
import com.esri.arcgisruntime.tasks.vectortilecache.ExportVectorTilesResult
import com.esri.arcgisruntime.tasks.vectortilecache.ExportVectorTilesTask
import java.io.File


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private var downloadArea: Graphic? = null
    private var exportVectorTilesJob: ExportVectorTilesJob? = null
    private var dialog: AlertDialog? = null
    private var isJobFinished: Boolean = true

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val mapPreviewLayout: ConstraintLayout by lazy {
        activityMainBinding.mapPreviewLayout
    }

    private val exportVectorTilesButton: Button by lazy {
        activityMainBinding.exportVectorTilesButton
    }

    private val previewMapView: MapView by lazy {
        activityMainBinding.previewMapView
    }

    private val dimBackground: View by lazy {
        activityMainBinding.dimBackground
    }

    private val closeButton: Button by lazy {
        activityMainBinding.closeButton
    }

    private val previewTextView: TextView by lazy {
        activityMainBinding.previewTextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)


        // create a graphic to show a red outline square around the vector tiles to be downloaded
        downloadArea = Graphic().apply {
            symbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2F)
        }
        // create a graphics overlay and add the downloadArea graphic
        val graphicsOverlay = GraphicsOverlay().apply {
            graphics.add(downloadArea)
        }

        mapView.apply {
            // set the map to BasemapType navigation night
            map = ArcGISMap(BasemapStyle.ARCGIS_STREETS_NIGHT)
            // disable rotation
            rotation = 0F
            // set the viewpoint of the sample to ESRI Redlands, CA campus
            setViewpoint(Viewpoint(34.056295, -117.195800, 100000.0))
            // add the graphics overlay to the MapView
            graphicsOverlays.add(graphicsOverlay)

            // update the red square whenever the viewpoint changes
            addViewpointChangedListener {
                updateDownloadAreaGeometry()
            }

            // when the map has loaded, create a vector tiled layer from the basemap and export the tiles
            map.addDoneLoadingListener {
                if (map.loadStatus == LoadStatus.LOADED) {
                    // check that the layer from the basemap is a vector tiled layer
                    val vectorTiledLayer = map.basemap.baseLayers[0] as ArcGISVectorTiledLayer
                    handleExportButton(vectorTiledLayer)
                } else {
                    showError(map.loadError.message.toString())
                }
            }
        }
    }

    /**
     * Sets up the ExportVectorTilesTask using the [vectorTiledLayer]
     * on export button click. Then call handleExportVectorTilesJob()
     */
    private fun handleExportButton(vectorTiledLayer: ArcGISVectorTiledLayer) {
        exportVectorTilesButton.setOnClickListener {
            // update the download area's geometry using the current viewpoint
            updateDownloadAreaGeometry()
            // create a new export vector tiles task
            val exportVectorTilesTask = ExportVectorTilesTask(vectorTiledLayer.uri)
            // the max scale parameter is set to 10% of the map's scale so the
            // number of tiles exported are within the vector tiled layer's max tile export limit
            val exportVectorTilesParametersFuture = exportVectorTilesTask
                .createDefaultExportVectorTilesParametersAsync(
                    downloadArea?.geometry,
                    mapView.mapScale * 0.1
                )

            exportVectorTilesParametersFuture.addDoneListener {
                try {
                    // get the loaded export vector tile parameters
                    val exportVectorTilesParameters =
                        exportVectorTilesParametersFuture.get()
                    // only start a new job, if the previous job has finished cancelling
                    if(isJobFinished){
                        // create a job to export vector tiles
                        handleExportVectorTilesJob(
                            exportVectorTilesParameters,
                            exportVectorTilesTask
                        )
                    } else {
                        showError("Previous job is cancelling asynchronously")
                    }
                } catch (e: Exception) {
                    showError(e.message.toString())
                }
            }
        }
    }

    /**
     * Start the export vector tiles job using [exportVectorTilesTask] and the
     * [exportVectorTilesParameters]. The vector tile package is exported as "file.vtpk"
     */
    private fun handleExportVectorTilesJob(
        exportVectorTilesParameters: ExportVectorTilesParameters,
        exportVectorTilesTask: ExportVectorTilesTask
    ) {
        // create a .vtpk and directory in the app's cache for saving exported tiles
        val vtpkFile = File(externalCacheDir, "/StyleItemResources/myVectorTiles.vtpk")
        val resDir = File(externalCacheDir, "/StyleItemResources")
        resDir.deleteRecursively()
        resDir.mkdir()

        // create a job with the export vector tile parameters
        // and exports the vector tile package as "file.vtpk"
        exportVectorTilesJob = exportVectorTilesTask.exportVectorTiles(
            exportVectorTilesParameters,
            vtpkFile.absolutePath, resDir.absolutePath
        ).apply {
            // inflate the progress dialog
            val dialogLayoutBinding = createProgressDialog()
            // start the export vector tile cache job
            start()
            // since job is now started, set to false
            isJobFinished = false

            // display the progress dialog
            dialog?.show()
            // update the dialog using the progress of the job
            addProgressChangedListener {
                dialogLayoutBinding.progressBar.progress = progress
                dialogLayoutBinding.progressTextView.text = "$progress% completed"
            }
            // on job done loading
            addJobDoneListener {
                dialog?.dismiss()
                if (status == Job.Status.SUCCEEDED) {
                    // display the map preview using the result from the completed job
                    showMapPreview(result)
                } else {
                    showError(error.message.toString())
                    isJobFinished = true
                }
            }
        }
    }

    /**
     * Updates the [downloadArea]'s geometry on ViewPoint change
     * or when export tiles button is clicked.
     */
    private fun updateDownloadAreaGeometry() {
        try {
            mapView.apply {
                if (mapView.map.loadStatus == LoadStatus.LOADED) {
                    // upper left corner of the downloaded tile cache area
                    val minScreenPoint: android.graphics.Point = android.graphics.Point(150, 175)
                    // lower right corner of the downloaded tile cache area
                    val maxScreenPoint: android.graphics.Point = android.graphics.Point(
                        mapView.width - 150,
                        mapView.height - 250
                    )
                    // convert screen points to map points
                    val minPoint: com.esri.arcgisruntime.geometry.Point =
                        mapView.screenToLocation(minScreenPoint)
                    val maxPoint: com.esri.arcgisruntime.geometry.Point =
                        mapView.screenToLocation(maxScreenPoint)
                    // use the points to define and return an envelope
                    downloadArea?.geometry = Envelope(minPoint, maxPoint)
                }
            }
        } catch (e: Exception) {
            // Silently fail, since mapView has not been rendered yet.
        }
    }

    /**
     * Create a progress dialog to track the progress of the [exportVectorTilesJob]
     */
    private fun createProgressDialog(): ProgressDialogLayoutBinding {
        val dialogLayoutBinding = ProgressDialogLayoutBinding.inflate(layoutInflater)
        val dialogBuilder = AlertDialog.Builder(this@MainActivity).apply {
            setTitle("Exporting vector tiles")
            setNegativeButton("Cancel job") { _, _ ->
                // cancels the export job asynchronously
                val future = exportVectorTilesJob?.cancelAsync()
                future?.addDoneListener {
                    // cancel is completed, so set to true
                    isJobFinished = true
                }
            }
            setCancelable(false)
            setView(dialogLayoutBinding.root)
        }
        dialog = dialogBuilder.create()
        return dialogLayoutBinding
    }

    /**
     * Display the preview of the exported map using the
     * [vectorTilesResult] from the completed job
     */
    private fun showMapPreview(vectorTilesResult: ExportVectorTilesResult?) {
        // get the layer exported for the preview MapView
        val vectorTiledLayer = ArcGISVectorTiledLayer(
            vectorTilesResult?.vectorTileCache,
            vectorTilesResult?.itemResourceCache
        )
        // set up the preview MapView
        previewMapView.apply {
            map = ArcGISMap(Basemap(vectorTiledLayer))
            setViewpoint(mapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE))
        }
        // control UI visibility
        previewMapView.getChildAt(0).visibility = View.VISIBLE
        show(closeButton, dimBackground, previewTextView, previewMapView)
        exportVectorTilesButton.visibility = View.GONE

        // required for some Android devices running older OS (combats Z-ordering bug in Android API)
        mapPreviewLayout.bringToFront()
    }

    /**
     * Makes the given views in the UI visible.
     * @param views the views to be made visible
     */
    private fun show(vararg views: View) {
        for (view in views) {
            view.visibility = View.VISIBLE
        }
    }

    /**
     * Makes the given views in the UI visible.
     * @param views the views to be made visible
     */
    private fun hide(vararg views: View) {
        for (view in views) {
            view.visibility = View.INVISIBLE
        }
    }

    /**
     * Called when close preview MapView is clicked
     */
    fun clearPreview(view: View) {
        // control UI visibility
        hide(closeButton, dimBackground, previewTextView, previewMapView)
        show(exportVectorTilesButton, mapView)
        downloadArea?.isVisible = true
        // required for some Android devices running older OS (combats Z-ordering bug in Android API)
        mapView.bringToFront()
    }

    private fun showError(message: String) {
        Log.e(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
