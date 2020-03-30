/* Copyright 2017 Esri
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

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import kotlinx.android.synthetic.main.activity_main_new.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import java.util.concurrent.ExecutionException
import java.util.zip.Inflater

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  private val bottomSheetView: View by lazy { this.bottomSheet }

  private val bottomSheetBehavior: BottomSheetBehavior<View> by lazy { BottomSheetBehavior.from(bottomSheetView) }

  private val graphicsOverlay: GraphicsOverlay by lazy {
    // create a graphics overlay and add it to the map view
    GraphicsOverlay().also {
      mapView.graphicsOverlays.add(it)
    }
  }
//  private val drawerToggle: ActionBarDrawerToggle by lazy { setupDrawer() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main_new)
    // create new basemap with the vector tiled layer from a service url
    val basemap = Basemap(
      ArcGISVectorTiledLayer(resources.getString(R.string.navigation_vector))
    )
    // create a map with the basemap
    ArcGISMap(basemap).let { map ->
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

    bottomSheetBehavior.peekHeight = 0
    bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetCallback() {
      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        bottomSheetView.header.imageView.rotation = slideOffset * 180f
      }
      override fun onStateChanged(bottomSheet: View, newState: Int) {
        bottomSheetView.header.imageView.rotation = when (newState) {
          BottomSheetBehavior.STATE_EXPANDED -> 180f
          else -> 0f
        }
      }
    })

    val mainContainerParams = mainContainer.layoutParams as CoordinatorLayout.LayoutParams
    mainContainerParams.bottomMargin = 0

    setupSymbols()

    directionFab.setOnClickListener {
      bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
      bottomSheetBehavior.peekHeight = bottomSheetView.header.height
      mainContainerParams.bottomMargin = bottomSheetView.header.height
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
            // set return directions as true to return turn-by-turn directions in the result of
            // getDirectionManeuvers().
            // UNSURE: what's going on with this comment
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
          // NOTE: to get turn-by-turn directions Route Parameters should set returnDirection flag as true
          val directions = route.directionManeuvers
          val directionsArray = Array<String>(directions.size) { i ->
            directions[i].directionText
          }

          if (progressDialog.isShowing) {
            progressDialog.dismiss()
          }

//          bottomSheet.visibility = View.VISIBLE

          bottomSheetView.setOnClickListener {
            bottomSheetBehavior.state = when (bottomSheetBehavior.state){
                BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
                else -> BottomSheetBehavior.STATE_COLLAPSED
              }
          }

          bottomSheetView.directionsListView.apply {
            // Set the adapter for the list view
            adapter = ArrayAdapter(
              this@MainActivity,
              android.R.layout.simple_list_item_1,
              directionsArray
            )

            onItemClickListener =
              AdapterView.OnItemClickListener { _, _, position, _ ->
                if (graphicsOverlay.graphics.size > 3) {
                  graphicsOverlay.graphics.removeAt(graphicsOverlay.graphics.size - 1)
                }

                val geometry = directions[position].geometry
                mapView.setViewpointAsync(
                  Viewpoint(geometry.extent, 20.0),
                  1f
                )
                // create a graphic with a symbol for the route and add it to the graphics overlay
                val selectedRouteSymbol = SimpleLineSymbol(
                  SimpleLineSymbol.Style.SOLID,
                  Color.GREEN, 5f
                )
                Graphic(geometry, selectedRouteSymbol).also {
                  graphicsOverlay.graphics.add(it)
                }

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
              }
          }

          directionFab.visibility = View.GONE
        }
         //TODO: should there be an else here?
      } catch (e: Exception) {
        Log.e(TAG, "${e.message}")
      }
    }
  }


  /**
   * Set up the Source, Destination and routeSymbol graphics symbol
   */
  private fun setupSymbols() {
    //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
    // Create a picture marker symbol from an app resource
    try {
      val startDrawable =
        ContextCompat.getDrawable(this, R.drawable.ic_source) as BitmapDrawable?
      val pinSourceSymbol = PictureMarkerSymbol.createAsync(startDrawable).get()
      pinSourceSymbol.loadAsync()
      pinSourceSymbol.addDoneLoadingListener {
        // add a new graphic as start point
        val sourcePoint = Point(
          -117.15083257944445,
          32.741123367963446,
          SpatialReferences.getWgs84()
        )
        Graphic(sourcePoint, pinSourceSymbol).also {
          graphicsOverlay.graphics.add(it)
        }
      }
      pinSourceSymbol.offsetY = 20f
    } catch (e: InterruptedException) {
      e.printStackTrace()
    } catch (e: ExecutionException) {
      e.printStackTrace()
    }
    //[DocRef: END]
    try {
      val endDrawable =
        ContextCompat.getDrawable(this, R.drawable.ic_destination) as BitmapDrawable?
      val pinDestinationSymbol = PictureMarkerSymbol.createAsync(endDrawable).get()
      pinDestinationSymbol.loadAsync()
      pinDestinationSymbol.addDoneLoadingListener {
        // add a new graphic as end point
        val destinationPoint = Point(
          -117.15557279683529,
          32.703360305883045,
          SpatialReferences.getWgs84()
        )
        Graphic(destinationPoint, pinDestinationSymbol).also {
          graphicsOverlay.graphics.add(it)
        }
      }
      pinDestinationSymbol.offsetY = 20f
    } catch (e: InterruptedException) {
      e.printStackTrace()
    } catch (e: ExecutionException) {
      e.printStackTrace()
    }
    //[DocRef: END]
  }

  /** Create a progress dialog box for tracking the route task.
   *
   * @param routeTask the route task progress to be tracked
   * @return an AlertDialog set with the dialog layout view
   */
  private fun createProgressDialog(routeTask: RouteTask): AlertDialog {
    val builder = AlertDialog.Builder(this@MainActivity).apply {
      setTitle("Solving route...")
      // provide a cancel button on the dialog
      setNeutralButton("Cancel") { _, _ ->
        routeTask.cancelLoad()
      }
      setCancelable(false)
      setView(R.layout.dialog_layout)
    }
    return builder.create()
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