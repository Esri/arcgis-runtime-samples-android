/*
 * Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.routearoundbarriers

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.CompositeSymbol
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.symbology.TextSymbol
import com.esri.arcgisruntime.tasks.networkanalysis.DirectionManeuver
import com.esri.arcgisruntime.tasks.networkanalysis.PolygonBarrier
import com.esri.arcgisruntime.tasks.networkanalysis.Route
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask
import com.esri.arcgisruntime.tasks.networkanalysis.Stop
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.route_controls_bottom_sheet.*
import kotlinx.android.synthetic.main.route_controls_bottom_sheet.view.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val TAG: String = MainActivity::class.java.simpleName

  private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

  private var routeTask: RouteTask? = null
  private var routeParameters: RouteParameters? = null
  private var pinSymbol: PictureMarkerSymbol? = null

  private val routeGraphicsOverlay by lazy { GraphicsOverlay() }
  private val stopsGraphicsOverlay by lazy { GraphicsOverlay() }
  private val barriersGraphicsOverlay by lazy { GraphicsOverlay() }

  private val stopList by lazy { mutableListOf<Stop>() }
  private val barrierList by lazy { mutableListOf<PolygonBarrier>() }
  private val directionsList by lazy { mutableListOf<DirectionManeuver>() }

  private val routeLineSymbol by lazy {
    SimpleLineSymbol(
      SimpleLineSymbol.Style.SOLID,
      Color.BLUE,
      5.0f
    )
  }
  private val barrierSymbol by lazy {
    SimpleFillSymbol(
      SimpleFillSymbol.Style.DIAGONAL_CROSS,
      Color.RED,
      null
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create simple renderer for routes, and set it to use the line symbol
    routeGraphicsOverlay.renderer = SimpleRenderer().apply {
      symbol = routeLineSymbol
    }

    mapView.apply {
      // add a map with the streets basemap to the map view, centered on San Diego
      map = ArcGISMap(BasemapStyle.ARCGIS_STREETS)
      // center on San Diego
      setViewpoint(Viewpoint(32.7270, -117.1750, 40000.0))
      // add the graphics overlays to the map view
      graphicsOverlays.addAll(
        listOf(stopsGraphicsOverlay, barriersGraphicsOverlay, routeGraphicsOverlay)
      )
      onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
        override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
          val screenPoint =
            android.graphics.Point(motionEvent.x.roundToInt(), motionEvent.y.roundToInt())
          addStopOrBarrier(screenPoint)
          return true
        }
      }
    }

    // create a new picture marker from a pin drawable
    pinSymbol = PictureMarkerSymbol.createAsync(
      ContextCompat.getDrawable(
        this,
        R.drawable.pin_symbol
      ) as BitmapDrawable
    ).get().apply {
      width = 30f
      height = 30f
      offsetY = 20f
    }

    // create route task from San Diego service
    routeTask = RouteTask(
      this,
      "https://sampleserver6.arcgisonline.com/arcgis/rest/services/NetworkAnalysis/SanDiego/NAServer/Route"
    ).apply {
      addDoneLoadingListener {
        if (loadStatus == LoadStatus.LOADED) {
          // get default route parameters
          val routeParametersFuture = createDefaultParametersAsync()
          routeParametersFuture.addDoneListener {
            try {
              routeParameters = routeParametersFuture.get().apply {
                // set flags to return stops and directions
                isReturnStops = true
                isReturnDirections = true
              }
            } catch (e: Exception) {
              Log.e(TAG, "Cannot create RouteTask parameters " + e.message)
            }
          }
        } else {
          Log.e(TAG, "Unable to load RouteTask $loadStatus")
        }
      }
    }
    routeTask?.loadAsync()


    // shrink the map view so it is not hidden under the bottom sheet header
    bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
    (mapViewContainer.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin =
      (bottomSheetBehavior as BottomSheetBehavior<View>).peekHeight
    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED

    bottomSheet.apply {
      // expand or collapse the bottom sheet when the header is clicked
      header.setOnClickListener {
        bottomSheetBehavior?.state = when (bottomSheetBehavior?.state) {
          BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_HALF_EXPANDED
          else -> BottomSheetBehavior.STATE_COLLAPSED
        }
      }
      // rotate the arrow so it starts off in the correct rotation
      header.imageView.rotation = 180f
    }

    // change button toggle state on click
    addStopButton.setOnClickListener { addBarrierButton.isChecked = false }
    addBarrierButton.setOnClickListener { addStopButton.isChecked = false }

    // solve route on checkbox change state
    reorderCheckBox.setOnCheckedChangeListener { _, _ -> createAndDisplayRoute() }
    preserveFirstStopCheckBox.setOnCheckedChangeListener { _, _ -> createAndDisplayRoute() }
    preserveLastStopCheckBox.setOnCheckedChangeListener { _, _ -> createAndDisplayRoute() }

    // start sample with add stop button true
    addStopButton.isChecked = true
  }

  /**
   * Add a stop or a point to the correct graphics overlay depending on which button is currently
   * checked.
   *
   * @param screenPoint at which to create a stop or point
   */
  private fun addStopOrBarrier(screenPoint: android.graphics.Point) {
    // convert screen point to map point
    val mapPoint = mapView.screenToLocation(screenPoint)
    // normalize geometry - important for geometries that will be sent to a server for processing
    val normalizedPoint = GeometryEngine.normalizeCentralMeridian(mapPoint) as Point
    // clear the displayed route, if it exists, since it might not be up to date any more
    routeGraphicsOverlay.graphics.clear()
    if (addStopButton.isChecked) {
      // use the clicked map point to construct a stop
      val stopPoint = Stop(Point(normalizedPoint.x, normalizedPoint.y, mapPoint.spatialReference))
      // add the new stop to the list of stops
      stopList.add(stopPoint)
      // create a marker symbol and graphics, and add the graphics to the graphics overlay
      stopsGraphicsOverlay.graphics.add(Graphic(mapPoint, createCompositeStopSymbol(stopList.size)))
    } else if (addBarrierButton.isChecked) {
      // create a buffered polygon around the clicked point
      val bufferedBarrierPolygon = GeometryEngine.buffer(mapPoint, 200.0)
      // create a polygon barrier for the routing task, and add it to the list of barriers
      barrierList.add(PolygonBarrier(bufferedBarrierPolygon))
      // build graphics for the barrier and add it to the graphics overlay
      barriersGraphicsOverlay.graphics.add(Graphic(bufferedBarrierPolygon, barrierSymbol))
    }
    createAndDisplayRoute()
  }

  /**
   * Create route parameters and a route task from them. Display the route result geometry as a
   * graphic and call showDirectionsInBottomSheet which shows directions in a list view.
   */
  private fun createAndDisplayRoute() {
    if (stopList.size < 2) {
      // clear the directions list since no route is displayed
      directionsList.clear()
      return
    }

    // clear the previous route from the graphics overlay, if it exists
    routeGraphicsOverlay.graphics.clear()
    // clear the directions list from the directions list view, if they exist
    directionsList.clear()

    routeParameters?.apply {
      // add the existing stops and barriers to the route parameters
      setStops(stopList)
      setPolygonBarriers(barrierList)

      // apply the requested route finding parameters
      isFindBestSequence = reorderCheckBox.isChecked
      isPreserveFirstStop = preserveFirstStopCheckBox.isChecked
      isPreserveLastStop = preserveLastStopCheckBox.isChecked
    }

    // solve the route task
    val routeResultFuture = routeTask?.solveRouteAsync(routeParameters)
    routeResultFuture?.addDoneListener {
      try {
        val routeResult: RouteResult = routeResultFuture.get()
        if (routeResult.routes.isNotEmpty()) {
          // get the first route result
          val firstRoute: Route = routeResult.routes[0]

          // create a graphic for the route and add it to the graphics overlay
          val routeGraphic = Graphic(firstRoute.routeGeometry)
          routeGraphicsOverlay.graphics.add(routeGraphic)

          // get the direction text for each maneuver and add them to the list to display
          directionsList.addAll(firstRoute.directionManeuvers)

          showDirectionsInBottomSheet()
        } else {
          Toast.makeText(this, "No routes found.", Toast.LENGTH_LONG).show()
        }
      } catch (e: Exception) {
        val error = "Solve route task failed: " + e.message
        Log.e(TAG, error)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
      }

      // show the reset button
      resetButton.visibility = VISIBLE
    }
  }

  /**
   * Clear all stops and polygon barriers from the route parameters, stop and barrier
   * lists and all graphics overlays. Also hide the directions list view and show the control
   * layout.
   *
   * @param reset button which calls this method
   */
  fun clearRouteAndGraphics(reset: View) {
    // clear stops from route parameters and stops list
    routeParameters?.clearStops()
    stopList.clear()
    // clear barriers from route parameters and barriers list
    routeParameters?.clearPolygonBarriers()
    barrierList.clear()
    // clear the directions list
    directionsList.clear()
    // clear all graphics overlays
    mapView.graphicsOverlays.forEach { it.graphics.clear() }
    // hide the reset button and directions list
    resetButton.visibility = GONE
    // hide the directions
    directionsTextView.visibility = GONE
    directionsListView.visibility = GONE
  }

  /**
   * Create a composite symbol consisting of a pin graphic overlaid with a particular stop number.
   *
   * @param stopNumber to overlay the pin symbol
   * @return a composite symbol consisting of the pin graphic overlaid with an the stop number
   */
  private fun createCompositeStopSymbol(stopNumber: Int): CompositeSymbol {
    // determine the stop number and create a new label
    val stopTextSymbol = TextSymbol(
      16f,
      stopNumber.toString(),
      -0x1,
      TextSymbol.HorizontalAlignment.CENTER,
      TextSymbol.VerticalAlignment.BOTTOM
    )
    stopTextSymbol.offsetY = pinSymbol?.height as Float / 2
    // construct a composite symbol out of the pin and text symbols, and return it
    return CompositeSymbol(listOf(pinSymbol, stopTextSymbol))
  }

  /**
   * Creates a bottom sheet to display a list of direction maneuvers.
   */
  private fun showDirectionsInBottomSheet() {
    // show the directions list view
    directionsTextView.visibility = VISIBLE
    directionsListView.visibility = VISIBLE
    // create a bottom sheet behavior from the bottom sheet view in the main layout
    bottomSheetBehavior?.apply {
      // animate the arrow when the bottom sheet slides
      addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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

    bottomSheet.directionsListView.apply {
      // Set the adapter for the list view
      adapter = ArrayAdapter(
        this@MainActivity,
        android.R.layout.simple_list_item_1,
        directionsList.map { it.directionText })
      // when the user taps a maneuver, set the viewpoint to that portion of the route
      onItemClickListener =
        AdapterView.OnItemClickListener { _, _, position, _ ->
          // remove any graphics that are not the original (blue) route graphic
          if (routeGraphicsOverlay.graphics.size > 1) {
            routeGraphicsOverlay.graphics.removeAt(routeGraphicsOverlay.graphics.size - 1)
          }
          // set the viewpoint to the selected maneuver
          val geometry = directionsList[position].geometry
          mapView.setViewpointAsync(Viewpoint(geometry.extent, 20.0), 1f)
          // create a graphic with a symbol for the maneuver and add it to the graphics overlay
          val selectedRouteSymbol = SimpleLineSymbol(
            SimpleLineSymbol.Style.SOLID,
            Color.GREEN, 5f
          )
          routeGraphicsOverlay.graphics.add(Graphic(geometry, selectedRouteSymbol))
          // collapse the bottom sheet
          bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
      // allow the list view to scroll within bottom sheet
      isNestedScrollingEnabled = true
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
