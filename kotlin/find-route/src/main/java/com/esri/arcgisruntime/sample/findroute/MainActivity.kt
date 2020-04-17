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
import androidx.appcompat.app.AlertDialog
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
import com.esri.arcgisruntime.tasks.networkanalysis.Route
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask
import com.esri.arcgisruntime.tasks.networkanalysis.Stop
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  private val bottomSheetBehavior: BottomSheetBehavior<View> by lazy {
    // create a bottom sheet behavior from the view included in the main layout
    BottomSheetBehavior.from(bottomSheet)
  }

  private val graphicsOverlay: GraphicsOverlay by lazy {
    // create a graphics overlay and add it to the map view
    GraphicsOverlay().also {
      mapView.graphicsOverlays.add(it)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create new basemap with the vector tiled layer from a service url
    val basemap = Basemap(
      ArcGISVectorTiledLayer(resources.getString(R.string.navigation_vector))
    )
    // create a map with the basemap
    // create a map 
    val map = ArcGISMap().apply {
      // set the basemap with the vector tiled layer from a service URL
      basemap = Basemap(ArcGISVectorTiledLayer(resources.getString(R.string.navigation_vector)))

      // set initial viewpoint to San Diego
      initialViewpoint = Viewpoint(32.7157, -117.1611, 200000.0)
    }

    // set the map to be displayed in this view
    mapView.map = map
      // set initial viewpoint to San Diego
      map.initialViewpoint = Viewpoint(32.7157, -117.1611, 200000.0)
      // set the map to be displayed in this view
      mapView.map = map
    }

    // ensure the floating action button moves to be above the attribution view
    val params = directionFab.layoutParams as ConstraintLayout.LayoutParams
    mapView.addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
      val heightDelta = bottom - oldBottom
      params.bottomMargin += heightDelta
    }

    // hide the bottom sheet
    bottomSheetBehavior.peekHeight = 0
    // animate the arrow when the bottom sheet slides
    bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        bottomSheet.header.imageView.rotation = slideOffset * 180f
      }

      override fun onStateChanged(bottomSheet: View, newState: Int) {
        bottomSheet.header.imageView.rotation = when (newState) {
          BottomSheetBehavior.STATE_EXPANDED -> 180f
          else -> 0f
        }
      }
    })
    // while the bottom sheet is hidden, make the map view span the whole screen
    val mainContainerParams = mainContainer.layoutParams as CoordinatorLayout.LayoutParams
    mainContainerParams.bottomMargin = 0

    // create the symbols for the route
    setupSymbols()

    // solve the route and display the bottom sheet when the FAB is clicked
    directionFab.setOnClickListener {
      bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
      bottomSheetBehavior.peekHeight = bottomSheet.header.height
      mainContainerParams.bottomMargin = bottomSheet.header.height
      solveRoute()
    }
  }

  /**
   * Solves the route using a Route Task, populates the navigation drawer with the directions,
   * and displays a graphic of the route on the map.
   */
  private fun solveRoute() {
    // create a route task instance
    val routeTask = RouteTask(this, getString(R.string.routing_service))
    // create an alert dialog for showing load progress
    val progressDialog = createProgressDialog(routeTask)
    progressDialog.show()

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
          // create a graphic for the route
          Graphic(route.routeGeometry, routeSymbol).also {
            // add the graphic to the map
            graphicsOverlay.graphics.add(it)
          }

          // get directions
          // NOTE: to get turn-by-turn directions the route parameters
          //  must have the isReturnDirections parameter set to true.
          val directions = route.directionManeuvers
          val directionsArray = Array<String>(directions.size) { i ->
            directions[i].directionText
          }

          if (progressDialog.isShowing) {
            progressDialog.dismiss()
          }

          // expand or collapse the bottom sheet when the header is clicked
          bottomSheet.header.setOnClickListener {
            bottomSheetBehavior.state = when (bottomSheetBehavior.state) {
              BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
              else -> BottomSheetBehavior.STATE_COLLAPSED
            }
          }

          bottomSheet.directionsListView.apply {
            // Set the adapter for the list view
            adapter = ArrayAdapter(
              this@MainActivity,
              android.R.layout.simple_list_item_1,
              directionsArray
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
                Graphic(geometry, selectedRouteSymbol).also {
                  graphicsOverlay.graphics.add(it)
                }

                // collapse the bottom sheet
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
              }
          }

          // when the route is solved, hide the FAB
          directionFab.visibility = View.GONE
        } else {
          val message = "Error loading route parameters."
          Log.e(TAG, message)
          Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
      } catch (e: Exception) {
        val message = "Error creating route parameters: ${e.message}"
        Log.e(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
      }
    }
  }

  /**
   * Set up the source, destination and route symbols.
   */
  private fun setupSymbols() {
    //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
    // create a picture marker symbol from an app resource
    try {

      val startDrawable =
        ContextCompat.getDrawable(this, R.drawable.ic_source) as BitmapDrawable?

      val pinSourceSymbol = PictureMarkerSymbol.createAsync(startDrawable).get()
      pinSourceSymbol.apply {
        loadAsync()
        addDoneLoadingListener {
          // add a new graphic as start point
          val sourcePoint = Point(
            -117.15083257944445,
            32.741123367963446,
            SpatialReferences.getWgs84()
          )
          Graphic(sourcePoint, this).also { pinSourceGraphic ->
            graphicsOverlay.graphics.add(pinSourceGraphic)
          }
        }
        offsetY = 20f
      }
    } catch (e: Exception) {
      val message = "Error loading picture marker symbol: ${e.message}"
      Log.e(TAG, message)
      Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    //[DocRef: END]
    try {

      val endDrawable =
        ContextCompat.getDrawable(this, R.drawable.ic_destination) as BitmapDrawable?

      val pinDestinationSymbol = PictureMarkerSymbol.createAsync(endDrawable).get()
      pinDestinationSymbol.apply {
        loadAsync()
        addDoneLoadingListener {
          // add a new graphic as end point
          val destinationPoint = Point(
            -117.15557279683529,
            32.703360305883045,
            SpatialReferences.getWgs84()
          )
          Graphic(destinationPoint, this).also { pinDestinationGraphic ->
            graphicsOverlay.graphics.add(pinDestinationGraphic)
          }
        }
        offsetY = 20f
      }
    } catch (e: Exception) {
      val message = "Error loading picture marker symbol: ${e.message}"
      Log.e(TAG, message)
      Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    //[DocRef: END]
  }

  /** Create a progress dialog box for tracking the route task.
   *
   * @param routeTask the route task progress to be tracked
   * @return an AlertDialog set with the dialog layout view
   */
  private fun createProgressDialog(routeTask: RouteTask) =
    AlertDialog.Builder(this@MainActivity)
      .setTitle("Solving route...")
      // provide a cancel button on the dialog
      .setNegativeButton("Cancel") { _, _ ->
        routeTask.cancelLoad()
      }
      .setCancelable(false)
      .setView(R.layout.dialog_layout)
      .create()

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
