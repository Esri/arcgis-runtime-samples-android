/* Copyright 2020 Esri
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
package com.esri.arcgisruntime.sample.addgraphicsrenderer

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PolygonBuilder
import com.esri.arcgisruntime.geometry.PolylineBuilder
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with a topographic basemap
    val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 15.169193, 16.333479, 2)
    // add graphics overlay
    addGraphicsOverlay()
    // set the map to be displayed in this view
    mapView.map = map
  }

  private fun addGraphicsOverlay() {
    // point graphic
    // create a graphic overlay for the point
    val pointGraphicOverlay = GraphicsOverlay()
    // add graphics overlay to the MapView
    mapView.graphicsOverlays.add(pointGraphicOverlay)
    // create point
    val pointGeometry =
      Point(40e5, 40e5, SpatialReferences.getWebMercator())
    // create graphic for point
    val pointGraphic = Graphic(pointGeometry)
    // add graphic to overlay
    pointGraphicOverlay.graphics.add(pointGraphic)
    // red diamond point symbol
    val pointSymbol =
      SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.RED, 10f)
    // create simple renderer
    val pointRenderer = SimpleRenderer(pointSymbol)
    // set the renderer on the graphics overlay to the new renderer
    pointGraphicOverlay.renderer = pointRenderer

    // line graphic
    // create graphic overlay for polyline
    val lineGraphicOverlay = GraphicsOverlay()
    // add graphics overlay to the MapView
    mapView.graphicsOverlays.add(lineGraphicOverlay)
    // create line
    val lineGeometry = PolylineBuilder(SpatialReferences.getWebMercator()).apply {
      addPoint(-10e5, 40e5)
      addPoint(20e5, 50e5)
    }
    // create graphic for polyline
    val lineGraphic = Graphic(lineGeometry.toGeometry())
    // add graphic to overlay
    lineGraphicOverlay.graphics.add(lineGraphic)
    // solid blue line symbol
    val lineSymbol =
      SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f)
    // create simple renderer
    val lineRenderer = SimpleRenderer(lineSymbol)
    // set the renderer on the graphics overlay to the new renderer
    lineGraphicOverlay.renderer = lineRenderer

    // polygon graphic
    // create graphic overlay for polygon
    val polygonGraphicOverlay = GraphicsOverlay()
    // add graphics overlay to MapView
    mapView.graphicsOverlays.add(polygonGraphicOverlay)
    // create polygon
    val polygonGeometry = PolygonBuilder(SpatialReferences.getWebMercator()).apply {
      addPoint(-20e5, 20e5)
      addPoint(20e5, 20e5)
      addPoint(20e5, -20e5)
      addPoint(-20e5, -20e5)
    }
    // create graphic for polygon
    val polygonGraphic = Graphic(polygonGeometry.toGeometry())
    // add graphic to overlay
    polygonGraphicOverlay.graphics.add(polygonGraphic)
    // solid yellow polygon symbol
    val polygonSymbol =
      SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.YELLOW, null)
    // create simple renderer
    val polygonRenderer = SimpleRenderer(polygonSymbol)
    // set the renderer on the graphics overlay to the new renderer
    polygonGraphicOverlay.renderer = polygonRenderer
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
