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

package com.esri.arcgisruntime.showlabelsonlayerin3d

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.arcgisservices.LabelDefinition
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.GroupLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.SceneView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.showlabelsonlayerin3d.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.TextSymbol
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

  /*  private val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val sceneView: SceneView by lazy {
        activityMainBinding.sceneView
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        val portal = Portal("https://www.arcgis.com")
        val portalItem = PortalItem(portal, "850dfee7d30f4d9da0ebca34a533c169")

        val scene = ArcGISScene(portalItem)
        scene.addDoneLoadingListener {
            (scene.operationalLayers.first { it.name == "Gas" } as? GroupLayer)?.let { gasGroupLayer ->
                (gasGroupLayer.layers[0] as FeatureLayer as? FeatureLayer)?.let { gasMainFeatureLayer ->
                    gasMainFeatureLayer.apply {
                        // add the label definition to the feature layer
                        labelDefinitions.add(makeLabelDefinition())
                        // enable layers on the feature layer
                        isLabelsEnabled = true
                    }
                }
            }
        }

        sceneView.scene = scene
    }

    private fun makeLabelDefinition(): LabelDefinition {
        // make and stylize the text symbol
        val textSymbol = TextSymbol().apply {
            angle = 0f
            backgroundColor = Color.TRANSPARENT
            outlineColor = Color.WHITE
            color = Color.RED
            haloColor = Color.WHITE
            haloWidth = 2f
            horizontalAlignment = TextSymbol.HorizontalAlignment.CENTER
            verticalAlignment = TextSymbol.VerticalAlignment.MIDDLE
            offsetX = 0f
            offsetY = 0f
            fontDecoration = TextSymbol.FontDecoration.NONE
            size = 14f
            fontStyle = TextSymbol.FontStyle.NORMAL
            fontWeight = TextSymbol.FontWeight.NORMAL
        }

        // make a JSON object
        val labelJSONObject = JsonObject().apply {
            add("expression", JsonPrimitive("\$feature.INSTALLATIONDATE"))
            add("labelPlacement", JsonPrimitive("esriServerLinePlacementAboveAlong"))
            add("useCodedValues", JsonPrimitive(true))
            add("symbol", JsonParser.parseString(textSymbol.toJson()))
        }

        // create and return a label definition from the JSON object
        return LabelDefinition.fromJson(labelJSONObject.toString())
    }

    override fun onPause() {
        sceneView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        sceneView.resume()
    }

    override fun onDestroy() {
        sceneView.dispose()
        super.onDestroy()
    }
}
