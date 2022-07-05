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
package com.esri.arcgisruntime.sample.featurelayerdefinitionexpression

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.featurelayerdefinitionexpression.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // set flag for applying expression to feature layer
    private var applyExpression: Boolean = true

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val applyExpressionButton: Button by lazy {
        activityMainBinding.applyExpressionButton
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a service feature table from a URL
        val serviceFeatureTable = ServiceFeatureTable(getString(R.string.sample_service_url))
        // create a feature layer using the service feature table
        val featureLayer = FeatureLayer(serviceFeatureTable)

        // create a map with a topographic base map
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC).apply {
            // add the feature layer to the map
            operationalLayers.add(featureLayer)
        }

        mapView.apply {
            // set the map to be displayed in the layout's map view
            this.map = map
            // set a viewpoint on the map view to center on San Francisco
            setViewpointCenterAsync(
                Point(-13630845.0, 4544861.0, SpatialReferences.getWebMercator()), 200000.0
            )
        }

        // set button listener
        applyExpressionButton.setOnClickListener {
            if (applyExpression) {
                // set the definition expression to nothing (empty string, null also works)
                featureLayer.definitionExpression = ""
                // change the text to apply
                applyExpressionButton.text = getString(R.string.action_def_exp)
                applyExpression = false
            } else {
                // apply a definition expression on the feature layer
                // if this is called before the layer is loaded, it will be applied to the loaded layer
                featureLayer.definitionExpression = "req_Type = 'Tree Maintenance or Damage'"
                // change the text to reset
                applyExpressionButton.text = getString(R.string.action_reset)
                applyExpression = true
            }
        }
        // click to set initial state
        applyExpressionButton.performClick()
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
