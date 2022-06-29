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
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.applyuniquevalueswithalternatesymbols.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.Symbol
import com.esri.arcgisruntime.symbology.SymbolReferenceProperties
import com.esri.arcgisruntime.symbology.UniqueValueRenderer


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a feature layer using the feature table
        val featureTable =
            ServiceFeatureTable("https://sampleserver6.arcgisonline.com/arcgis/rest/services/SF311/FeatureServer/0")
        val featureLayer = FeatureLayer(featureTable)

        // create a symbol for a specific scale range
        val triangleMultilayerSymbol = SimpleMarkerSymbol(
            SimpleMarkerSymbol.Style.TRIANGLE,
            Color.RED,
            30F
        ).toMultilayerSymbol().apply {
            referenceProperties = SymbolReferenceProperties(5000.0, 0.0)
        }

        // create alternate symbols for use at different scale ranges
        val blueAlternateSymbol = SimpleMarkerSymbol(
            SimpleMarkerSymbol.Style.SQUARE,
            Color.BLUE,
            30F
        ).toMultilayerSymbol().apply {
            referenceProperties = SymbolReferenceProperties(10000.0, 5000.0)
        }

        val yellowAlternateSymbol = SimpleMarkerSymbol(
            SimpleMarkerSymbol.Style.DIAMOND,
            Color.YELLOW,
            30F
        ).toMultilayerSymbol().apply {
            referenceProperties = SymbolReferenceProperties(20000.0, 10000.0)
        }

        val alternateSymbols = listOf<Symbol>(blueAlternateSymbol, yellowAlternateSymbol)

        // create a unique value with the triangle symbol and the alternate symbols
        val uniqueValue = UniqueValueRenderer.UniqueValue(
            "unique value",
            "unique values based on request type",
            triangleMultilayerSymbol,
            listOf("Damaged Property"),
            alternateSymbols
        )

        // create a unique value renderer
        val uniqueValueRenderer = UniqueValueRenderer().apply {
            uniqueValues.add(uniqueValue)
            fieldNames.add("req_type")
        }

        // set a default symbol for the unique value renderer.
        // This will be use for features that aren't "Damaged Property"
        // or when out of range of the UniqueValue symbols.
        uniqueValueRenderer.defaultSymbol = SimpleMarkerSymbol(
            SimpleMarkerSymbol.Style.DIAMOND,
            Color.MAGENTA,
            15F
        ).toMultilayerSymbol()

        // set the unique value renderer on the feature layer
        featureLayer.renderer = uniqueValueRenderer

        // create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        val centerPoint = Point(
            -13631205.660131,
            4546829.846004,
            SpatialReferences.getWebMercator()
        )
        mapView.setViewpoint(
            Viewpoint(centerPoint, 25000.0)
        )
        mapView.map.operationalLayers.add(featureLayer)
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
