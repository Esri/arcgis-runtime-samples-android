package com.esri.arcgisruntime.sample.nearestvertex

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with a basemap and add it to the map view
    mapView.map = ArcGISMap(Basemap.createTopographic())

    // create a graphics overlay to show the polygon, clicked location, and nearest vertex/coordinate
    val graphicsOverlay = GraphicsOverlay()
    mapView.graphicsOverlays.add(graphicsOverlay)

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
    val polygonOutlineSymbol =
      SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, -0xff0100, 2f)
    val polygonFillSymbol =
      SimpleFillSymbol(SimpleFillSymbol.Style.FORWARD_DIAGONAL, -0xff0100, polygonOutlineSymbol)
    val polygonGraphic = Graphic(polygon, polygonFillSymbol)
    graphicsOverlay.graphics.add(polygonGraphic)

    // create graphics for the clicked location, nearest coordinate, and nearest vertex markers
    val clickedLocationSymbol =
      SimpleMarkerSymbol(SimpleMarkerSymbol.Style.X, -0x5b00, 15f)
    val nearestCoordinateSymbol =
      SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, -0x10000, 10f)
    val nearestVertexSymbol =
      SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, -0xffff01, 15f)
    val tappedLocationGraphic = Graphic()
    tappedLocationGraphic.symbol = clickedLocationSymbol
    val nearestCoordinateGraphic = Graphic()
    nearestCoordinateGraphic.symbol = nearestCoordinateSymbol
    val nearestVertexGraphic = Graphic()
    nearestVertexGraphic.symbol = nearestVertexSymbol
    graphicsOverlay.graphics
      .addAll(listOf(tappedLocationGraphic, nearestCoordinateGraphic, nearestVertexGraphic))

    // get the nearest vertex on tap
    mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
      override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        // create a screen point from where the user tapped
        val screenPoint = android.graphics.Point(e.x.roundToInt(), e.y.roundToInt())
        // create a map point from the screen point
        val mapPoint: Point = mapView.screenToLocation(screenPoint)
        // the map point should be normalized to the central meridian when wrapping around a map, so its value stays within the coordinate system of the map view
        val normalizedMapPoint: Point = GeometryEngine.normalizeCentralMeridian(mapPoint) as Point
        // show where the user clicked
        tappedLocationGraphic.geometry = normalizedMapPoint

        // show the nearest coordinate and vertex
        val nearestCoordinateResult = GeometryEngine.nearestCoordinate(polygon, normalizedMapPoint)
        val nearestVertexResult = GeometryEngine.nearestVertex(polygon, normalizedMapPoint)
        nearestVertexGraphic.geometry = nearestVertexResult.coordinate
        nearestCoordinateGraphic.geometry = nearestCoordinateResult.coordinate

        // show the distances to the nearest vertex and nearest coordinate rounded to the nearest kilometer
        val vertexDistance = (nearestVertexResult.distance / 1000.0).toInt()
        val coordinateDistance = (nearestCoordinateResult.distance / 1000.0).toInt()
        vertexDistanceTextView.text = "Vertex distance: $vertexDistance km"
        coordinateDistanceTextView.text = "Coordinate distance: $coordinateDistance km"
        return true
      }
    }

    // zoom to the polygon's extent
    mapView.setViewpointGeometryAsync(polygon.extent, 100.0)
  }

  override fun onPause() {
    super.onPause()
    mapView.pause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.dispose()
  }
}
