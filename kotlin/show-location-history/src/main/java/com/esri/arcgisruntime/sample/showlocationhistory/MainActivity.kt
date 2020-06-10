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

package com.esri.arcgisruntime.sample.showlocationhistory

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.geometry.PolylineBuilder
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.location.SimulatedLocationDataSource
import com.esri.arcgisruntime.location.SimulationParameters
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Calendar

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a center point for the data in Redlands, California
    val center = Point(-13185535.98, 4037766.28, SpatialReference.create(102100))

    // create a graphics overlay for the points and use a red circle for the symbols
    val locationHistoryOverlay = GraphicsOverlay()
    val locationSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10f)
    locationHistoryOverlay.renderer = SimpleRenderer(locationSymbol)

    // create a graphics overlay for the lines connecting the points and use a blue line for the symbol
    val locationHistoryLineOverlay = GraphicsOverlay()
    val locationLineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GREEN, 2.0f)
    locationHistoryLineOverlay.renderer = SimpleRenderer(locationLineSymbol)

    mapView.apply {
      // set the map to a dark gray canvas basemap
      map = ArcGISMap("https://www.arcgis.com/home/item.html?id=1970c1995b8f44749f4b9b6e81b5ba45")
      // set the viewpoint
      setViewpoint(Viewpoint(center, 7000.0))
      // add the graphics overlays to the map view
      graphicsOverlays.addAll(listOf(locationHistoryOverlay, locationHistoryLineOverlay))
    }

    // create a polyline builder to connect the location points
    val polylineBuilder = PolylineBuilder(SpatialReference.create(102100))

    // create a simulated location data source from json data with simulation parameters to set a consistent velocity
    val simulatedLocationDataSource = SimulatedLocationDataSource().apply {
      setLocations(
        Polyline.fromJson(getString(R.string.polyline_data)) as Polyline,
        SimulationParameters(Calendar.getInstance(), 30.0, 0.0, 0.0)
      )
    }

    simulatedLocationDataSource.addLocationChangedListener { locationChangedEvent ->
      // get the point from the location changed event
      val nextPoint = locationChangedEvent.location.position
      // add the new point to the polyline builder
      polylineBuilder.addPoint(nextPoint)
      // add the new point to the two graphics overlays and reset the line connecting the points
      locationHistoryOverlay.graphics.add(Graphic(nextPoint))
      locationHistoryLineOverlay.graphics.apply {
        clear()
        add(Graphic(polylineBuilder.toGeometry()))
      }
    }

    // configure the map view's location display to follow the simulated location data source
    mapView.locationDisplay.apply {
      locationDataSource = simulatedLocationDataSource
      autoPanMode = LocationDisplay.AutoPanMode.RECENTER
      initialZoomScale = 7000.0
    }

    // start and stop the simulated location data source when the button is tapped
    button.setOnClickListener {
      if (simulatedLocationDataSource.isStarted) {
        simulatedLocationDataSource.stop()
        button.setImageResource(R.drawable.ic_my_location_white_24dp)
      } else {
        simulatedLocationDataSource.startAsync()
        button.setImageResource(R.drawable.ic_navigation_white_24dp)
      }
    }

    // make sure the floating action button doesn't obscure the attribution bar
    mapView.addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
      val layoutParams = button.layoutParams as CoordinatorLayout.LayoutParams
      layoutParams.bottomMargin += bottom - oldBottom
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
