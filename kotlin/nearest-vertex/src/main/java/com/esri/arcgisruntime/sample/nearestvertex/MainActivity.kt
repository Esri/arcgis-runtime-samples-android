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
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activityMainBinding.root)

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create a graphic for the polygon
    val polygonPoints = PointCollection(SpatialReferences.getWebMercator())
    polygonPoints.addAll(
      listOf(
        Point(-5991501.677830, 5599295.131468),
        Point(-6928550.398185, 2087936.739807),
        Point(-3149463.800709, 1840803.011362),
        Point(-1563689.043184, 3714900.452072),
        Point(-3180355.516764, 5619889.608838)
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
      // create a map with a basemap and add it to the map view
      map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

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
          // the map point should be normalized to the central meridian when wrapping around a map,
          // so its value stays within the coordinate system of the map view
          val normalizedMapPoint: Point = GeometryEngine.normalizeCentralMeridian(mapPoint) as Point
          // show where the user clicked
          tappedLocationGraphic.geometry = normalizedMapPoint

          // use the geometry engine to get the nearest vertex
          val nearestVertexResult = GeometryEngine.nearestVertex(polygon, normalizedMapPoint)
          // set the nearest vertex graphic's geometry to the nearest vertex
          nearestVertexGraphic.geometry = nearestVertexResult.coordinate
          // use the geometry engine to get the nearest coordinate
          val nearestCoordinateResult =
            GeometryEngine.nearestCoordinate(polygon, normalizedMapPoint)
          // set the nearest coordinate graphic's geometry to the nearest coordinate
          nearestCoordinateGraphic.geometry = nearestCoordinateResult.coordinate

          // show the distances to the nearest vertex and nearest coordinate
          distanceLayout.visibility = VISIBLE
          val vertexDistance = (nearestVertexResult.distance / 1000.0).toInt()
          val coordinateDistance = (nearestCoordinateResult.distance / 1000.0).toInt()
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
