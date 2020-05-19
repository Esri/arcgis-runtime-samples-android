package com.esri.arcgisruntime.sample.offlinegeocode

import android.database.MatrixCursor
import android.graphics.Color
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
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
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.esri.arcgisruntime.tasks.geocode.ReverseGeocodeParameters
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName
  private val geocodeParameters = GeocodeParameters().apply {
    // get place name and street address attributes
    resultAttributeNames.addAll(listOf("PlaceName", "StAddr"))
    // return only the closest result
    maxResults = 1
  }

  val locatorTask: LocatorTask by lazy {
    LocatorTask(
      getExternalFilesDir(null)?.path + resources.getString(R.string.san_diego_loc)
    )
  }
  // create a point symbol for showing the address location
  val pointSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 20.0f)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // set the map to the map view
    mapView.map = ArcGISMap()
    // add a graphics overlay to the map view
    val graphicsOverlay = GraphicsOverlay()
    mapView.graphicsOverlays.add(graphicsOverlay)
    // add a touch listener to the map view
    //TODO

    // load the tile cache from local storage
    val tileCache =
      TileCache(getExternalFilesDir(null)?.path + getString(R.string.san_diego_tpk))
    // use the tile cache extent to set the view point
    tileCache.addDoneLoadingListener { mapView.setViewpoint(Viewpoint(tileCache.fullExtent)) }
    // create a tiled layer and add it to as the base map
    val tiledLayer = ArcGISTiledLayer(tileCache)
    mapView.map.basemap = Basemap(tiledLayer)
    // create geocode parameters
    val geocodeParameters = GeocodeParameters().apply {
      resultAttributeNames.add("*")
      maxResults = 1
    }
    // create reverse geocode parameters
    val reverseGeocodeParameters = ReverseGeocodeParameters().apply {
      resultAttributeNames.add("*")
      outputSpatialReference = mapView.map.spatialReference
      maxResults = 1
    }
    // load the locator task from external storage
    locatorTask.loadAsync()
    locatorTask.addDoneLoadingListener { setupAddressSearchView() }
  }

  /**
   * Sets up the address SearchView and uses MatrixCursor to show suggestions to the user as text is entered.
   */
  private fun setupAddressSearchView() {
    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(address: String): Boolean {
        // geocode typed address
        geoCodeTypedAddress(address)
        // clear focus from search views
        searchView.clearFocus()
        return true
      }

      override fun onQueryTextChange(newText: String?): Boolean {
        return true
      }
    })


    val suggestions = resources.getStringArray(R.array.suggestion_items).toList()

    // set up parameters for searching with MatrixCursor
    val columnNames = arrayOf(BaseColumns._ID, "address")
    val suggestionsCursor = MatrixCursor(columnNames)

    // add each address suggestion to a new row
    for ((key, value) in suggestions.withIndex()) {
      suggestionsCursor.addRow(arrayOf<Any>(key, value))
    }
    // column names for the adapter to look at when mapping data
    val cols = arrayOf("address")
    // ids that show where data should be assigned in the layout
    val to = intArrayOf(R.id.suggestion_address)
    // define SimpleCursorAdapter
    val suggestionAdapter = SimpleCursorAdapter(
      this@MainActivity,
      R.layout.suggestion, suggestionsCursor, cols, to, 0
    )

    searchView.suggestionsAdapter = suggestionAdapter

    searchView.setOnSuggestionListener(object: SearchView.OnSuggestionListener {
      override fun onSuggestionSelect(position: Int): Boolean {
        TODO("Not yet implemented")
        return true
      }

      override fun onSuggestionClick(position: Int): Boolean {
        geoCodeTypedAddress(suggestions[position])
        return true
      }
    })
  }

  /**
   * Use the locator task to geocode the the given address.
   *
   * @param address as a string
   */
  private fun geoCodeTypedAddress(address: String) {
    // Execute async task to find the address
    locatorTask.addDoneLoadingListener {
      if (locatorTask.getLoadStatus() === LoadStatus.LOADED) {
        // get a list of geocode results for the given address
        val geocodeFuture: ListenableFuture<List<GeocodeResult>> =
          locatorTask.geocodeAsync(address, geocodeParameters)
        geocodeFuture.addDoneListener {
          try {
            // get the geocode results
            val geocodeResults = geocodeFuture.get()
            if (!geocodeResults.isEmpty()) {
              // get the first result
              val geocodeResult = geocodeResults[0]
              displayGeocodeResult(geocodeResult.displayLocation, geocodeResult.label)
            } else {
              Toast.makeText(this, "No location found for: $address", Toast.LENGTH_LONG).show()
            }
          } catch (e: InterruptedException) {
            val error = "Error getting geocode result: " + e.message
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            Log.e(TAG, error)
          } catch (e: Exception) {
            val error = "Error getting geocode result: " + e.message
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            Log.e(TAG, error)
          }
        }
      } else {
        val error =
          "Error loading locator task: " + locatorTask.getLoadError().message
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
      }
    }
  }

  /**
   * Draw a point and open a callout showing geocode results on map.
   *
   * @param resultPoint geometry to show where the geocode result is
   * @param address     to display in the associated callout
   */
  private fun displayGeocodeResult(
    resultPoint: Point,
    address: CharSequence
  ) {
    // dismiss the callout if showing
    if (mapView.getCallout().isShowing()) {
      mapView.getCallout().dismiss()
    }

    val graphicsOverlay = mapView.graphicsOverlays[0]
    // remove any previous graphics/search results
    graphicsOverlay.getGraphics().clear()
    // create graphic object for resulting location
    val pointGraphic = Graphic(resultPoint, pointSymbol)
    // add graphic to location layer
    graphicsOverlay.getGraphics().add(pointGraphic)
    // Zoom map to geocode result location
    mapView.setViewpointAsync(Viewpoint(resultPoint, 8000.0), 3f)
    showCallout(resultPoint, address)
  }

  /**
   * Show a callout at the given point with the given text.
   *
   * @param point to define callout location
   * @param text to define callout content
   */
  private fun showCallout(point: Point, text: CharSequence) {
    val callout: Callout = mapView.getCallout()
    val calloutTextView = TextView(this)
    calloutTextView.setText(text)
    callout.setLocation(point)
    callout.setContent(calloutTextView)
    callout.show()
  }
}
