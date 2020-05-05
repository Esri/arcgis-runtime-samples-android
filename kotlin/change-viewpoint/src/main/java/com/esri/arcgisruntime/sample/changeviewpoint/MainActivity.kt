package com.esri.arcgisruntime.sample.changeviewpoint

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  private val scale = 5000.0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create ArcGISMap with imagery basemap and set it to the map view
    mapView.map = ArcGISMap(Basemap.createImageryWithLabels())

    // create point for starting location
    val startPoint = Point(-14093.0, 6711377.0, SpatialReferences.getWebMercator())

    // set viewpoint of map view to starting point and scaled
    mapView.setViewpointCenterAsync(startPoint, scale)
  }

  fun onAnimateClicked(view: View) {
    // create the London location point
    val londonPoint = Point(-14093.0, 6711377.0, SpatialReferences.getWebMercator())
    // create the viewpoint with the London point and scale
    val viewpoint = Viewpoint(londonPoint, scale)
    // set the map views's viewpoint to London with a seven second duration
    mapView.setViewpointAsync(viewpoint, 7f)
  }

  fun onCenterClicked(view: View) {
    // create the Waterloo location point
    val waterlooPoint = Point(-12153.0, 6710527.0, SpatialReferences.getWebMercator())
    // set the map views's viewpoint centered on Waterloo and scaled
    mapView.setViewpointCenterAsync(waterlooPoint, scale)
  }

  fun onGeometryClicked(view: View) {
    // create a collection of points around Westminster
    val westminsterPoints = PointCollection(SpatialReferences.getWebMercator())
    westminsterPoints.add(Point(-13823.0, 6710390.0))
    westminsterPoints.add(Point(-13823.0, 6710150.0))
    westminsterPoints.add(Point(-14680.0, 6710390.0))
    westminsterPoints.add(Point(-14680.0, 6710150.0))
    val geometry = Polyline(westminsterPoints)

    // set the map views's viewpoint to Westminster
    mapView.setViewpointGeometryAsync(geometry)
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
