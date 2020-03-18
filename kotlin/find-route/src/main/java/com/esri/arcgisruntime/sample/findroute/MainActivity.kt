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

import android.app.ProgressDialog
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
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  private var routeSymbol: SimpleLineSymbol? = null
  private val graphicsOverlay: GraphicsOverlay by lazy {
    GraphicsOverlay().also {
      // add the overlay to the map view
      mapView.graphicsOverlays.add(it)
    }
  }
  private var drawerToggle: ActionBarDrawerToggle? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // create new Vector Tiled Layer from service url
    val vectorTiledLayer = ArcGISVectorTiledLayer(
      resources.getString(R.string.navigation_vector)
    )
    // create a map with the tiled layer basemap
    ArcGISMap(Basemap(vectorTiledLayer)).let { map ->
      // set initial viewpoint to San Diego
      map.initialViewpoint = Viewpoint(32.7157, -117.1611, 200000.0)
      // set the map to be displayed in this view
      mapView.map = map
    }
    // update UI when attribution view changes
    val params = directionFab.layoutParams as FrameLayout.LayoutParams
    mapView.addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
      val heightDelta = bottom - oldBottom
      params.bottomMargin += heightDelta
    }
    setupDrawer()
    setupSymbols()
    val progressDialog = ProgressDialog(this)
    progressDialog.setTitle(getString(R.string.progress_title))
    progressDialog.setMessage(getString(R.string.progress_message))
    directionFab.setOnClickListener {
      progressDialog.show()
      supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setHomeButtonEnabled(true)
        title = getString(R.string.app_name)
      }
      drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
      // create RouteTask instance
      val routeTask = RouteTask(this, getString(R.string.routing_service))
      val listenableFuture = routeTask.createDefaultParametersAsync()
      listenableFuture.addDoneListener {
        try {
          if (listenableFuture.isDone) {
            val routeParams = listenableFuture.get()
            // create stops
            val stop1 =
              Stop(
                Point(
                  -117.15083257944445,
                  32.741123367963446,
                  SpatialReferences.getWgs84()
                )
              )
            val stop2 =
              Stop(
                Point(
                  -117.15557279683529,
                  32.703360305883045,
                  SpatialReferences.getWgs84()
                )
              )
            val routeStops: MutableList<Stop> = arrayListOf(stop1, stop2)
            routeParams.setStops(routeStops)
            // set return directions as true to return turn-by-turn directions in the result of
            // getDirectionManeuvers().
            routeParams.isReturnDirections = true
            // solve
            val result = routeTask.solveRouteAsync(routeParams).get()
            val routes: List<*> = result.routes
            val route = routes[0] as Route
            // create a mRouteSymbol graphic
            val routeGraphic = Graphic(route.routeGeometry, routeSymbol)
            // add mRouteSymbol graphic to the map
            graphicsOverlay.graphics.add(routeGraphic)
            // get directions
            // NOTE: to get turn-by-turn directions Route Parameters should set returnDirection flag as true
            val directions = route.directionManeuvers
//            val directionsArray = arrayOfNulls<String>(directions.size)
            val directionsArray = Array<String>(directions.size) { i ->
              directions[i].directionText
            }

            Log.d(TAG, directions[0].geometry.extent.xMin.toString() + "")
            Log.d(TAG, directions[0].geometry.extent.yMin.toString() + "")
            // Set the adapter for the list view
            left_drawer.adapter = ArrayAdapter(
              applicationContext,
              R.layout.directions_layout,
              directionsArray
            )
            if (progressDialog.isShowing) {
              progressDialog.dismiss()
            }
            left_drawer.onItemClickListener =
              AdapterView.OnItemClickListener { _, _, position, _ ->
                if (graphicsOverlay.graphics.size > 3) {
                  graphicsOverlay.graphics
                    .removeAt(graphicsOverlay.graphics.size - 1)
                }
                drawer_layout.closeDrawers()
                val directionManeuver = directions[position]
                val geometry = directionManeuver.geometry
                val viewpoint = Viewpoint(geometry.extent, 20.0)
                mapView.setViewpointAsync(viewpoint, 3f)
                val selectedRouteSymbol = SimpleLineSymbol(
                  SimpleLineSymbol.Style.SOLID,
                  Color.GREEN, 5f
                )
                val selectedRouteGraphic = Graphic(
                  directions[position].geometry,
                  selectedRouteSymbol
                )
                graphicsOverlay.graphics.add(selectedRouteGraphic)
              }
          }
        } catch (e: Exception) {
          Log.e(
            TAG,
            e.message ?: ""
          )
        }
      }
    }
  }

  /**
   * Set up the Source, Destination and mRouteSymbol graphics symbol
   */
  private fun setupSymbols() {
    //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
//Create a picture marker symbol from an app resource
    val startDrawable =
      ContextCompat.getDrawable(this, R.drawable.ic_source) as BitmapDrawable?
    val pinSourceSymbol: PictureMarkerSymbol
    try {
      pinSourceSymbol = PictureMarkerSymbol.createAsync(startDrawable).get()
      pinSourceSymbol.loadAsync()
      pinSourceSymbol.addDoneLoadingListener {
        // add a new graphic as start point
        val sourcePoint = Point(
          -117.15083257944445,
          32.741123367963446,
          SpatialReferences.getWgs84()
        )
        val pinSourceGraphic = Graphic(sourcePoint, pinSourceSymbol)
        graphicsOverlay.graphics.add(pinSourceGraphic)
      }
      pinSourceSymbol.offsetY = 20f
    } catch (e: InterruptedException) {
      e.printStackTrace()
    } catch (e: ExecutionException) {
      e.printStackTrace()
    }
    //[DocRef: END]
    val endDrawable =
      ContextCompat.getDrawable(this, R.drawable.ic_destination) as BitmapDrawable?
    val pinDestinationSymbol: PictureMarkerSymbol
    try {
      pinDestinationSymbol = PictureMarkerSymbol.createAsync(endDrawable).get()
      pinDestinationSymbol.loadAsync()
      pinDestinationSymbol.addDoneLoadingListener {
        //add a new graphic as end point
        val destinationPoint = Point(
          -117.15557279683529,
          32.703360305883045,
          SpatialReferences.getWgs84()
        )
        val destinationGraphic = Graphic(destinationPoint, pinDestinationSymbol)
        graphicsOverlay.graphics.add(destinationGraphic)
      }
      pinDestinationSymbol.offsetY = 20f
    } catch (e: InterruptedException) {
      e.printStackTrace()
    } catch (e: ExecutionException) {
      e.printStackTrace()
    }
    //[DocRef: END]
    routeSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f)
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

  /**
   * set up the drawer
   */
  private fun setupDrawer() {
    drawerToggle = object : ActionBarDrawerToggle(
      this,
      drawer_layout,
      R.string.drawer_open,
      R.string.drawer_close
    ) {
      /** Called when a drawer has settled in a completely open state.  */
      override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
        invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
      }

      /** Called when a drawer has settled in a completely closed state.  */
      override fun onDrawerClosed(view: View) {
        super.onDrawerClosed(view)
        invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
      }
    }
    drawerToggle?.isDrawerIndicatorEnabled = true
    drawer_layout.addDrawerListener(drawerToggle as ActionBarDrawerToggle)
    drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    // Sync the toggle state after onRestoreInstanceState has occurred.
    drawerToggle?.syncState()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
//    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    super.onConfigurationChanged(newConfig)
    drawerToggle?.onConfigurationChanged(newConfig)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle action bar item clicks here. The action bar will
// automatically handle clicks on the Home/Up button, so long
// as you specify a parent activity in AndroidManifest.xml.
// Activate the navigation drawer toggle
    return drawerToggle?.onOptionsItemSelected(item) ?: super.onOptionsItemSelected(item)
  }

}