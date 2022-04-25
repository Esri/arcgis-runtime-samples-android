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

package com.esri.arcgisruntime.sample.setmaxextent

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.EnvelopeBuilder
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.setmaxextent.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val increaseButton: Button by lazy {
        activityMainBinding.increaseButton
    }

    private val decreaseButton: Button by lazy {
        activityMainBinding.decreaseButton
    }

    private val updateMapButton: Button by lazy {
        activityMainBinding.updateMap
    }

    private val extentEnvelope =
        Envelope(Point(-12139393.2109, 5012444.0468), Point(-11359277.5124, 4438148.7816))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with the BasemapStyle streets focused on Colorado
        val coloradoMap = ArcGISMap(BasemapStyle.ARCGIS_STREETS).apply {
            // set the map's max extent to an envelope of Colorado's northwest and southeast corners
            maxExtent = extentEnvelope
        }

        // create a graphics overlay of the map's max extent
        val coloradoGraphicsOverlay = GraphicsOverlay().apply {
            // set the graphic's geometry to the max extent of the map
            graphics.add(Graphic(coloradoMap.maxExtent))
            // create a simple red dashed line renderer
            renderer = SimpleRenderer(SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.RED, 5f))
        }

        // envelop to update the map with a new maxExtent
        val newEnvelope = EnvelopeBuilder(extentEnvelope)

        // a factor of greater than 1.0 expands the envelope
        increaseButton.setOnClickListener {
            // update the envelop by a factor of 1.5
            newEnvelope.expand(1.5)
            // update the graphic overlay with the newEnvelope's geometry
            coloradoGraphicsOverlay.graphics[0].geometry = newEnvelope.toGeometry()
            // enable update mao button
            updateMapButton.isEnabled = true
        }

        // a factor of less than 1.0 shrinks the envelope
        decreaseButton.setOnClickListener {
            // update the envelop by a factor of 0.5
            newEnvelope.expand(0.5)
            // update the graphic overlay with the newEnvelope's geometry
            coloradoGraphicsOverlay.graphics[0].geometry = newEnvelope.toGeometry()
            // enable update mao button
            updateMapButton.isEnabled = true
        }

        updateMapButton.setOnClickListener {
            // calculate the total factor changed by new width / old width and round to two decimal places
            val factorChanged =
                (newEnvelope.toGeometry().width / mapView.map.maxExtent.width).round(2)
            // display the total envelop factor updated
            Toast.makeText(
                this,
                "MaxExtent updated by a factor of $factorChanged",
                Toast.LENGTH_SHORT
            ).show()
            // update the max extent of the mapview to the new extent
            mapView.map.maxExtent = newEnvelope.toGeometry()
            // disable the button since map has been updated
            updateMapButton.isEnabled = false
        }

        mapView.apply {
            // set the map to the map view
            map = coloradoMap
            // set the graphics overlay to the map view
            graphicsOverlays.add(coloradoGraphicsOverlay)
        }
    }

    /**
     * Kotlin extension function to round double to n [decimals] places
     */
    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
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
