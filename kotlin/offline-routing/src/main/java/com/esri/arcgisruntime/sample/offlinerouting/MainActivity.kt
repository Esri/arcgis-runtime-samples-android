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

package com.esri.arcgisruntime.sample.offlinerouting

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.CompositeSymbol
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.TextSymbol
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask
import com.esri.arcgisruntime.tasks.networkanalysis.Stop
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val stopsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
  private val routeOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
  private var routeParameters: RouteParameters? = null
  private val routeTask: RouteTask by lazy {
    RouteTask(
      this,
      getExternalFilesDir(null)?.path + getString(R.string.geodatabase_path),
      "Streets_ND"
    )
  }

  private val TAG: String = MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a tile cache from the tpkx
    val tileCache = TileCache(getExternalFilesDir(null)?.path + getString(R.string.tpkx_path))
    val tiledLayer = ArcGISTiledLayer(tileCache)
    // make a basemap with the tiled layer and add it to the mapview as an ArcGISMap
    mapView.map = ArcGISMap(Basemap(tiledLayer))

    // add the graphics overlays to the map view
    mapView.graphicsOverlays.addAll(listOf(stopsOverlay, routeOverlay))

    // load the route task
    routeTask.loadAsync()
    routeTask.addDoneLoadingListener {
      if (routeTask.loadStatus == LoadStatus.LOADED) {
        try {
          // create route parameters
          routeParameters = routeTask.createDefaultParametersAsync().get()
        } catch (e: Exception) {
          val error = "Error getting default route parameters. ${e.message}"
          Log.e(TAG, error)
          Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
      } else {
        val error = "Error loading route task: ${routeTask.loadError.message}"
        Log.e(TAG, error)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
      }
    }

    // set up travel mode switch
    modeSwitch.setOnCheckedChangeListener { _, isChecked ->
      routeParameters?.travelMode = when (isChecked) {
        R.id.fastest_button -> routeTask.routeTaskInfo.travelModes[0]
        R.id.shortest_button -> routeTask.routeTaskInfo.travelModes[1]
        else -> routeTask.routeTaskInfo.travelModes[0]
      }
      Toast.makeText(
        this,
        "${routeParameters?.travelMode?.name} route selected.",
        Toast.LENGTH_SHORT
      ).show()
      updateRoute()
    }

    // make a clear button to reset the stops and routes
    clearButton.setOnClickListener {
      stopsOverlay.graphics.clear()
      routeOverlay.graphics.clear()
    }
    // move the clear button above the attribution bar
    mapView.addAttributionViewLayoutChangeListener { v, _, _, _, _, _, oldTop, _, oldBottom ->
      val heightChanged = v.height - (oldBottom - oldTop)
      clearButton.y += -heightChanged
    }
    // add a graphics overlay to show the boundary
    GraphicsOverlay().let {
      val envelope = Envelope(
        Point(-13045352.223196, 3864910.900750, 0.0, SpatialReferences.getWebMercator()),
        Point(-13024588.857198, 3838880.505604, 0.0, SpatialReferences.getWebMercator())
      )
      val boundarySymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF00FF00.toInt(), 5f)
      it.graphics.add(Graphic(envelope, boundarySymbol))
      mapView.graphicsOverlays.add(it)
    }

    // set up the touch listeners on the map view
    createMapGestures()
  }

  /**
   * Sets up the onTouchListener for the mapView.
   * For single taps, graphics will be selected.
   * For double touch drags, graphics will be moved.
   * */
  private fun createMapGestures() {
    mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
      override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {

        val screenPoint = android.graphics.Point(
          motionEvent.x.roundToInt(),
          motionEvent.y.roundToInt()
        )
        addOrSelectGraphic(screenPoint)
        return true
      }

      override fun onDoubleTouchDrag(motionEvent: MotionEvent): Boolean {

        val screenPoint = android.graphics.Point(
          motionEvent.x.roundToInt(),
          motionEvent.y.roundToInt()
        )

        // move the selected graphic to the new location
        if (stopsOverlay.selectedGraphics.isNotEmpty()) {
          stopsOverlay.selectedGraphics[0]?.geometry = mapView.screenToLocation(screenPoint)
          updateRoute()
        }
        // ignore default double touch drag gesture
        return true
      }

      // ignore default double tap gesture
      override fun onDoubleTap(e: MotionEvent?): Boolean {
        return true
      }
    }
  }

  /**
   * Updates the calculated route by calling routeTask.solveRouteAsync().
   * Creates a graphic to display the route.
   * */
  private fun updateRoute() {
    // get a list of stops from the graphics currently on the graphics overlay.
    val stops = stopsOverlay.graphics.map {
      Stop(it.geometry as Point)
    }

    // do not calculate a route if there is only one stop
    if (stops.size <= 1) return

    routeParameters?.setStops(stops)

    // solve the route
    val results = routeTask.solveRouteAsync(routeParameters)
    results.addDoneListener {
      try {
        val result = results.get()
        val route = result.routes[0]

        // create graphic for route
        val graphic = Graphic(
          route.routeGeometry, SimpleLineSymbol(
            SimpleLineSymbol.Style.SOLID,
            0xFF0000FF.toInt(), 3F
          )
        )

        routeOverlay.graphics.clear()
        routeOverlay.graphics.add(graphic)
      } catch (e: Exception) {
        val error = "No route solution. ${e.message}"
        Log.e(TAG, error)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        routeOverlay.graphics.clear()
      }
    }
  }

  /**
   * Selects a graphic if there is one at the provided tapped location or, if there is none, creates a new graphic.
   *
   * @param screenPoint a point in screen space where the user tapped
   * */
  private fun addOrSelectGraphic(screenPoint: android.graphics.Point) {
    // identify the selected graphic
    val results = mapView.identifyGraphicsOverlayAsync(stopsOverlay, screenPoint, 10.0, false)
    results.addDoneListener {
      try {
        val graphics = results.get().graphics
        // unselect everything
        if (stopsOverlay.selectedGraphics.isNotEmpty()) {
          stopsOverlay.unselectGraphics(stopsOverlay.selectedGraphics)
        }
        // if the user tapped on something, select it
        if (graphics.isNotEmpty()) {
          val firstGraphic = graphics[0]
          firstGraphic.isSelected = true
        } else { // there is no graphic at this location
          // make a new graphic at the tapped location
          val locationPoint = mapView.screenToLocation(screenPoint)
          createStopSymbol(stopsOverlay.graphics.size + 1, locationPoint)
        }
      } catch (e: Exception) {
        val error = "Error identifying graphic: ${e.stackTrace}"
        Log.e(TAG, error)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
      }
    }
  }

  /**
   * Creates a composite symbol to represent a numbered stop.
   *
   * @param stopNumber the ordinal number of this stop
   * @param locationPoint the point in map space where the symbol should be placed
   */
  private fun createStopSymbol(stopNumber: Int, locationPoint: Point) {
    try {
      // create a new picture marker symbol and load it
      val pictureMarkerSymbol = PictureMarkerSymbol.createAsync(
        ContextCompat.getDrawable(
          this,
          R.drawable.pin_symbol
        ) as BitmapDrawable
      ).get()
      // create a text symbol with the stop number
      val textSymbol = TextSymbol(
        12f,
        stopNumber.toString(),
        0xFFFFFFFF.toInt(),
        TextSymbol.HorizontalAlignment.CENTER,
        TextSymbol.VerticalAlignment.BOTTOM
      )
      textSymbol.offsetY = -4f
      // create a composite symbol and add the picture marker symbol and text symbol
      val compositeSymbol = CompositeSymbol()
      compositeSymbol.symbols.addAll(listOf(pictureMarkerSymbol, textSymbol))
      // create a graphic to add to the overlay and update the route
      val graphic = Graphic(locationPoint, compositeSymbol)
      stopsOverlay.graphics.add(graphic)
      updateRoute()

    } catch (e: Exception) {
      Log.e(TAG, "Failed to create composite symbol: ${e.stackTrace}")
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
