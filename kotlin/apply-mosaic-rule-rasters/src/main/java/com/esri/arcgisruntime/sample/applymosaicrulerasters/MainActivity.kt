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

package com.esri.arcgisruntime.sample.applymosaicrulerasters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.LayerViewStatus
import com.esri.arcgisruntime.raster.ImageServiceRaster
import com.esri.arcgisruntime.raster.MosaicMethod
import com.esri.arcgisruntime.raster.MosaicOperation
import com.esri.arcgisruntime.raster.MosaicRule
import com.esri.arcgisruntime.security.AuthenticationManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val imageServiceRaster: ImageServiceRaster by lazy {
    ImageServiceRaster("http://rtc-100-8.esri.com/arcgis/rest/services/imageServices/amberg_germany/ImageServer").apply {
      if (mosaicRule == null) mosaicRule = MosaicRule()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    AuthenticationManager.setTrustAllSigners(true)

    val rasterLayer = RasterLayer(imageServiceRaster)
    mapView.map = ArcGISMap(Basemap.createTopographicVector()).apply {
      operationalLayers.add(rasterLayer)
    }

    rasterLayer.addDoneLoadingListener {
      if (rasterLayer.loadStatus != LoadStatus.LOADED) {
        rasterLayer.loadError?.let { error ->
          Log.e("MainActivity", "Raster layer failed to load: ${error.cause}")
        }
        return@addDoneLoadingListener
      }

      mapView.setViewpointAsync(Viewpoint(rasterLayer.fullExtent.center, 25000.0))
    }

    ArrayAdapter.createFromResource(
      this,
      R.array.mosaic_rules,
      android.R.layout.simple_spinner_item
    ).also { adapter ->
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
      spinner.adapter = adapter
    }

    spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
      override fun onNothingSelected(parent: AdapterView<*>?) {
        setMosaicRule("Default")
      }

      override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        setMosaicRule(parent.getItemAtPosition(position) as String)
      }
    }
  }

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
