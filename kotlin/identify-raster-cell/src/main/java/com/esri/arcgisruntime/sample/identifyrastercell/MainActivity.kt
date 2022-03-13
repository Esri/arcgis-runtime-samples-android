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

package com.esri.arcgisruntime.sample.identifyrastercell

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.raster.Raster
import com.esri.arcgisruntime.raster.RasterCell
import com.esri.arcgisruntime.sample.identifyrastercell.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var rasterLayer: RasterLayer? = null

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // load the raster file
        val rasterFile =
            Raster(getExternalFilesDir(null)?.path + "/SA_EVI_8Day_03May20.tif")

        // create the layer
        rasterLayer = RasterLayer(rasterFile)

        // define a new map
        val rasterMap = ArcGISMap(BasemapStyle.ARCGIS_OCEANS).apply {
            // add the raster layer
            operationalLayers.add(rasterLayer)
        }

        mapView.apply {
            // add the map to the map view
            map = rasterMap
            setViewpoint(Viewpoint(-33.9, 18.6, 1000000.0))

            // set behavior for double touch drag and on single tap gestures
            onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
                override fun onDoubleTouchDrag(e: MotionEvent): Boolean {
                    // identify the pixel at the given screen point
                    identifyPixel(Point(e.x.toInt(), e.y.toInt()))
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    // identify the pixel at the given screen point
                    identifyPixel(Point(e.x.toInt(), e.y.toInt()))
                    return true
                }
            }
        }
    }

    /**
     * Identify the pixel at the given screen point and report raster cell attributes in a callout.
     *
     * @param screenPoint from motion event, for use in identify
     */
    private fun identifyPixel(screenPoint: Point) {
        rasterLayer?.let { rasterLayer ->
            // identify at the tapped screen point
            val identifyResultFuture =
                mapView.identifyLayerAsync(rasterLayer, screenPoint, 1.0, false, 10)

            identifyResultFuture.addDoneListener {
                // get the identify result
                val identifyResult = identifyResultFuture.get()

                // create a string builder
                val stringBuilder = StringBuilder()

                // get the a list of geoelements as raster cells from the identify result
                identifyResult.elements.filterIsInstance<RasterCell>().forEach { cell ->
                    // get each attribute for the cell
                    cell.attributes.forEach {
                        // add the key/value pair to the string builder
                        stringBuilder.append(it.key + ": " + it.value)
                        stringBuilder.append("\n")
                    }

                    // format the X & Y coordinate values of the raster cell to a human readable string
                    val xyString =
                        "X: ${String.format("%.4f", cell.geometry.extent.xMin)} " + "\n" +
                                "Y: ${String.format("%.4f", cell.geometry.extent.yMin)}"
                    // add the coordinate string to the string builder
                    stringBuilder.append(xyString)

                    // create a textview for the callout
                    val calloutContent = TextView(applicationContext).apply {
                        setTextColor(Color.BLACK)
                        // format coordinates to 4 decimal places and display lat long read out
                        text = stringBuilder.toString()
                    }
                    // display the callout in the map view
                    mapView.callout.apply {
                        location = mapView.screenToLocation(screenPoint)
                        content = calloutContent
                        style.leaderLength = 64
                    }.show()
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
