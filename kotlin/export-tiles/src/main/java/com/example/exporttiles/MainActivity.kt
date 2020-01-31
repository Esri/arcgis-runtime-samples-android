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

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheJob
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheParameters
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheTask
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_layout.*
import kotlinx.android.synthetic.main.dialog_layout.view.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var tiledLayer: ArcGISTiledLayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create a temporary directory in the app's cache for saving exported tiles to
        // create up a temporary directory in the app's cache for saving downloaded preplanned maps
        val exportTilesDirectory = File(cacheDir, getString(R.string.tile_cache_folder))

        tiledLayer = ArcGISTiledLayer(getString(R.string.world_street_map))
        val map = ArcGISMap().apply {
            this.basemap = Basemap(tiledLayer)
            this.minScale = 10000000.0
        }

        mapView.apply {
            this.map = map
            this.setViewpoint(Viewpoint(51.5, 0.0, 10000000.0))
        }

        // setting up an AlertDialog Builder
        exportTilesButton.setOnClickListener {

            val exportTileCacheTask = ExportTileCacheTask(tiledLayer.uri)
            val parametersFuture: ListenableFuture<ExportTileCacheParameters> =
                exportTileCacheTask.createDefaultExportTileCacheParametersAsync(viewToExtent(), mapView.mapScale, tiledLayer.maxScale)
            parametersFuture.addDoneListener {
                val parameters: ExportTileCacheParameters = parametersFuture.get()
                val exportTileCacheJob = exportTileCacheTask.exportTileCache(parameters, exportTilesDirectory.path + "file.tpk")
                exportTileCacheJob.start()

                createProgressDialog(exportTileCacheJob)

                exportTileCacheJob.addJobDoneListener{
                    
                }


            }

        }
    }

    private fun createProgressDialog(exportTileCacheJob: ExportTileCacheJob) {

        // Initialize a new instance of
        val builder = AlertDialog.Builder(this@MainActivity)
        // Set the alert dialog title
        builder.setTitle("Exporting Tiles")
        // Display a neutral button on alert dialog
        builder.setNeutralButton("Cancel") { _, _ ->
            exportTileCacheJob.cancel()
            Toast.makeText(applicationContext, "You cancelled the tile export.", Toast.LENGTH_SHORT).show()
        }

        builder.setView(R.layout.dialog_layout)
        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()
        // get the layout
        val view = dialog.layoutInflater.inflate(R.layout.dialog_layout, null)
        //get the progress bar
        view.progressBar2.setProgress(exportTileCacheJob.progress)
        // Display the alert dialog on app interface
        dialog.show()

    }


    private fun viewToExtent(): Envelope? { // upper left corner of the downloaded tile cache area
        val minScreenPoint = Point(mapView.getLeft() - mapView.getWidth(),
            mapView.getTop() - mapView.getHeight())
        // lower right corner of the downloaded tile cache area
        val maxScreenPoint = Point(minScreenPoint.x + mapView.getWidth() * 3,
            minScreenPoint.y + mapView.getHeight() * 3)
        // convert screen points to map points
        val minPoint: com.esri.arcgisruntime.geometry.Point = mapView.screenToLocation(minScreenPoint)
        val maxPoint: com.esri.arcgisruntime.geometry.Point = mapView.screenToLocation(maxScreenPoint)
        // use the points to define and return an envelope
        return Envelope(minPoint, maxPoint)
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
