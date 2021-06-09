/*
 *  Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.displaysubtypefeaturelayer

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.arcgisservices.LabelDefinition
import com.esri.arcgisruntime.arcgisservices.LabelingPlacement
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.SubtypeFeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.labeling.SimpleLabelExpression
import com.esri.arcgisruntime.security.UserCredential
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.symbology.TextSymbol
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.sublayer_control_layout.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // setup map with basemap and an initial viewpoint
        mapView.apply {
            map = ArcGISMap(BasemapStyle.ARCGIS_STREETS_NIGHT)
            setViewpoint(
                Viewpoint(
                    Envelope(
                        -9812691.11079696,
                        5128687.20710657,
                        -9812377.9447607,
                        5128865.36767282,
                        SpatialReferences.getWebMercator()
                    )
                )
            )

            // on any navigation on the map view
            addMapScaleChangedListener {
                currentMapScaleTextView.text =
                    getString(R.string.current_map_scale_text, mapView.mapScale.roundToInt())
            }
        }

        // create a service feature table
        val serviceFeatureTable =
            ServiceFeatureTable("https://sampleserver7.arcgisonline.com/server/rest/services/UtilityNetwork/NapervilleElectric/FeatureServer/0").apply {
                // set user credentials to authenticate with the service
                credential = UserCredential("viewer01", "I68VGU^nMurF")
            }

        // create a subtype feature layer from the service feature table
        val subtypeFeatureLayer = SubtypeFeatureLayer(serviceFeatureTable)
        // add it to the map
        mapView.map.operationalLayers.add(subtypeFeatureLayer)

        // create a text symbol for styling the sublayer label definition
        val textSymbol = TextSymbol().apply {
            size = 12f
            color = Color.BLUE
            outlineColor = Color.WHITE
            haloColor = Color.WHITE
            haloWidth = 3f
        }

        // create a label definition with a simple label expression
        val simpleLabelExpression = SimpleLabelExpression("[nominalvoltage]")
        val labelDefinition = LabelDefinition(simpleLabelExpression, textSymbol).apply {
            placement = LabelingPlacement.POINT_ABOVE_RIGHT
            isUseCodedValues = true
        }

        // once the subtype feature layer is loaded
        subtypeFeatureLayer.addDoneLoadingListener {
            // create a subtype sublayer
            val subtypeSublayer =
                subtypeFeatureLayer.getSublayerWithSubtypeName("Street Light").apply {
                    isLabelsEnabled = true
                    labelDefinitions.add(labelDefinition)
                }

            // show subtype sublayer when checked, hide when unchecked
            showSubtypeSublayerCheckBox.setOnClickListener {
                subtypeSublayer.isVisible = showSubtypeSublayerCheckBox.isChecked
            }

            // get the original renderer of the subtype sublayer
            val originalRenderer = subtypeSublayer.renderer

            // when the selected radio button changes
            rendererRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                // set the sublayer renderer to
                subtypeSublayer.renderer = when (checkedId) {
                    alternativeRendererButton.id -> {
                        // use an alternative renderer
                        SimpleRenderer(
                            SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.MAGENTA, 20f)
                        )
                    }
                    originalRendererButton.id -> {
                        // use the original renderer
                        originalRenderer
                    }
                    else -> {
                        error("Invalid radio button.")
                    }
                }
            }

            // set the minimum scale of the labels for the sub layer
            setMinScaleButton.setOnClickListener {
                // set the subtype sublayer's min scale to be the current scale of the map view
                subtypeSublayer.minScale = mapView.mapScale
                // update the UI to show
                labelingScaleTextView.text =
                    getString(
                        R.string.subtype_sublayer_scale_text,
                        subtypeSublayer.minScale.roundToInt()
                    )
            }
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
