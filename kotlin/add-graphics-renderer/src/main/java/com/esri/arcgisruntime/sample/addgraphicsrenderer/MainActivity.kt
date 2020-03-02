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
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PolygonBuilder
import com.esri.arcgisruntime.geometry.PolylineBuilder
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer

class MainActivity : AppCompatActivity() {
  private var mMapView: MapView? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // create MapView from layout
    mMapView =
      findViewById<View>(R.id.mapView) as MapView
    // create a map with the Basemap Type topographic
    val mMap = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 15.169193, 16.333479, 2)
    // add graphics overlay
    addGraphicsOverlay()
    // set the map to be displayed in this view
    mMapView!!.map = mMap
  }

  private fun addGraphicsOverlay() { // point graphic
    val pointGeometry =
      Point(40e5, 40e5, SpatialReferences.getWebMercator())
    // red diamond point symbol
    val pointSymbol =
      SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.RED, 10)
    // create graphic for point
    val pointGraphic = Graphic(pointGeometry)
    // create a graphic overlay for the point
    val pointGraphicOverlay = GraphicsOverlay()
    // create simple renderer
    val pointRenderer = SimpleRenderer(pointSymbol)
    pointGraphicOverlay.renderer = pointRenderer
    // add graphic to overlay
    pointGraphicOverlay.graphics.add(pointGraphic)
    // add graphics overlay to the MapView
    mMapView!!.graphicsOverlays.add(pointGraphicOverlay)
    // line graphic
    val lineGeometry = PolylineBuilder(SpatialReferences.getWebMercator())
    lineGeometry.addPoint(-10e5, 40e5)
    lineGeometry.addPoint(20e5, 50e5)
    // solid blue line symbol
    val lineSymbol =
      SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5)
    // create graphic for polyline
    val lineGraphic = Graphic(lineGeometry.toGeometry())
    // create graphic overlay for polyline
    val lineGraphicOverlay = GraphicsOverlay()
    // create simple renderer
    val lineRenderer = SimpleRenderer(lineSymbol)
    // add graphic to overlay
    lineGraphicOverlay.renderer = lineRenderer
    // add graphic to overlay
    lineGraphicOverlay.graphics.add(lineGraphic)
    // add graphics overlay to the MapView
    mMapView!!.graphicsOverlays.add(lineGraphicOverlay)
    //polygon graphic
    val polygonGeometry = PolygonBuilder(SpatialReferences.getWebMercator())
    polygonGeometry.addPoint(-20e5, 20e5)
    polygonGeometry.addPoint(20e5, 20e5)
    polygonGeometry.addPoint(20e5, -20e5)
    polygonGeometry.addPoint(-20e5, -20e5)
    // solid yellow polygon symbol
    val polygonSymbol =
      SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.YELLOW, null)
    // create graphic for polygon
    val polygonGraphic = Graphic(polygonGeometry.toGeometry())
    // create graphic overlay for polygon
    val polygonGraphicOverlay = GraphicsOverlay()
    // create simple renderer
    val polygonRenderer = SimpleRenderer(polygonSymbol)
    // add graphic to overlay
    polygonGraphicOverlay.renderer = polygonRenderer
    // add graphic to overlay
    polygonGraphicOverlay.graphics.add(polygonGraphic)
    // add graphics overlay to MapView
    mMapView!!.graphicsOverlays.add(polygonGraphicOverlay)
  }

  override fun onPause() {
    super.onPause()
    mMapView!!.pause()
  }

  override fun onResume() {
    super.onResume()
    mMapView!!.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mMapView!!.dispose()
  }
}