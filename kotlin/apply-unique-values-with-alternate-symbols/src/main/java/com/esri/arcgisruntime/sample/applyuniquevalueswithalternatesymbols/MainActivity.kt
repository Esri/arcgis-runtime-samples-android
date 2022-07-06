/* Copyright 2022 Esri
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

package com.esri.arcgisruntime.sample.applyuniquevalueswithalternatesymbols

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.AnimationCurve
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.applyuniquevalueswithalternatesymbols.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.Symbol
import com.esri.arcgisruntime.symbology.SymbolReferenceProperties
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    // create feature table using the feature service URL
    private val featureTable = ServiceFeatureTable("https://sampleserver6.arcgisonline.com/arcgis/rest/services/SF311/FeatureServer/0")

    // create a feature layer using the feature table
    private val featureLayer = FeatureLayer(featureTable)

    // create a center map point in San Francisco, CA
    private val centerPoint = Point(-13631205.660131, 4546829.846004, SpatialReferences.getWebMercator())

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val scaleText: TextView by lazy {
        activityMainBinding.scaleText
    }

    private val resetViewpointButton: Button by lazy {
        activityMainBinding.resetViewpointButton
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is
        // required to access basemaps and other location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // set the unique value renderer on the feature layer
        featureLayer.renderer = makeUniqueValueRenderer()

        mapView.apply {
            // create a map with the BasemapType topographic to be displayed in the layout's MapView
            map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)
            // add the feature layer to the map view's map
            map.operationalLayers.add(featureLayer)
            // set the viewpoint to be centered at San Francisco, CA
            setViewpoint(Viewpoint(centerPoint, 25000.0))
            // update the scale text view as the view point changes
            addViewpointChangedListener { updateScaleLabel() }
        }

        // resets the view point using a ease in out animation
        resetViewpointButton.setOnClickListener {
            mapView.setViewpointAsync(
                Viewpoint(centerPoint, 25000.0),
                5F,
                AnimationCurve.EASE_IN_OUT_SINE
            )
        }
    }

    /**
     * Create the unique values renderer for the feature layer
     */
    private fun makeUniqueValueRenderer(): UniqueValueRenderer {
        // create the default symbol
        val symbol = SimpleMarkerSymbol(
            SimpleMarkerSymbol.Style.TRIANGLE,
            Color.RED,
            30F
        )
        // convert the symbol to a multi layer symbol
        val multilayerSymbol = symbol.toMultilayerSymbol().apply {
            referenceProperties = SymbolReferenceProperties(5000.0, 0.0)
        }
        // create alternate symbols for the unique value
        val alternateSymbols = createAlternateSymbols()
        // create a unique value with alternate symbols
        val uniqueValue = UniqueValueRenderer.UniqueValue(
            "unique values based on request type",
            "unique value",
            multilayerSymbol,
            listOf("Damaged Property"),
            alternateSymbols
        )
        // create a unique value renderer
        val uniqueValueRenderer = UniqueValueRenderer().apply {
            // add the unique value
            uniqueValues.add(uniqueValue)
            // set the field name
            fieldNames.add("req_type")
        }
        // create and set the default symbol
        val defaultSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.MAGENTA, 15F)
        // set a default symbol for the unique value renderer.
        // This will be use for features that aren't "Damaged Property"
        // or when out of range of the UniqueValue symbols.
        uniqueValueRenderer.defaultSymbol = defaultSymbol.toMultilayerSymbol()

        // set the unique value renderer on the feature layer
        return uniqueValueRenderer
    }

    /**
     * Create alternate symbols for the unique value renderer
     */
    private fun createAlternateSymbols(): List<Symbol> {
        // create the alternate symbol for the mid range scale
        val alternateSymbolBlue = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, Color.BLUE, 30F)
        // convert the symbol to a multilayer symbol
        val alternateSymbolMultilayerBlue = alternateSymbolBlue.toMultilayerSymbol().apply {
            // set the reference properties
            referenceProperties = SymbolReferenceProperties(10000.0, 5000.0)
        }
        // create the alternate symbol for the high range scale
        val alternateSymbolYellow = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.YELLOW, 30F)
        // convert the symbol to a multilayer symbol
        val alternateSymbolMultilayerYellow = alternateSymbolYellow.toMultilayerSymbol().apply {
            // set the reference properties
            referenceProperties = SymbolReferenceProperties(20000.0, 10000.0)
        }
        // return both alternate symbols
        return listOf(alternateSymbolMultilayerBlue, alternateSymbolMultilayerYellow)
    }

    /**
     * Update the label to display the current scale
     */
    private fun updateScaleLabel() {
        // formats numbers using comma
        val formattedScale = "%,d".format(mapView.mapScale.roundToInt())
        // updates the text view with the current scale
        scaleText.text = "Current scale: 1:$formattedScale"
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
