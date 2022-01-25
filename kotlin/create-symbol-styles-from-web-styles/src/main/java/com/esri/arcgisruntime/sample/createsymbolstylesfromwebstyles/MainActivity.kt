package com.esri.arcgisruntime.sample.createsymbolstylesfromwebstyles

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.createsymbolstylesfromwebstyles.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.createsymbolstylesfromwebstyles.databinding.LegendRowBinding
import com.esri.arcgisruntime.symbology.SymbolStyle
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val legendFAB: FloatingActionButton by lazy {
        activityMainBinding.legendFAB
    }

    private val scrollViewLayout: LinearLayout by lazy {
        activityMainBinding.scrollViewLayout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a unique value renderer
        val uniqueValueRenderer = UniqueValueRenderer().apply {
            // add the name of a field from the feature layer data that symbols will be mapped to
            fieldNames.add("cat2")
        }

        // create a feature layer from a service
        val featureLayer =
            FeatureLayer(ServiceFeatureTable("http://services.arcgis.com/V6ZHFr6zdgNZuVG0/arcgis/rest/services/LA_County_Points_of_Interest/FeatureServer/0")).apply {
                // set the unique value renderer on the feature layer
                renderer = uniqueValueRenderer
            }

        // create a map with the light gray basemap style
        val map = ArcGISMap(BasemapStyle.ARCGIS_LIGHT_GRAY).apply {
            // set an initial reference scale on the map for controlling symbol size
            referenceScale = 100000.0
            // add the feature layer to the map's operational layers
            operationalLayers.add(featureLayer)
        }

        // set up map view properties
        mapView.apply {
            // set the map
            this.map = map
            // set a viewpoint
            mapView.setViewpoint(Viewpoint(34.28301, -118.44186, 7000.0))

        }

        // create a symbol style from a web style
        // note: ArcGIS Online is used as the default portal when null is passed as the portal parameter
        val symbolStyle = SymbolStyle("Esri2DPointSymbolsStyle", null)

        // setup the UI for the legend
        setupUI()

        // create a list of the required symbol names from the web style
        val symbolNames = listOf(
            "atm",
            "beach",
            "campground",
            "city-hall",
            "hospital",
            "library",
            "park",
            "place-of-worship",
            "police-station",
            "post-office",
            "school",
            "trail"
        )

        // create unique values for the renderer and construct a symbol for each feature
        symbolNames.forEach { symbolName ->

            // create a placeholder legend row
            val loadingLegendRowBinding = LegendRowBinding.inflate(layoutInflater).apply {
                symbolTextView.text = getString(R.string.loading)
            }
            // add the placeholder row to the scroll view
            scrollViewLayout.addView(loadingLegendRowBinding.root)

            // search for each symbol in the symbol style
            val searchResult = symbolStyle.getSymbolAsync(listOf(symbolName))
            searchResult.addDoneListener {
                try {
                    // get the symbol from the search result
                    val symbol = searchResult.get()
                    // get a list of all categories to be mapped to the symbol
                    val categories = mapSymbolNameToField(symbolName)
                    categories.forEach { category ->
                        // create a unique value for each category
                        val uniqueValue = UniqueValueRenderer.UniqueValue(
                            "",
                            symbolName,
                            symbol,
                            listOf(category)
                        )
                        // add each unique value to the unique value renderer
                        uniqueValueRenderer.uniqueValues.add(uniqueValue)
                    }
                    // create a swatch from symbol
                    val symbolBitmapFuture = symbol.createSwatchAsync(this, Color.WHITE)
                    symbolBitmapFuture.addDoneListener {
                        // create a legend row
                        val legendRowBinding = LegendRowBinding.inflate(layoutInflater).apply {
                            // set the symbol to the row's image view
                            symbolImageView.setImageBitmap(symbolBitmapFuture.get())
                            // set the symbol name to the row's text view
                            symbolTextView.text = symbolName
                        }
                        // remove the loading row already at this index
                        scrollViewLayout.removeViewAt(symbolNames.indexOf(symbolName))
                        // add the legend row at the correct index
                        scrollViewLayout.addView(legendRowBinding.root, symbolNames.indexOf(symbolName))
                    }
                } catch (e: Exception) {
                    val error = "Error getting symbol: " + e.message
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    Log.e(TAG, error)
                }
            }
        }

        // add a map scale changed listener on the map view to control the symbol sizes at different scales
        mapView.addMapScaleChangedListener {
            featureLayer.isScaleSymbols = mapView.mapScale >= 80000
        }
    }

    /**
     * Returns a list of categories to be matched to a symbol name.
     *
     * @param symbolName the name of a symbol from a symbol style
     * @return categories a list of categories matched to the provided symbol name
     */
    private fun mapSymbolNameToField(symbolName: String): List<String> {
        return mutableListOf<String>().apply {
            when (symbolName) {
                "atm" -> add("Banking and Finance")
                "beach" -> add("Beaches and Marinas")
                "campground" -> add("Campgrounds")
                "city-hall" -> addAll(
                    listOf(
                        "City Halls",
                        "Government Offices"
                    )
                )
                "hospital" -> addAll(
                    listOf(
                        "Hospitals and Medical Centers",
                        "Health Screening and Testing",
                        "Health Centers",
                        "Mental Health Centers"
                    )
                )
                "library" -> add("Libraries")
                "park" -> add("Parks and Gardens")
                "place-of-worship" -> add("Churches")
                "police-station" -> add("Sheriff and Police Stations")
                "post-office" -> addAll(
                    listOf(
                        "DHL Locations",
                        "Federal Express Locations"
                    )
                )
                "school" -> addAll(
                    listOf(
                        "Public High Schools",
                        "Public Elementary Schools",
                        "Private and Charter Schools"
                    )
                )
                "trail" -> add("Trails")
            }
        }
    }

    /**
     * Sets up UI behaviour. Closes expandable floating action button on touching the scene view.
     * Moves floating action button on attribution view expanded. Expands floating action button on
     * tap.
     */
    private fun setupUI() {
        mapView.apply {
            // create a touch listener
            onTouchListener = object : DefaultMapViewOnTouchListener(applicationContext, mapView) {
                // close the options sheet when the map is tapped
                override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                    if (legendFAB.isExpanded) {
                        legendFAB.isExpanded = false
                    }
                    return super.onTouch(view, motionEvent)
                }
            }
            // ensure the floating action button moves to be above the attribution view
            addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                val heightDelta = bottom - oldBottom
                (legendFAB.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin += heightDelta
            }
        }

        // show the options sheet when the floating action button is clicked
        legendFAB.setOnClickListener {
            legendFAB.isExpanded = !legendFAB.isExpanded
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
