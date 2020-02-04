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

package com.example.exporttiles

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheJob
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheParameters
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheTask
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_layout.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create an ArcGISTiledLayer to use as the basemap
        val tiledLayer = ArcGISTiledLayer(getString(R.string.world_street_map))
        val map = ArcGISMap().apply {
            basemap = Basemap(tiledLayer)
            minScale = 10000000.0
        }
        // create a graphics overlay
        val graphicsOverlay = GraphicsOverlay()
        mapView.apply {
            // set the map to the map view
            this.map = map
            setViewpoint(Viewpoint(51.5, 0.0, 10000000.0))
            // add the graphics overlay to the map view
            graphicsOverlays.add(graphicsOverlay)
        }

        // create a graphic to show a red box around the tiles we want to download
        val downloadArea = Graphic()
        graphicsOverlay.graphics.add(downloadArea)
        val simpleLineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f)
        downloadArea.symbol = simpleLineSymbol

        // update the box whenever the viewpoint changes
        mapView.addViewpointChangedListener {
            if (mapView.map.loadStatus == LoadStatus.LOADED) {
                // upper left corner of the downloaded tile cache area
                val minScreenPoint = Point(150, 175)
                // lower right corner of the downloaded tile cache area
                val maxScreenPoint = Point(mapView.getWidth() -150,
                    mapView.getHeight() -250)
                // convert screen points to map points
                    val minPoint: com.esri.arcgisruntime.geometry.Point? = mapView?.screenToLocation(minScreenPoint)
                    val maxPoint: com.esri.arcgisruntime.geometry.Point? = mapView?.screenToLocation(maxScreenPoint)
                    // use the points to define and return an envelope
                if (minScreenPoint != null && maxScreenPoint != null) {
                    val envelope = Envelope(minPoint, maxPoint)
                    downloadArea.geometry = envelope
                }
            }
        }

        // create up a temporary directory in the app's cache for saving exported tiles
        val exportTilesDirectory = File(cacheDir, getString(R.string.tile_cache_folder))

        // when the button is clicked, export the tiles to a temporary file
        exportTilesButton.setOnClickListener {
            val exportTileCacheTask = ExportTileCacheTask(tiledLayer.uri)
            val parametersFuture: ListenableFuture<ExportTileCacheParameters> =
                exportTileCacheTask.createDefaultExportTileCacheParametersAsync(downloadArea.geometry, mapView.mapScale, tiledLayer.maxScale)

            parametersFuture.addDoneListener {
                val parameters: ExportTileCacheParameters = parametersFuture.get()
                // export tiles to temporary cache on device
                val exportTileCacheJob = exportTileCacheTask.exportTileCache(parameters, exportTilesDirectory.path + "file.tpk")
                exportTileCacheJob.start()
                // create a progress dialog to show export tile progress
                val dialog = createProgressDialog(exportTileCacheJob)
                dialog.show()
                exportTileCacheJob.addProgressChangedListener {
                    dialog.progressBar2.setProgress(exportTileCacheJob.progress)
                }

                exportTileCacheJob.addJobDoneListener {
                    dialog.dismiss()
                    if (exportTileCacheJob.status == Job.Status.SUCCEEDED) {
                        val exportedTileCacheResult: TileCache = exportTileCacheJob.result
                        showMapPreview(exportedTileCacheResult)
                    } else {
                        Log.e(TAG, exportTileCacheJob.error.additionalMessage)
                        Toast.makeText(this,
                            exportTileCacheJob.error.additionalMessage,
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // close the preview window
        closeButton.setOnClickListener{clearPreview()}
        clearPreview()
    }

    /**
     * Show tile cache preview window including containing the exported tils.
     *
     * @param result takes the TileCache from the ExportTileCacheJob
     */
    private fun showMapPreview(result: TileCache){
        val newTiledLayer = ArcGISTiledLayer(result)
        val previewMap = ArcGISMap(Basemap(newTiledLayer))

        previewMapView.apply {
            map = previewMap
            setViewpoint(mapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE))
            visibility = View.VISIBLE
            getChildAt(0).visibility = View.VISIBLE
        }
        mapPreviewLayout.bringToFront()
        exportTilesButton.visibility = View.GONE
    }

    /**
     * Create a progress dialog box for tracking the export tile cache job.
     *
     * @param exportTileCacheJob the export tile cache job progress to be tracked
     * @return an AlertDialog set with the dialog layout view
     */
    private fun createProgressDialog(exportTileCacheJob: ExportTileCacheJob): AlertDialog {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Exporting tiles...")
        // provide a cancel button on the dialog
        builder.setNeutralButton("Cancel") { _, _ ->
            exportTileCacheJob.cancel()
        }

        builder.setView(R.layout.dialog_layout)
        return builder.create()
    }

    /**
     * Clear the preview window.
     */
    private fun clearPreview() {
        previewMapView.getChildAt(0).visibility = View.INVISIBLE
        mapView.bringToFront()
        exportTilesButton.visibility = View.VISIBLE
    }

    /**
     * Recursively deletes all files in the given directory.
     *
     * @param dir to delete
     */
    private fun deleteDirectory(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (child in children) {
                val success = deleteDirectory(File(dir, child))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onPause() {
        mapView.pause()
        deleteDirectory(getCacheDir())
        super.onPause()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }
}
