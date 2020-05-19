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
package com.esri.arcgisruntime.sample.findroute

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.networkanalysis.DirectionManeuver
import com.esri.arcgisruntime.tasks.networkanalysis.Route
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask
import com.esri.arcgisruntime.tasks.networkanalysis.Stop
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  private val graphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with the basemap
    val map = ArcGISMap().apply {
      // set the basemap with the vector tiled layer from a service URL
      basemap = Basemap(
        ArcGISVectorTiledLayer(getString(R.string.navigation_vector))
      )
      // set initial viewpoint to San Diego
      initialViewpoint = Viewpoint(32.7157, -117.1611, 200000.0)
    }

    mapView.apply {
      // set the map to be displayed in this view
      this.map = map
      // ensure the floating action button moves to be above the attribution view
      addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
        val heightDelta = bottom - oldBottom
        (directionFab.layoutParams as ConstraintLayout.LayoutParams).bottomMargin += heightDelta
      }
      mapView.graphicsOverlays.add(graphicsOverlay)
    }

    // create the symbols for the route
    setupSymbols()

    // hide the bottom sheet and make the map view span the whole screen
    bottomSheet.visibility = View.INVISIBLE
    (mainContainer.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = 0

    // solve the route and display the bottom sheet when the FAB is clicked
    directionFab.setOnClickListener { solveRoute() }
  }

  /**
   * Solves the route using a Route Task, populates the navigation drawer with the directions,
   * and displays a graphic of the route on the map.
   */
  private fun solveRoute() {
    // create a route task instance
    val routeTask = RouteTask(this, getString(R.string.routing_service))

    // show the progress bar
    mainProgressBar.visibility = View.VISIBLE

    val listenableFuture = routeTask.createDefaultParametersAsync()
    listenableFuture.addDoneListener {
      try {
        if (listenableFuture.isDone) {
          val routeParams = listenableFuture.get()
          // create stops
          val stops = arrayListOf(
            Stop(Point(-117.15083257944445, 32.741123367963446, SpatialReferences.getWgs84())),
            Stop(Point(-117.15557279683529, 32.703360305883045, SpatialReferences.getWgs84()))
          )
          routeParams.apply {
            setStops(stops)
            // set return directions as true to return turn-by-turn directions in the route's directionManeuvers
            isReturnDirections = true
          }

          // solve the route
          val result = routeTask.solveRouteAsync(routeParams).get()
          val route = result.routes[0] as Route
          // create a simple line symbol for the route
          val routeSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f)
          // create a graphic for the route and add it to the graphics overlay
          graphicsOverlay.graphics.add(Graphic(route.routeGeometry, routeSymbol))

          // get the list of direction maneuvers and display it
          // NOTE: to get turn-by-turn directions the route parameters
          //  must have the isReturnDirections parameter set to true.
          val directions = route.directionManeuvers
          setupBottomSheet(directions)

          // when the route is solved, hide the FAB and the progress bar
          directionFab.visibility = View.GONE
          mainProgressBar.visibility = View.GONE
        } else {
          val error = "Error loading route parameters."
          Log.e(TAG, error)
          Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
      } catch (e: Exception) {
        val error = "Error creating route parameters: ${e.message}"
        Log.e(TAG, error)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
      }
    }
  }

  /**
   * Set up the source, destination and route symbols.
   */
  private fun setupSymbols() {
    try {
      val startDrawable =
        ContextCompat.getDrawable(this, R.drawable.ic_source) as BitmapDrawable?

      startDrawable?.let {
        val pinSourceSymbol = PictureMarkerSymbol.createAsync(it).get().apply {
          // make the graphic smaller
          width = 30f
          height = 30f
          offsetY = 20f
        }
        // create a point for the new graphic
        val sourcePoint = Point(
          -117.15083257944445, 32.741123367963446, SpatialReferences.getWgs84()
        )
        // create a graphic and it to the graphics overlay
        graphicsOverlay.graphics.add(Graphic(sourcePoint, pinSourceSymbol))
      }
    } catch (e: Exception) {
      val error = "Error loading picture marker symbol: ${e.message}"
      Log.e(TAG, error)
      Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }
    try {
      val endDrawable =
        ContextCompat.getDrawable(this, R.drawable.ic_destination) as BitmapDrawable?

      endDrawable?.let {
        val pinDestinationSymbol = PictureMarkerSymbol.createAsync(endDrawable).get().apply {
          // make the graphic smaller
          width = 30f
          height = 30f
          offsetY = 20f
        }
        // create a point for the new graphic
        val destinationPoint = Point(
          -117.15557279683529,
          32.703360305883045,
          SpatialReferences.getWgs84()
        )
        // create a graphic and add it to the graphics overlay
        graphicsOverlay.graphics.add(Graphic(destinationPoint, pinDestinationSymbol))
      }
    } catch (e: Exception) {
      val error = "Error loading picture marker symbol: ${e.message}"
      Log.e(TAG, error)
      Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }
  }

  /** Creates a bottom sheet to display a list of direction maneuvers.
   *
   * @param directions a list of DirectionManeuver which represents the route
   */
  private fun setupBottomSheet(directions: List<DirectionManeuver>) {
    // create a bottom sheet behavior from the bottom sheet view in the main layout
    val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
      // expand the bottom sheet, and ensure it is displayed on the screen when collapsed
      state = BottomSheetBehavior.STATE_EXPANDED
      peekHeight = bottomSheet.header.height
      // animate the arrow when the bottom sheet slides
      addBottomSheetCallback(object : BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
          bottomSheet.header.imageView.rotation = slideOffset * 180f
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
          bottomSheet.header.imageView.rotation = when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> 180f
            else -> bottomSheet.header.imageView.rotation
          }
        }
      })
    }

    bottomSheet.apply {
      visibility = View.VISIBLE
      // expand or collapse the bottom sheet when the header is clicked
      header.setOnClickListener {
        bottomSheetBehavior.state = when (bottomSheetBehavior.state) {
          BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
          else -> BottomSheetBehavior.STATE_COLLAPSED
        }
      }
      // rotate the arrow so it starts off in the correct rotation
      header.imageView.rotation = 180f
    }

    bottomSheet.directionsListView.apply {
      // Set the adapter for the list view
      adapter = ArrayAdapter(
        this@MainActivity,
        android.R.layout.simple_list_item_1,
        directions.map { it.directionText }
      )
      // when the user taps a maneuver, set the viewpoint to that portion of the route
      onItemClickListener =
        AdapterView.OnItemClickListener { _, _, position, _ ->
          // remove any graphics that are not the two stops and the route graphic
          if (graphicsOverlay.graphics.size > 3) {
            graphicsOverlay.graphics.removeAt(graphicsOverlay.graphics.size - 1)
          }
          // set the viewpoint to the selected maneuver
          val geometry = directions[position].geometry
          mapView.setViewpointAsync(
            Viewpoint(geometry.extent, 20.0),
            1f
          )
          // create a graphic with a symbol for the maneuver and add it to the graphics overlay
          val selectedRouteSymbol = SimpleLineSymbol(
            SimpleLineSymbol.Style.SOLID,
            Color.GREEN, 5f
          )
          graphicsOverlay.graphics.add(Graphic(geometry, selectedRouteSymbol))
          // collapse the bottom sheet
          bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }
    // shrink the map view so it is not hidden under the bottom sheet header
    (mainContainer.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin =
      bottomSheet.header.height
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
