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

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.ItemResourceCache
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.exportvectortiles.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.vectortilecache.ExportVectorTilesTask
import java.io.File


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val exportVectorTilesButton: Button by lazy {
        activityMainBinding.exportVectorTilesButton
    }

    private val progressBar: ProgressBar by lazy {
        activityMainBinding.progressBar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION_NIGHT)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        mapView.setViewpoint(Viewpoint(34.056295, -117.195800, 10000.0))

        // create a graphics overlay for the map view
        val graphicsOverlay = GraphicsOverlay()
        mapView.graphicsOverlays.add(graphicsOverlay)

        // create a graphic to show a red outline square around the tiles to be downloaded
        val downloadArea = Graphic()
        graphicsOverlay.graphics.add(downloadArea)
        val simpleLineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2F)
        downloadArea.symbol = simpleLineSymbol

        // update the square whenever the viewpoint changes
        mapView.addViewpointChangedListener {
            if (map.loadStatus == LoadStatus.LOADED) {
                // upper left corner of the downloaded tile cache area
                val minScreenPoint = Point(50, 50)
                // lower right corner of the downloaded tile cache area
                val maxScreenPoint = Point(mapView.width - 100, mapView.height - 100)
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

        // when the map has loaded, create a vector tiled layer from it and export tiles
        mapView.map.addDoneLoadingListener {
            if (map.loadStatus == LoadStatus.LOADED) {
                // enable the export tiles button
                exportVectorTilesButton.isEnabled = true
                // check that the layer from the basemap is a vector tiled layer
                val layer = map.basemap.baseLayers[0]
                if (layer is ArcGISVectorTiledLayer) {
                    val vectorTiledLayer: ArcGISVectorTiledLayer = layer

                    // when the button is clicked, export the tiles to a temporary file
                    exportVectorTilesButton.setOnClickListener {
                        exportVectorTilesButton.isEnabled = false
                        progressBar.visibility = View.VISIBLE

                        // create a new export vector tiles task
                        val exportVectorTilesTask = ExportVectorTilesTask(vectorTiledLayer.uri)
                        // the max scale parameter is set to 10% of the map's scale to limit the
                        // number of tiles exported to within the vector tiled layer's max tile export limit
                        val exportVectorTilesParametersFuture = exportVectorTilesTask
                            .createDefaultExportVectorTilesParametersAsync(
                                downloadArea.geometry,
                                mapView.mapScale * 0.1
                            )

                        exportVectorTilesParametersFuture.addDoneListener {
                            try {
                                val exportVectorTilesParameters =
                                    exportVectorTilesParametersFuture.get()
                                // create a temporary directory in the app's cache for saving exported tiles
                                val exportTilesDirectory =
                                    File(
                                        externalCacheDir,
                                        getString(R.string.vector_tile_cache_folder)
                                    )
                                // create a job with the parameters
                                val exportVectorTilesJob = exportVectorTilesTask.exportVectorTiles(
                                    exportVectorTilesParameters,
                                    exportTilesDirectory.path + "file.vtpk"
                                ).apply {
                                    // start the export vector tile cache job
                                    start()
                                    addProgressChangedListener {
                                        progressBar.progress = progress
                                    }
                                    addJobDoneListener {
                                        // show preview of exported tiles in alert
                                        val tileCache = result.vectorTileCache
                                        val resourceCache: ItemResourceCache =
                                            result.itemResourceCache

                                        // reset the UI
                                        progressBar.visibility = View.GONE
                                        progressBar.progress = 0
                                        exportVectorTilesButton.isEnabled = false
                                    }

                                }
                            } catch (e: Exception) {

                            }
                        }
                    }
                }
            }
        }
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
