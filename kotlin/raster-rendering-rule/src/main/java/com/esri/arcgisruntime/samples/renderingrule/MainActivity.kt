/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.samples.renderingrule

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.raster.ImageServiceRaster
import com.esri.arcgisruntime.raster.RenderingRule
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private lateinit var map: ArcGISMap

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a Streets BaseMap
    map = ArcGISMap(Basemap.createStreets())
    // set the map to be displayed in this view
    mapView.map = map

    // create image service raster as raster layer and add to map
    val imageServiceRaster = ImageServiceRaster(resources.getString(R.string.image_service_url))
    val imageRasterLayer = RasterLayer(imageServiceRaster)
    map.operationalLayers.add(imageRasterLayer)

    val renderRulesList = mutableListOf<String>()
    val spinnerAdapter =
      ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, renderRulesList)
    spinner.adapter = spinnerAdapter
    // zoom to the extent of the raster service
    imageRasterLayer.addDoneLoadingListener {
      if (imageRasterLayer.loadStatus == LoadStatus.LOADED) {
        // zoom to extent of raster
        mapView.setViewpointGeometryAsync(imageServiceRaster.serviceInfo.fullExtent)
        // get the predefined rendering rules and add to spinner
        val renderingRuleInfos = imageServiceRaster.serviceInfo.renderingRuleInfos
        for (renderRuleInfo in renderingRuleInfos) {
          val renderingRuleName = renderRuleInfo.name
          renderRulesList.add(renderingRuleName)
          // update array adapter with list update
          spinnerAdapter.notifyDataSetChanged()
        }
      }
    }

    // listen to the spinner
    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d("MainActivity", "Spinner nothing selected")
      }

      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
        applyRenderingRule(imageServiceRaster, position)
    }

  }

  /**
   * Apply a rendering rule on a Raster and add it to the map
   *
   * @param imageServiceRaster image service raster to apply rendering on
   * @param index spinner selected position representing the rule to apply
   */
  fun applyRenderingRule(imageServiceRaster: ImageServiceRaster, index: Int) {
    // clear all rasters
    map.operationalLayers.clear()
    // get the rendering rule info at the selected index
    val renderRuleInfo = imageServiceRaster.serviceInfo.renderingRuleInfos[index]
    // create a rendering rule object using the rendering rule info
    val renderingRule = RenderingRule(renderRuleInfo)
    // create a new image service raster
    val appliedImageServiceRaster =
      ImageServiceRaster(resources.getString(R.string.image_service_url))
    // apply the rendering rule
    appliedImageServiceRaster.renderingRule = renderingRule
    // create a raster layer using the image service raster
    val rasterLayer = RasterLayer(appliedImageServiceRaster)
    // add the raster layer to the map
    map.operationalLayers.add(rasterLayer)
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
