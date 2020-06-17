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
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Multipoint
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
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

    val map = ArcGISMap(Basemap.createTopographic())
    mapView.map = map
    mapView.graphicsOverlays.add(GraphicsOverlay())
    val simpleMarkerSymbol = SimpleMarkerSymbol(
      SimpleMarkerSymbol.Style.CIRCLE,
      0xFFFF0000.toInt(), 10F
    )
    val pointGraphic = Graphic().apply { symbol = simpleMarkerSymbol }
    mapView.graphicsOverlays[0].graphics.add(pointGraphic)

    val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0000FF.toInt(), 3F)
    val fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.NULL, 0x00000000, lineSymbol)
    val convexHullGraphic = Graphic()
    mapView.graphicsOverlays[0].graphics.add(convexHullGraphic)

    val inputPoints = arrayListOf<Point>()

    mapView.setOnTouchListener { _, e ->
      val point = android.graphics.Point(e.x.toInt(), e.y.toInt())
      val convertedPoint = mapView.screenToLocation(point)
      inputPoints.add(convertedPoint)
      val multiPoint = Multipoint(PointCollection(inputPoints))
      pointGraphic.geometry = multiPoint
      true
    }

    button.setOnClickListener {
      val convexHull = GeometryEngine.convexHull(pointGraphic.geometry)
      convexHullGraphic.symbol = when (convexHull.geometryType) {
        GeometryType.POINT -> simpleMarkerSymbol
        GeometryType.POLYLINE -> lineSymbol
        GeometryType.POLYGON -> fillSymbol
        else -> null
      }
      convexHullGraphic.geometry = convexHull
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
