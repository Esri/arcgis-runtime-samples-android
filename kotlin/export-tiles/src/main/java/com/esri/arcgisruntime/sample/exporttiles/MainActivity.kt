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

package com.esri.arcgisruntime.sample.exporttiles

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.exporttiles.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.exporttiles.databinding.DialogLayoutBinding
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheJob
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheParameters
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheTask
import java.io.File

class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.simpleName
    private var exportTileCacheJob: ExportTileCacheJob? = null
    private var downloadArea: Graphic? = null

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val exportTilesButton: Button by lazy {
        activityMainBinding.exportTilesButton
    }

    private val mapPreviewLayout: ConstraintLayout by lazy {
        activityMainBinding.mapPreviewLayout
    }

    private val previewTextView: TextView by lazy {
        activityMainBinding.previewTextView
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val closeButton: Button by lazy {
        activityMainBinding.closeButton
    }

    private val previewMapView: MapView by lazy {
        activityMainBinding.previewMapView
    }

    private val dimBackground: View by lazy {
        activityMainBinding.dimBackground
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // create an ArcGISTiledLayer to use as the basemap
        val tiledLayer = ArcGISTiledLayer(getString(R.string.world_street_map))
        val map = ArcGISMap().apply {
            basemap = Basemap(tiledLayer)
            minScale = 10000000.0
        }

        // create a graphic and graphics overlay to show a red box around the tiles to be downloaded
        downloadArea = Graphic()
        downloadArea?.symbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f)
        val graphicsOverlay = GraphicsOverlay()
        graphicsOverlay.graphics.add(downloadArea)

        mapView.apply {
            // set the map to the map view
            this.map = map
            setViewpoint(Viewpoint(51.5, 0.0, 10000000.0))

            // add the graphics overlay to the map view
            graphicsOverlays.add(graphicsOverlay)

            // update the box whenever the viewpoint changes
            addViewpointChangedListener {
                if (mapView.map.loadStatus == LoadStatus.LOADED) {
                    // upper left corner of the downloaded tile cache area
                    val minScreenPoint: android.graphics.Point = android.graphics.Point(150, 175)
                    // lower right corner of the downloaded tile cache area
                    val maxScreenPoint: android.graphics.Point = android.graphics.Point(
                        mapView.width - 150,
                        mapView.height - 250
                    )
                    // convert screen points to map points
                    val minPoint: Point = mapView.screenToLocation(minScreenPoint)
                    val maxPoint: Point = mapView.screenToLocation(maxScreenPoint)
                    // use the points to define and return an envelope
                    downloadArea?.geometry = Envelope(minPoint, maxPoint)
                }
            }
        }

        // when the button is clicked, export the tiles to the temporary directory
        exportTilesButton.setOnClickListener {
            val exportTileCacheTask = ExportTileCacheTask(tiledLayer.uri)
            // set up the export tile cache parameters
            val parametersFuture: ListenableFuture<ExportTileCacheParameters> =
                exportTileCacheTask.createDefaultExportTileCacheParametersAsync(
                    downloadArea?.geometry,
                    mapView.mapScale,
                    tiledLayer.maxScale
                )

            parametersFuture.addDoneListener {
                try {
                    val parameters: ExportTileCacheParameters = parametersFuture.get()
                    // create a temporary directory in the app's cache for saving exported tiles
                    val exportTilesDirectory =
                        File(externalCacheDir, getString(R.string.tile_cache_folder))
                    // export tiles to temporary cache on device
                    exportTileCacheJob =
                        exportTileCacheTask.exportTileCache(
                            parameters,
                            exportTilesDirectory.path + "file.tpk"
                        ).apply {
                            // start the export tile cache job
                            start()

                            val dialogLayoutBinding = DialogLayoutBinding.inflate(layoutInflater)
                            // show progress of the export tile cache job on the progress bar
                            val dialog = createProgressDialog(this)
                            dialog.setView(dialogLayoutBinding.root)
                            dialog.show()

                            // on progress change
                            addProgressChangedListener {
                                dialogLayoutBinding.progressBar.progress = progress
                                dialogLayoutBinding.progressTextView.text = "$progress%"
                            }

                            // when the job has completed, close the dialog and show the job result in the map preview
                            addJobDoneListener {
                                dialog.dismiss()
                                if (status == Job.Status.SUCCEEDED) {
                                    showMapPreview(result)
                                    downloadArea?.isVisible = false

                                } else {
                                    ("Job did not succeed: " + error.additionalMessage).also {
                                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG)
                                            .show()
                                        Log.e(TAG, error.additionalMessage)
                                    }
                                }
                            }
                        }
                } catch (e: Exception) {
                    val error = "Error generating tile cache parameters: ${e.message}"
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    Log.e(TAG, error)
                }
            }
        }

        // get correct view order set up on start
        clearPreview(mapView)
    }

    /**
     * Show tile cache preview window including containing the exported tiles.
     *
     * @param result takes the TileCache from the ExportTileCacheJob
     */
    private fun showMapPreview(result: TileCache) {

        // set up the preview map view
        previewMapView.apply {
            map = ArcGISMap(Basemap(ArcGISTiledLayer(result)))
            setViewpoint(mapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE))
        }
        // control UI visibility
        previewMapView.getChildAt(0).visibility = View.VISIBLE
        show(closeButton, dimBackground, previewTextView, previewMapView)
        exportTilesButton.visibility = View.GONE

        // required for some Android devices running older OS (combats Z-ordering bug in Android API)
        mapPreviewLayout.bringToFront()
    }

    /**
     * Clear the preview window.
     */
    fun clearPreview(view: View) {

        previewMapView.getChildAt(0).visibility = View.INVISIBLE
        hide(closeButton, dimBackground, previewTextView, previewMapView)
        // control UI visibility
        show(exportTilesButton, mapView)
        downloadArea?.isVisible = true

        // required for some Android devices running older OS (combats Z-ordering bug in Android API)
        mapView.bringToFront()
    }

    /**
     * Create a progress dialog box for tracking the export tile cache job.
     *
     * @param exportTileCacheJob the export tile cache job progress to be tracked
     * @return an AlertDialog set with the dialog layout view
     */
    private fun createProgressDialog(exportTileCacheJob: ExportTileCacheJob): AlertDialog {
        val builder = AlertDialog.Builder(this@MainActivity).apply {
            setTitle("Exporting tiles...")
            // provide a cancel button on the dialog
            setNeutralButton("Cancel") { _, _ ->
                exportTileCacheJob.cancel()
            }
            setCancelable(false)
            val dialogLayoutBinding = DialogLayoutBinding.inflate(layoutInflater)
            setView(dialogLayoutBinding.root)
        }
        return builder.create()
    }

    /**
     * Makes the given views in the UI visible.
     *
     * @param views the views to be made visible
     */
    private fun show(vararg views: View) {
        for (view in views) {
            view.visibility = View.VISIBLE
        }
    }

    /**
     * Makes the given views in the UI visible.
     *
     * @param views the views to be made visible
     */
    private fun hide(vararg views: View) {
        for (view in views) {
            view.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
        previewMapView.resume()
    }

    override fun onPause() {
        mapView.pause()
        previewMapView.pause()
        // delete app cache when the app loses focus
        cacheDir.deleteRecursively()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.dispose()
        previewMapView.dispose()
        super.onDestroy()
    }
}
