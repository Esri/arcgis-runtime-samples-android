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
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.arcgisservices.LabelDefinition
import com.esri.arcgisruntime.arcgisservices.LabelingPlacement
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.showlabelsonlayer.databinding.ActivityMainBinding
import com.esri.arcgisruntime.mapping.labeling.ArcadeLabelExpression
import com.esri.arcgisruntime.symbology.TextSymbol

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

        // create a map view and set a map
        val map = ArcGISMap(BasemapStyle.ARCGIS_LIGHT_GRAY)
        mapView.map = map

        // create a feature layer from an online feature service of US Congressional Districts
        val serviceFeatureTable =
            ServiceFeatureTable(getString(R.string.congressional_districts_url))
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

        val republicanLabelDefinition = makeLabelDefinition("Republican", Color.RED)
        val democratLabelDefinition = makeLabelDefinition("Democrat", Color.BLUE)

        featureLayer.apply {
            // add the definitions to the feature layer
            labelDefinitions.addAll(listOf(republicanLabelDefinition, democratLabelDefinition))
            // enable labels
            isLabelsEnabled = true
        }
    }

    /**
     * Creates a label definition for a given party and color to populate a text symbol.
     *
     * @param party name to be passed into the label definition's WHERE clause
     * @param textColor to be passed into the text symbol
     *
     * @return label definition created from the given arcade expression
     */
    private fun makeLabelDefinition(party: String, textColor: Int): LabelDefinition {

        // create text symbol for styling the label
        val textSymbol = TextSymbol().apply {
            size = 12f
            color = textColor
            haloColor = Color.WHITE
            haloWidth = 2f
        }

        // create a label definition with an Arcade expression
        val arcadeLabelExpression =
            ArcadeLabelExpression("\$feature.NAME + \" (\" + left(\$feature.PARTY,1) + \")\\nDistrict \" + \$feature.CDFIPS")

        return LabelDefinition(arcadeLabelExpression, textSymbol).apply {
            placement = LabelingPlacement.POLYGON_ALWAYS_HORIZONTAL
            whereClause = String.format("PARTY = '%s'", party)
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
