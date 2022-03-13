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

package com.esri.arcgisruntime.sample.applymosaicruletorasters

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.raster.ImageServiceRaster
import com.esri.arcgisruntime.raster.MosaicMethod
import com.esri.arcgisruntime.raster.MosaicOperation
import com.esri.arcgisruntime.raster.MosaicRule
import com.esri.arcgisruntime.sample.applymosaicruletorasters.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val spinner: Spinner by lazy {
        activityMainBinding.spinner
    }

    // create an image service raster from a url for an image service
    private val imageServiceRaster: ImageServiceRaster by lazy {
        ImageServiceRaster("https://sampleserver7.arcgisonline.com/server/rest/services/amberg_germany/ImageServer").apply {
            // set its mosaic rule
            if (mosaicRule == null) mosaicRule = MosaicRule()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a raster layer from the image service
        val rasterLayer = RasterLayer(imageServiceRaster)
        // add the raster layer to a new map on the map view
        mapView.map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC).apply {
            operationalLayers.add(rasterLayer)
        }

        // listen for the raster layer to finish loading
        rasterLayer.addDoneLoadingListener {
            // if the raster layer didn't load
            if (rasterLayer.loadStatus != LoadStatus.LOADED) {
                rasterLayer.loadError?.let { error ->
                    Log.e(TAG, "Raster layer failed to load: ${error.cause}")
                }
                return@addDoneLoadingListener
            }
            // set the map's viewpoint to the raster extent
            mapView.setViewpointAsync(Viewpoint(rasterLayer.fullExtent.center, 25000.0))
        }

        // set up the spinner with some predefined mosaic rules
        ArrayAdapter.createFromResource(
            this,
            R.array.mosaic_rules,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                setMosaicRule("Default")
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                setMosaicRule(parent.getItemAtPosition(position) as String)
            }
        }
    }

    /**
     * Applies one of the predefined mosaic rules to the image service raster.
     *
     * @param ruleName one of "Default", "Northwest", "Center", "By attribute", and "Lock raster"
     */
    private fun setMosaicRule(ruleName: String) {
        imageServiceRaster.mosaicRule = MosaicRule().apply {
            when (ruleName) {
                "Default" -> {
                    mosaicMethod = MosaicMethod.NONE
                }
                "Northwest" -> {
                    mosaicMethod = MosaicMethod.NORTHWEST
                    mosaicOperation = MosaicOperation.FIRST
                }
                "Center" -> {
                    mosaicMethod = MosaicMethod.CENTER
                    mosaicOperation = MosaicOperation.BLEND
                }
                "By attribute" -> {
                    mosaicMethod = MosaicMethod.ATTRIBUTE
                    sortField = "OBJECTID"
                }
                "Lock raster" -> {
                    mosaicMethod = MosaicMethod.LOCK_RASTER
                    lockRasterIds.clear()
                    lockRasterIds.addAll(listOf(1, 7, 12))
                }
                else -> {
                    mosaicMethod = MosaicMethod.NONE
                }
            }
        }
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
