/*
 *  Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.nearestvertex

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.sample.nearestvertex.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val activityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  private val distanceLayout: ConstraintLayout by lazy {
    activityMainBinding.distanceLayout
  }

  private val mapView: MapView by lazy {
    activityMainBinding.mapView
  }

  private val vertexDistanceTextView: TextView by lazy {
    activityMainBinding.vertexDistanceTextView
  }

  private val coordinateDistanceTextView: TextView by lazy {
    activityMainBinding.coordinateDistanceTextView
  }

  // California zone 5 (ftUS) state plane coordinate system.
  private val statePlaneCaliforniaZone5 = SpatialReference.create(2229)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activityMainBinding.root)

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create a graphic for the polygon
    val polygonPoints = PointCollection(statePlaneCaliforniaZone5)
    polygonPoints.addAll(
      listOf(
        Point(6627416.41469281, 1804532.53233782),
        Point(6669147.89779046, 2479145.16609522),
        Point(7265673.02678292, 2484254.50442408),
        Point(7676192.55880379, 2001458.66365744),
        Point(7175695.94143837, 1840722.34474458)
      )
    )
    val polygon = Polygon(polygonPoints)
    val polygonOutlineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GREEN, 2f)
    val polygonFillSymbol =
      SimpleFillSymbol(SimpleFillSymbol.Style.FORWARD_DIAGONAL, Color.GREEN, polygonOutlineSymbol)
    val polygonGraphic = Graphic(polygon, polygonFillSymbol)

    // create graphics with symbols for tapped location, nearest coordinate, and nearest vertex
    val tappedLocationGraphic = Graphic().apply {
      symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.X, Color.MAGENTA, 15f)
    }
    val nearestCoordinateGraphic = Graphic().apply {
      symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.RED, 10f)
    }
    val nearestVertexGraphic = Graphic().apply {
      symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLUE, 15f)
    }

    // create a graphics overlay to show the polygon, tapped location, and nearest vertex/coordinate
    val graphicsOverlay = GraphicsOverlay().apply {
      graphics.addAll(
        listOf(
          polygonGraphic,
          tappedLocationGraphic,
          nearestCoordinateGraphic,
          nearestVertexGraphic
        )
      )
    }

    mapView.apply {
      // create a map using the PortalItem
      // and add the FeatureLayer to the map view's basemap
      map = ArcGISMap(statePlaneCaliforniaZone5)

      val portalItem = PortalItem(Portal("https://arcgisruntime.maps.arcgis.com",false),"99fd67933e754a1181cc755146be21ca")
      val usStatesGeneralizedLayer = FeatureLayer(portalItem,0)
      map.basemap.baseLayers.add(usStatesGeneralizedLayer)

      // add the graphics overlay to the map view
      graphicsOverlays.add(graphicsOverlay)

      // zoom to the polygon's extent
      setViewpointGeometryAsync(polygon.extent, 100.0)

      // get the nearest vertex on tap
      onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
          // create a screen point from where the user tapped
          val screenPoint = android.graphics.Point(e.x.roundToInt(), e.y.roundToInt())
          // create a map point from the screen point
          val mapPoint: Point = mapView.screenToLocation(screenPoint)
          // show where the user clicked
          tappedLocationGraphic.geometry = mapPoint

          // use the geometry engine to get the nearest vertex
          val nearestVertexResult = GeometryEngine.nearestVertex(polygon, mapPoint)
          // set the nearest vertex graphic's geometry to the nearest vertex
          nearestVertexGraphic.geometry = nearestVertexResult.coordinate
          // use the geometry engine to get the nearest coordinate
          val nearestCoordinateResult = GeometryEngine.nearestCoordinate(polygon, mapPoint)
          // set the nearest coordinate graphic's geometry to the nearest coordinate
          nearestCoordinateGraphic.geometry = nearestCoordinateResult.coordinate

          // show the distances to the nearest vertex and nearest coordinate
          distanceLayout.visibility = VISIBLE
          val vertexDistance = (nearestVertexResult.distance / 5280.0).toInt()
          val coordinateDistance = (nearestCoordinateResult.distance / 5280.0).toInt()
          vertexDistanceTextView.text = getString(R.string.nearest_vertex, vertexDistance)
          coordinateDistanceTextView.text =
            getString(R.string.nearest_coordinate, coordinateDistance)
          return true
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
