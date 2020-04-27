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
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.raster.ImageServiceRaster
import com.esri.arcgisruntime.raster.Raster
import com.esri.arcgisruntime.raster.RasterCell
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  private lateinit var rasterLayer: RasterLayer

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // load the raster file
    val rasterFile = Raster(getExternalFilesDir(null)?.path + "/raster-file/Shasta.tif")

    // create the layer
    rasterLayer = RasterLayer(rasterFile)

    // define a new map
    val rasterMap = ArcGISMap(Basemap.createTopographicVector()).apply {
      // add the raster layer
      operationalLayers.add(rasterLayer)
    }

    mapView.apply {
      // add the map to the map view
      map = rasterMap

      // once the raster layer has loaded
      rasterLayer.addDoneLoadingListener {
        // set the map view's view point to the raster layer extent
        setViewpointGeometryAsync(rasterLayer.fullExtent)
      }

      // on single tap
      onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
          // identify the pixel at the given screen point
          identifyPixel(Point(e.x.toInt(), e.y.toInt()), rasterLayer)
          return true
        }
      }
    }
  }

  private fun identifyPixel(screenPoint: Point, rasterLayer: RasterLayer) {
    // identify at the tapped screen point
    val identifyResultFuture = mapView.identifyLayerAsync(rasterLayer, screenPoint, 1.0, false, 10)
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

        // Shorten the X & Y values a little to show better in the callout
        val theX: Double = cell.geometry.extent.xMin
        val theY: Double = cell.geometry.extent.yMin

        // Format the X & Y values as a human readable string
        val theString = "X: " + String.format("%.4f", theX) + " Y: " + String.format("%.4f", theY)

        // Add the X & Y coordinates where the user clicked raster cell to the string builder
        stringBuilder.append(theString)

        // create a textview for the callout
        val calloutContent = TextView(applicationContext).apply {
          setTextColor(Color.BLACK)
          // format coordinates to 4 decimal places and display lat long read out
          text = stringBuilder.toString()
        }
        // Display the callout in the map view
        mapView.callout.apply {
          location = mapView.screenToLocation(screenPoint)
          content = calloutContent
        }.show()

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
