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

package com.esri.arcgisruntime.sample.convexhull

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Multipoint
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create an array list to store points
    val inputPoints = arrayListOf<Point>()

    // create a symbol and graphic to represent single points
    val pointSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000.toInt(), 10F)
    val pointGraphic = Graphic().apply { symbol = pointSymbol }

    // create a graphic for the convex hull consisting of a line and empty interior
    val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0000FF.toInt(), 3F)
    val fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.NULL, 0x00000000, lineSymbol)
    val convexHullGraphic = Graphic()

    // create a graphics overlay and add the graphics to it
    val graphicsOverlay = GraphicsOverlay()
    graphicsOverlay.graphics.addAll(listOf(pointGraphic, convexHullGraphic))

    mapView.apply {
      // set the map to a topographic basemap
      map = ArcGISMap(Basemap.createTopographic())
      // add the graphics overlay to the map
      graphicsOverlays.add(graphicsOverlay)

      onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
          // get the tapped point and convert it to a screen point
          val point = android.graphics.Point(e.x.toInt(), e.y.toInt())
          val convertedPoint = mapView.screenToLocation(point)

          // add the new point to the list and recreate the graphic representing the input points
          inputPoints.add(convertedPoint)
          val multiPoint = Multipoint(PointCollection(inputPoints))
          pointGraphic.geometry = multiPoint

          if (inputPoints.isNotEmpty()) {
            createButton.isEnabled = true
            resetButton.isEnabled = true
          }

          return super.onSingleTapConfirmed(e)
        }
      }
    }

    // create the convex hull when the button is pressed
    createButton.setOnClickListener {
      // normalize the points for panning beyond the meridian
      val normalizedPoints = GeometryEngine.normalizeCentralMeridian(pointGraphic.geometry)
      // create a convex hull from the points
      val convexHull = GeometryEngine.convexHull(normalizedPoints)
      // the convex hull's geometry may be a point or polyline if the number of points is less than 3
      convexHullGraphic.symbol = when (convexHull.geometryType) {
        GeometryType.POINT -> pointSymbol
        GeometryType.POLYLINE -> lineSymbol
        GeometryType.POLYGON -> fillSymbol
        else -> null
      }
      // set the convex hull graphic to display the new geometry
      convexHullGraphic.geometry = convexHull
      // disable the button
      createButton.isEnabled = false
    }

    // clear the points and graphics and disable the buttons when reset is tapped
    resetButton.setOnClickListener {
      inputPoints.clear()
      pointGraphic.geometry = null
      convexHullGraphic.geometry = null
      resetButton.isEnabled = false
      createButton.isEnabled = false
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
