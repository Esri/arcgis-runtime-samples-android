/*
 * Copyright 2021 Esri
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
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.arcgisservices.LabelDefinition
import com.esri.arcgisruntime.arcgisservices.LabelingPlacement
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.labeling.ArcadeLabelExpression
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.symbology.TextSymbol
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        val portal = Portal("https://www.arcgis.com")
        val portalItem = PortalItem(portal, "850dfee7d30f4d9da0ebca34a533c169")

        val scene = ArcGISScene(portalItem)
        scene.addDoneLoadingListener {
            // get "Gas Main" feature layer from the "Gas" layer
            (scene.operationalLayers.first { it.name == "Gas" }
                .subLayerContents.first { it.name == "Gas Main" } as? FeatureLayer)?.let { gasMainFeatureLayer ->
                gasMainFeatureLayer.apply {
                    // clear the existing label definition
                    labelDefinitions.clear()
                    // add the label definition defined in the makeLabelDefinition function
                    labelDefinitions.add(makeLabelDefinition())
                    // enable labels
                    isLabelsEnabled = true
                }
            }
        }

        sceneView.scene = scene
    }

    private fun makeLabelDefinition(): LabelDefinition {
        // make and stylize the text symbol
        val textSymbol = TextSymbol().apply {
            color = getColor(R.color.colorLabels)
            haloColor = Color.WHITE
            haloWidth = 2f
            size = 16f
        }

        // create and return a label definition
        return LabelDefinition().apply {
            expression = ArcadeLabelExpression("Text(\$feature.INSTALLATIONDATE, `DD MMM YY`)")
            placement = LabelingPlacement.LINE_ABOVE_ALONG
            isUseCodedValues = true
            setTextSymbol(textSymbol)
        }
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
