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

package com.esri.arcgisruntime.showlabelsonlayer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.arcgisservices.LabelDefinition
import com.esri.arcgisruntime.data.ServiceFeatureTable
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

    // create a map view and set a map
    val map = ArcGISMap(Basemap.createLightGrayCanvas())
    mapView.map = map

    // create a feature layer from an online feature service of US Congressional Districts
    val serviceFeatureTable = ServiceFeatureTable(getString(R.string.congressional_districts_url))
    val featureLayer = FeatureLayer(serviceFeatureTable)
    map.operationalLayers.add(featureLayer)

    // zoom to the layer when it's done loading
    featureLayer.addDoneLoadingListener {
      if (featureLayer.loadStatus == LoadStatus.LOADED) {
        // set viewpoint to feature layer extent
        mapView.setViewpointAsync(Viewpoint(featureLayer.fullExtent))
      } else {
        val error = "Error loading feature layer :" + featureLayer.loadError.cause
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
      }
    }

    // use red text with white halo for republican district labels
    val republicanTextSymbol = TextSymbol().apply {
      size = 10f
      color = Color.RED
      haloColor = Color.WHITE
      haloWidth = 2f
    }

    // use blue text with white halo for democrat district labels
    val democratTextSymbol = TextSymbol().apply {
      size = 10f
      color = Color.BLUE
      haloColor = Color.WHITE
      haloWidth = 2f
    }

    // use a custom label expression combining some of the feature's fields
    val expressionInfo = JsonObject().apply {
      add(
        "expression",
        JsonPrimitive("\$feature.NAME + \" (\" + left(\$feature.PARTY,1) + \")\\nDistrict \" + \$feature.CDFIPS")
      )
    }

    // construct the label definition json
    val json = JsonObject().apply {
      add("labelExpressionInfo", expressionInfo)
      // position the label in the center of the feature
      add("labelPlacement", JsonPrimitive("esriServerPolygonPlacementAlwaysHorizontal"))
    }

    // create a copy of the json with a custom where clause and symbol only for republican districts
    val republicanJson = json.deepCopy().apply {
      add("where", JsonPrimitive("PARTY = 'Republican'"))
      add("symbol", JsonParser.parseString(republicanTextSymbol.toJson()))
    }

    // create a copy of the json with a custom where clause and symbol only for democrat districts
    val democratJson = json.deepCopy().apply {
      add("where", JsonPrimitive("PARTY = 'Democrat'"))
      add("symbol", JsonParser.parseString(democratTextSymbol.toJson()))
    }

    // create label definitions from the JSON strings
    val republicanLabelDefinition = LabelDefinition.fromJson(republicanJson.toString())
    val democratLabelDefinition = LabelDefinition.fromJson(democratJson.toString())
    featureLayer.apply {
      // add the definitions to the feature layer
      labelDefinitions.addAll(listOf(republicanLabelDefinition, democratLabelDefinition))
      // enable labels
      isLabelsEnabled = true
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
