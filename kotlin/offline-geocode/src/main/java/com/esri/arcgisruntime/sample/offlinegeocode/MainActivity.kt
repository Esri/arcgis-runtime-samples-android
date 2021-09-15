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

package com.esri.arcgisruntime.sample.offlinegeocode

import android.database.MatrixCursor
import android.graphics.Color
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.MotionEvent
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.esri.arcgisruntime.tasks.geocode.ReverseGeocodeParameters
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  private val geocodeParameters: GeocodeParameters by lazy {
    GeocodeParameters().apply {
      // get all attributes
      resultAttributeNames.add("*")
      // get only the closest result
      maxResults = 1
    }
  }

  private val reverseGeocodeParameters: ReverseGeocodeParameters by lazy {
    ReverseGeocodeParameters().apply {
      // get all attributes
      resultAttributeNames.add("*")
      // use the map's spatial reference
      outputSpatialReference = mapView.map.spatialReference
      // get only the closest result
      maxResults = 1
    }
  }

  private val locatorTask: LocatorTask by lazy {
    LocatorTask(
      getExternalFilesDir(null)?.path + resources.getString(R.string.san_diego_loc)
    )
  }

  // create a point symbol for showing the address location
  private val pointSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 20.0f)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // load the tile cache from local storage
    val tileCache = TileCache(getExternalFilesDir(null)?.path + getString(R.string.san_diego_tpkx))
    // use the tile cache extent to set the view point
    tileCache.addDoneLoadingListener { mapView.setViewpoint(Viewpoint(tileCache.fullExtent)) }
    // create a tiled layer from the tile cache
    val tiledLayer = ArcGISTiledLayer(tileCache)
    // set up the map view
    mapView.apply {
      // create a map with the tiled layer base map
      map = ArcGISMap(Basemap(tiledLayer))
      // add a graphics overlay to the map view
      graphicsOverlays.add(GraphicsOverlay())
      // add a touch listener to the map view
      onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
          val screenPoint = android.graphics.Point(e.x.roundToInt(), e.y.toInt())
          reverseGeocode(mapView.screenToLocation(screenPoint))
          return true
        }

        override fun onDoubleTouchDrag(e: MotionEvent): Boolean {
          return onSingleTapConfirmed(e)
        }
      }
    }
    // load the locator task from external storage
    locatorTask.loadAsync()
    locatorTask.addDoneLoadingListener { setupAddressSearchView() }
  }

  /**
   * Use the locator task to geocode the given address.
   *
   * @param address as a string to geocode
   */
  private fun geocode(address: String) {
    // execute async task to find the address
    locatorTask.addDoneLoadingListener {
      if (locatorTask.loadStatus != LoadStatus.LOADED) {
        val error =
          "Error loading locator task: " + locatorTask.loadError.message
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
        return@addDoneLoadingListener
      }
      // get a list of geocode results for the given address
      val geocodeFuture: ListenableFuture<List<GeocodeResult>> =
        locatorTask.geocodeAsync(address, geocodeParameters)
      geocodeFuture.addDoneListener {
        try {
          // get the geocode results
          val geocodeResults = geocodeFuture.get()
          if (geocodeResults.isEmpty()) {
            Toast.makeText(this, "No location found for: $address", Toast.LENGTH_LONG).show()
            return@addDoneListener
          }
          // get the first result
          val geocodeResult = geocodeResults[0]
          displayGeocodeResult(geocodeResult.displayLocation, geocodeResult.label)

        } catch (e: Exception) {
          val error = "Error getting geocode result: " + e.message
          Toast.makeText(this, error, Toast.LENGTH_LONG).show()
          Log.e(TAG, error)
        }
      }
    }
  }

  /**
   * Uses the locator task to reverse geocode the given point.
   *
   * @param point on which to perform the reverse geocode
   */
  private fun reverseGeocode(point: Point) {
    val results = locatorTask.reverseGeocodeAsync(point, reverseGeocodeParameters)
    try {
      val geocodeResults = results.get()
      if (geocodeResults.isEmpty()) {
        Toast.makeText(this, "No addresses found at that location!", Toast.LENGTH_LONG).show()
        return
      }
      // get the top result
      val geocode = geocodeResults[0]
      // attributes from a click-based search
      val street = geocode.attributes["StAddr"].toString()
      val city = geocode.attributes["City"].toString()
      val state = geocode.attributes["Region"].toString()
      val zip = geocode.attributes["Postal"].toString()
      val detail = "$city, $state $zip"
      val address = "$street, $detail"
      displayGeocodeResult(point, address)

    } catch (e: Exception) {
      val error = "Error getting geocode results: " + e.message
      Toast.makeText(this, error, Toast.LENGTH_LONG).show()
      Log.e(TAG, error)
    }
  }

  /**
   * Draw a point and open a callout showing geocode results on map.
   *
   * @param resultPoint geometry to show where the geocode result is
   * @param address     to display in the associated callout
   */
  private fun displayGeocodeResult(resultPoint: Point, address: CharSequence) {
    // dismiss the callout if showing
    if (mapView.callout.isShowing) {
      mapView.callout.dismiss()
    }
    val graphicsOverlay = mapView.graphicsOverlays[0]
    // remove any previous graphics/search results
    graphicsOverlay.graphics.clear()
    // create graphic object for resulting location and add it to the ographics overlay
    graphicsOverlay.graphics.add(Graphic(resultPoint, pointSymbol))
    // zoom map to geocode result location
    mapView.setViewpointAsync(Viewpoint(resultPoint, 8000.0), 3f)
    showCallout(resultPoint, address)
  }

  /**
   * Show a callout at the given point with the given text.
   *
   * @param point to define callout location
   * @param calloutText to define callout content
   */
  private fun showCallout(point: Point, calloutText: CharSequence) {
    val calloutTextView = TextView(this).apply {
      text = calloutText
    }
    mapView.callout.apply {
      location = point
      content = calloutTextView
    }
    mapView.callout.show()
  }

  /**
   * Sets up the address SearchView and uses MatrixCursor to show suggestions to the user.
   */
  private fun setupAddressSearchView() {
    // get the list of pre-made suggestions
    val suggestions = resources.getStringArray(R.array.suggestion_items)

    // set up parameters for searching with MatrixCursor
    val columnNames = arrayOf(BaseColumns._ID, "address")
    val suggestionsCursor = MatrixCursor(columnNames)

    // add each address suggestion to a new row
    suggestions.forEachIndexed { i, s -> suggestionsCursor.addRow(arrayOf(i, s)) }

    // create the adapter for the search view's suggestions
    searchView.apply {
      suggestionsAdapter = SimpleCursorAdapter(
        this@MainActivity,
        R.layout.suggestion,
        suggestionsCursor,
        arrayOf("address"),
        intArrayOf(R.id.suggestion_address),
        0
      )
      // show the suggestions as soon as the user opens the search view
      findViewById<AutoCompleteTextView>(R.id.search_src_text).threshold = 0
      // geocode the searched address on submit
      setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(address: String): Boolean {
          geocode(address)
          searchView.clearFocus()
          return true
        }

        override fun onQueryTextChange(newText: String?) = true
      })

      // geocode a suggestions when selected
      setOnSuggestionListener(object : SearchView.OnSuggestionListener {
        override fun onSuggestionSelect(position: Int) = true

        override fun onSuggestionClick(position: Int): Boolean {
          geocode(suggestions[position])
          return true
        }
      })
    }
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
