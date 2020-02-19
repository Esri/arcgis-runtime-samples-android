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
import androidx.appcompat.widget.SearchView
//import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
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

  private var pinSourceSymbol: PictureMarkerSymbol? = null
  private val TAG: String = MainActivity::class.java.simpleName
  private lateinit var locatorTask: LocatorTask
  private var graphicsOverlay: GraphicsOverlay? = null
  private var callout: Callout? = null
  private var addressGeocodeParameters: GeocodeParameters? = null


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with the streets vector basemap type
    val topographicBasemap = ArcGISMap(Basemap.createStreetsVector())

    mapView.apply {
      // set the map to be displayed in the mapview
      map = topographicBasemap
      // set the map viewpoint to start over North America
      setViewpoint(Viewpoint(40.0, -100.0, 100000000.0))
      // define the graphics overlay and add it to the map view
      graphicsOverlay = GraphicsOverlay()
      graphicsOverlays.add(graphicsOverlay)
    }

    // create a locator task from an online service
    locatorTask = LocatorTask(getString(R.string.locator_task_uri))

    // create the picture market symbol to show address location
    createPinSymbol()

    // add listener to handle screen taps
    mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
      override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        identifyGraphic(motionEvent)
        return true
      }
    }

    setupAddressSearchView()
  }

  /**
   * Sets up the address SearchView. Uses MatrixCursor to show suggestions to the user as text is input.
   */
  private fun setupAddressSearchView() {
    addressGeocodeParameters = GeocodeParameters().apply {
      // get place name and street address attributes
      resultAttributeNames.addAll(listOf("PlaceName", "StAddr"))
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

        override fun onQueryTextChange(newText: String?): Boolean {
         // if the newText string isn't empty, get suggestions from the locator task
          if (!newText.equals("")) {
            val suggestionsFuture = locatorTask.suggestAsync(newText)
            suggestionsFuture.addDoneListener{
              try {
                // get the results of the async operation
                val suggestResults = suggestionsFuture.get()

                val COLUMN_NAME_ADDRESS = "address"
                val columnNames = arrayOf(BaseColumns._ID, COLUMN_NAME_ADDRESS)
                val suggestionsCursor = MatrixCursor(columnNames)

                var key = 0
                // add each address suggestion to a new row
                for (result in suggestResults) {
                  suggestionsCursor.addRow(arrayOf<Any>(key++, result.getLabel()))
                }
                // define SimpleCursorAdapter
                // column names for the adapter to look at when mapping data
                val cols = arrayOf(COLUMN_NAME_ADDRESS)
                // ids that show where data should be assigned in the layout
                val to = intArrayOf(R.id.suggestion_address)
                val suggestionAdapter = SimpleCursorAdapter(this@MainActivity, R.layout.suggestion,
                    suggestionsCursor, cols, to, 0)

                addressSearchView.suggestionsAdapter = suggestionAdapter
                // handle an address suggestion being chosen
                addressSearchView.setOnSuggestionListener(object: SearchView.OnSuggestionListener{
                  override fun onSuggestionSelect(position: Int): Boolean { return false }

                  override fun onSuggestionClick(position: Int): Boolean {
                    // get the selected row
                    val selectedRow = suggestionAdapter.getItem(position) as MatrixCursor
                    // get the row's index
                    val selectedCursorIndex = selectedRow.getColumnIndex(COLUMN_NAME_ADDRESS)
                    // get the  string from the tow at index
                    val address = selectedRow.getString(selectedCursorIndex)
                    // use clicked suggestion as query
                    addressSearchView.setQuery(address, true)
                    return true
                  }
                })
              } catch (e: Exception) {
                Log.e(TAG, "Geocode suggestion error: " + e.message)
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
    // check that the address isn't null TODO find a kotlin way of doing this
    locatorTask.addDoneLoadingListener {
      if (locatorTask.loadStatus == LoadStatus.LOADED) {
        // run the locatorTask geocode task, passing in the address
        val geocodeResultListenableFuture =
          locatorTask.geocodeAsync(address, addressGeocodeParameters)
        geocodeResultListenableFuture.addDoneListener {
          try {
            // get the results of the async operation
            val geocodeResults = geocodeResultListenableFuture.get()
            if (geocodeResults.size > 0) {
              displaySearchResultOnMap(geocodeResults[0])
            } else {
              Toast.makeText(
                applicationContext, "No location with that name: " + address, Toast.LENGTH_LONG).show()
            }
          } catch (e: Exception) {
            when (e) {
              is ExecutionException, is InterruptedException -> {
                Log.e(TAG, "Geocode error: " + e.message)
                Toast.makeText(applicationContext, "Geocode failed on address.", Toast.LENGTH_LONG)
                  .show()
              }
              else -> throw e
            }
          }
        }
      } else {
        Log.i(TAG, "Trying to reload locator task")
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
    // dismiss existing callout if present and showing
    if (mapView.getCallout() != null && mapView.getCallout().isShowing()) {
      mapView.getCallout().dismiss()
    }
    // clear graphics overlay of existing graphics TODO check that not also clearing the graphics overlay is ok, as per Java implementation (line 304 in java)
    graphicsOverlay?.graphics?.clear()
    // create graphic object for resulting location
    val resultPoint = geocodeResult.displayLocation
    val resultLocationGraphic = Graphic(resultPoint, geocodeResult.attributes, pinSourceSymbol)
    // add graphic to location layer
    graphicsOverlay?.graphics?.add(resultLocationGraphic)
    mapView.setViewpointAsync(Viewpoint(geocodeResult.extent, 3.0))
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
        if (graphics.size > 0) {
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
      val calloutText = graphic.attributes.get("PlaceName").toString() + "\n" +
          graphic.attributes.get("StAddr").toString()
      text = calloutText
    }
    // configure the callout
    callout = mapView.callout.apply {
      showOptions = Callout.ShowOptions(true, false, false)
      content = calloutContent
      // set the leader position using the center of the graphic
      val centerOfGraphic = graphic.geometry.extent.center
      val calloutLocation = graphic.computeCalloutLocation(centerOfGraphic, mapView)
      setGeoElement(graphic, calloutLocation)
      // show the callout
      show()
//      Toast.makeText(applicationContext, calloutLocation.x.toString(), Toast.LENGTH_LONG).show()
    }
  }


  /**
   *  Creates a picture marker symbol from the pin icon, and sets it to half of its original size
   */
  private fun createPinSymbol() {

    val pinDrawable = ContextCompat.getDrawable(this, R.drawable.pin) as BitmapDrawable?
    try {
      pinSourceSymbol = PictureMarkerSymbol.createAsync(pinDrawable).get()
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
    pinSourceSymbol?.width = 19f
    pinSourceSymbol?.height = 72f
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
