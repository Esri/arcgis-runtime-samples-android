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

package com.esri.arcgisruntime.sample.findaddress

import android.database.MatrixCursor
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val TAG: String = MainActivity::class.java.simpleName
  private var callout: Callout? = null
  private var addressGeocodeParameters: GeocodeParameters? = null
  // create a picture marker symbol
  private val pinSourceSymbol: PictureMarkerSymbol? by lazy { createPinSymbol() }
  // create a locator task from an online service
  private val locatorTask: LocatorTask by lazy {
    LocatorTask("https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer")
  }
  // create a new Graphics Overlay
  private val graphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create a map with the streets vector basemap type
    val topographicMap = ArcGISMap(BasemapStyle.ARCGIS_STREETS)

    mapView.apply {
      // set the map to be displayed in the mapview
      map = topographicMap
      // set the map viewpoint to start over North America
      setViewpoint(Viewpoint(40.0, -100.0, 100000000.0))
      // define the graphics overlay and add it to the map view
      graphicsOverlays.add(graphicsOverlay)
      // add listener to handle screen taps
      onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
        override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
          identifyGraphic(motionEvent)
          return true
        }
      }
    }

    // create the picture marker symbol to show address location
    createPinSymbol()

    // populate the spinner list of address suggestions
    val examples = arrayOf("277 N Avenida Caballeros, Palm Springs, " +
        "CA", "380 New York St, Redlands, CA 92373", "Београд", "Москва", "北京")
    // initialize an adapter for the suggestions spinner
    val suggestionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, examples)
    suggestionSpinner.adapter = suggestionAdapter

    // when an item is selected in the spinner set, go to that address
    suggestionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        geoCodeTypedAddress(suggestionSpinner.selectedItem.toString())
      }
      override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    setupAddressSearchView()
  }

  /**
   * Sets up the address SearchView and uses MatrixCursor to show suggestions to the user as text is entered.
   */
  private fun setupAddressSearchView() {
    addressGeocodeParameters = GeocodeParameters().apply {
      // get place name and street address attributes
      resultAttributeNames.addAll(listOf("PlaceName", "Place_addr"))
      // return only the closest result
      maxResults = 1

      addressSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(address: String): Boolean {
          // geocode typed address
          geoCodeTypedAddress(address)
          // clear focus from search views
          addressSearchView.clearFocus()
          return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
         // if the newText string isn't empty, get suggestions from the locator task
          if (newText.isNotEmpty()){
            val suggestionsFuture = locatorTask.suggestAsync(newText)
            suggestionsFuture.addDoneListener{
              try {
                // get the results of the async operation
                val suggestResults = suggestionsFuture.get()
                // set up parameters for searching with MatrixCursor
                val address = "address"
                val columnNames = arrayOf(BaseColumns._ID, address)
                val suggestionsCursor = MatrixCursor(columnNames)

                // add each address suggestion to a new row
                for ((key, result) in suggestResults.withIndex()) {
                  suggestionsCursor.addRow(arrayOf<Any>(key, result.label))
                }
                // column names for the adapter to look at when mapping data
                val cols = arrayOf(address)
                // ids that show where data should be assigned in the layout
                val to = intArrayOf(R.id.suggestion_address)
                // define SimpleCursorAdapter
                val suggestionAdapter = SimpleCursorAdapter(this@MainActivity,
                  R.layout.suggestion, suggestionsCursor, cols, to, 0)

                addressSearchView.suggestionsAdapter = suggestionAdapter
                // handle an address suggestion being chosen
                addressSearchView.setOnSuggestionListener(object: SearchView.OnSuggestionListener{
                  override fun onSuggestionSelect(position: Int): Boolean { return false }
                  override fun onSuggestionClick(position: Int): Boolean {
                    // get the selected row
                    (suggestionAdapter.getItem(position) as? MatrixCursor)?.let { selectedRow ->
                      // get the row's index
                      val selectedCursorIndex = selectedRow.getColumnIndex(address)
                      // get the string from the row at index
                      val selectedAddress = selectedRow.getString(selectedCursorIndex)
                      // use clicked suggestion as query
                      addressSearchView.setQuery(selectedAddress, true)
                    }
                    return true
                  }
                })
              } catch (e: Exception) {
                Log.e(TAG, "Geocode suggestion error: " + e.message)
                Toast.makeText(applicationContext, "Geocode suggestion error", Toast.LENGTH_LONG)
                  .show()
              }
            }
          }
          return true
        }
      })
    }
  }

  /**
   * Geocode an address passed in by the user.
   *
   * @param address the address read in from searchViews
   */
  private fun geoCodeTypedAddress(address: String) {
    locatorTask.addDoneLoadingListener {
      if (locatorTask.loadStatus == LoadStatus.LOADED) {
        // run the locatorTask geocode task, passing in the address
        val geocodeResultFuture =
          locatorTask.geocodeAsync(address, addressGeocodeParameters)
        geocodeResultFuture.addDoneListener {
          try {
            // get the results of the async operation
            val geocodeResults = geocodeResultFuture.get()
            if (geocodeResults.isNotEmpty()) {
              displaySearchResultOnMap(geocodeResults[0])
            } else {
              Toast.makeText(
                applicationContext, getString(R.string.location_not_found) + address, Toast.LENGTH_LONG).show()
            }
          } catch (e: Exception) {
            when (e) {
              is ExecutionException, is InterruptedException -> {
                Log.e(TAG, "Geocode error: " + e.message)
                Toast.makeText(applicationContext, getString(R.string.geo_locate_error), Toast.LENGTH_LONG)
                  .show()
              }
              else -> throw e
            }
          }
        }
      } else {
        locatorTask.retryLoadAsync()
      }
    }
    locatorTask.loadAsync()
  }

  /**
   * Turns a GeocodeResult into a Point and adds it to a graphic overlay on the map.
   *
   * @param geocodeResult a single geocode result
   */
  private fun displaySearchResultOnMap(geocodeResult: GeocodeResult) {
    // clear graphics overlay of existing graphics
    graphicsOverlay.graphics.clear()
    // create graphic object for resulting location
    val resultPoint = geocodeResult.displayLocation
    val resultLocationGraphic = Graphic(resultPoint, geocodeResult.attributes, pinSourceSymbol)
    // add graphic to location layer
    graphicsOverlay.graphics.add(resultLocationGraphic)
    mapView.setViewpointAsync(Viewpoint(geocodeResult.extent), 1f)
    showCallout(resultLocationGraphic)
  }

  /**
   * Identifies and shows a call out on a tapped graphic.
   *
   * @param motionEvent the motion event containing a tapped screen point
   */
  private fun identifyGraphic(motionEvent: MotionEvent) {
    // get the screen point
    val screenPoint: android.graphics.Point = android.graphics.Point(
      motionEvent.x.roundToInt(), motionEvent.y.roundToInt()
    )
    // from the graphics overlay, get the graphics near the tapped location
    val identifyResultsFuture: ListenableFuture<IdentifyGraphicsOverlayResult> =
      mapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 10.0, false)
    identifyResultsFuture.addDoneListener{
      try {
        val identifyGraphicsOverlayResult: IdentifyGraphicsOverlayResult = identifyResultsFuture.get()
        val graphics = identifyGraphicsOverlayResult.graphics
        // get the first graphic identified
        if (graphics.isNotEmpty()) {
          val identifiedGraphic: Graphic = graphics[0]
          // show the callout of the identified graphic
          showCallout(identifiedGraphic)
        } else {
          // dismiss the callout if no graphic is identified (e.g. tapping away from the graphic)
          callout?.dismiss()
        }
      } catch (e: Exception) {
        Log.e(TAG, "Identify error: " + e.message)
      }
    }
  }

  /**
   * Shows the given graphic's attributes as a call out.
   *
   * @param graphic the graphic containing the attributes to be displayed
   */
  private fun showCallout(graphic: Graphic) {
    // create a text view for the callout
    val calloutContent = TextView(applicationContext).apply {
      setTextColor(Color.BLACK)
      // get the graphic attributes for place name and street address, and display them as text in the callout
      this.text = if (graphic.attributes["PlaceName"].toString().isNotEmpty()) {
        graphic.attributes["PlaceName"].toString() + "\n" + graphic.attributes["Place_addr"].toString()
      } else {
        graphic.attributes["Place_addr"].toString()
      }
    }
    // get the center of the graphic to set the callout location
    val centerOfGraphic = graphic.geometry.extent.center
    val calloutLocation = graphic.computeCalloutLocation(centerOfGraphic, mapView)

    callout = mapView.callout.apply {
      showOptions = Callout.ShowOptions(true, true, true)
      content = calloutContent
      // set the leader position using the callout location
      setGeoElement(graphic, calloutLocation)
      // show callout beneath graphic
      style.leaderPosition = Callout.Style.LeaderPosition.UPPER_MIDDLE
      // show the callout
      show()
    }
  }

  /**
   *  Creates a picture marker symbol from the pin icon, and sets it to half of its original size.
   */
  private fun createPinSymbol(): PictureMarkerSymbol? {
    val pinDrawable = ContextCompat.getDrawable(this, R.drawable.pin) as BitmapDrawable?
    val pinSymbol: PictureMarkerSymbol
    try {
      pinSymbol = PictureMarkerSymbol.createAsync(pinDrawable).get()
      pinSymbol.width = 19f
      pinSymbol.height = 72f
      return pinSymbol
    } catch (e: Exception) {
      when (e) {
        is ExecutionException, is InterruptedException -> {
          Log.e(TAG, "Picture Marker Symbol error: " + e.message)
          Toast.makeText(applicationContext, "Failed to load pin drawable.", Toast.LENGTH_LONG)
            .show()
        }
        else -> throw e
      }
    }
    return null
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
