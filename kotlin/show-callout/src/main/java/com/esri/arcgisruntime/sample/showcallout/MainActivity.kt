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

package com.esri.arcgisruntime.sample.showcallout

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with a topographic basemap
    val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 16)

    // set the map to be displayed in the layout's map view
    mapView.map = map

    mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {

      override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        // get the point that was tapped on the screen
        val screenPoint =
          android.graphics.Point(motionEvent.x.roundToInt(), motionEvent.y.roundToInt())
        // create a map point from that screen point
        val mapPoint = mapView.screenToLocation(screenPoint)
        // convert the point to WGS84 for obtaining lat/lon format
        val wgs84Point = GeometryEngine.project(mapPoint, SpatialReferences.getWgs84()) as Point
        // create a textview for the callout
        val calloutContent = TextView(applicationContext).apply {
          setTextColor(Color.BLACK)
          setSingleLine()
          // format coordinates to 4 decimal places and display lat long read out
          text = getString(R.string.callout_text, wgs84Point.y, wgs84Point.x)
        }

        // get the callout, set its content and show it and the tapped location
        mapView.callout.apply {
          location = mapPoint
          content = calloutContent
          show()
        }

        // center the map on the tapped location
        mapView.setViewpointCenterAsync(mapPoint)

        return true
      }
    }
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
