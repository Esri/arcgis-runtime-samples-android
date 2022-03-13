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

package com.esri.arcgisruntime.sample.featurecollectionlayerportalitem

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.FeatureCollection
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.featurecollectionlayerportalitem.databinding.ActivityMainBinding

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

        // create a portal item from an ArcGIS Online portal and a feature collection item id
        val portal = Portal("https://www.arcgis.com/")
        val collectionItem = PortalItem(portal, "32798dfad17942858d5eef82ee802f0b")

        // if the portal item loaded correctly, create a feature collection layer with the feature collection
        val featureCollection = FeatureCollection(collectionItem)
        val featureCollectionLayer = FeatureCollectionLayer(featureCollection)

        // create a map with the oceans basemap and add it to the map view
        mapView.map = ArcGISMap(BasemapStyle.ARCGIS_OCEANS).apply {
            // add the new feature collection layer to the map
            operationalLayers.add(featureCollectionLayer)
        }

        // check for loading errors using a DoneLoadingListener
        collectionItem.addDoneLoadingListener {
            // abort if the portal item fails to load
            if (collectionItem.loadStatus != LoadStatus.LOADED) {
                val error = "Failed to load portal item: ${collectionItem.loadError.message}"
                Log.e(TAG, error)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                return@addDoneLoadingListener
            }
            // abort if the portal item is not a feature collection, as expected
            if (collectionItem.type != PortalItem.Type.FEATURE_COLLECTION) {
                val error = "Portal item is not a feature collection!"
                Log.e(TAG, error)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                return@addDoneLoadingListener
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
