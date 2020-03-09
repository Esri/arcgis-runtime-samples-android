/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.showlabelsonlayer

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.arcgisservices.LabelDefinition
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.symbology.TextSymbol
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val map = ArcGISMap(Basemap.createLightGrayCanvas())
    mapView.map = map

    // create a feature layer from an online feature service of US Highways and add it to the map
    val serviceFeatureTable = ServiceFeatureTable(getString(R.string.us_highways_1))
    val featureLayer = FeatureLayer(serviceFeatureTable)
    map.operationalLayers.add(featureLayer)

    // zoom to the layer when it's done loading
    featureLayer.addDoneLoadingListener {
      if (featureLayer.loadStatus == LoadStatus.LOADED) {
        // set viewpoint to the center of the US
        mapView.setViewpointAsync(
          Viewpoint(
            Point(-10974490.0, 4814376.0, 0.0, SpatialReferences.getWebMercator()),
            20000000.0
          )
        )
      } else {
        Toast.makeText(
            this,
            getString(R.string.error_message) + featureLayer.loadError.message,
            Toast.LENGTH_LONG
          )
          .show()
        Log.e(TAG, getString(R.string.error_message) + featureLayer.loadError.message)
      }
    }

    // use large blue text with a yellow halo for the labels
    val textSymbol = TextSymbol().apply {
      size = 20f
      color = -0xffff01
      haloColor = -0x100
      haloWidth = 2f
    }

    // construct the label definition json
    val json = JsonObject().apply {
      // prepend 'I - ' (for Interstate) to the route number for the label
      val expressionInfo = JsonObject()
      expressionInfo.add("expression", JsonPrimitive("'I -' + \$feature.rte_num1"))
      add("labelExpressionInfo", expressionInfo)
      // position the label above and along the direction of the road
      add("labelPlacement", JsonPrimitive("esriServerLinePlacementAboveAlong"))
      // only show labels on the interstate highways (others have an empty rte_num1 attribute)
      add("where", JsonPrimitive("\$feature.rte_num1 <> ' '"))
      // set the text symbol as the label symbol
      add("symbol", JsonParser().parse(textSymbol.toJson()))
    }

    // create a label definition from the JSON string
    val labelDefinition = LabelDefinition.fromJson(json.toString())
    // add the definition to the feature layer and enable labels on it
    featureLayer.labelDefinitions.add(labelDefinition)
    featureLayer.isLabelsEnabled = true
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
