/* Copyright 2022 Esri
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

package com.esri.arcgisruntime.sample.displayfeaturelayers

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.data.ShapefileFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.displayfeaturelayers.databinding.ActivityMainBinding
import com.esri.arcgisruntime.security.UserCredential
import java.io.File


class MainActivity : AppCompatActivity() {

    // enum to keep track of the selected source to display the feature layer
    enum class FeatureLayerSource(val menuPosition: Int) {
        SERVICE_FEATURE_TABLE(0),
        PORTAL_ITEM(1),
        GEODATABASE(2),
        GEOPACKAGE(3),
        SHAPEFILE(4)
    }

    private val TAG = MainActivity::class.java.simpleName

    // keeps track of the previously selected feature layer source
    private var previousSource: FeatureLayerSource? = null

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val bottomListItems: AutoCompleteTextView by lazy {
        activityMainBinding.bottomListItems
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to
        // access basemaps and other location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with the BasemapStyle topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)
        // set the map to be displayed in the layout's MapView
        mapView.map = map

        // create an adapter with the types of feature layer
        // sources to be displayed in menu
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.feature_layer_sources)
        )

        bottomListItems.apply {
            // populate the bottom list with the feature layer sources
            setAdapter(adapter)
            // click listener when feature layer source is selected
            setOnItemClickListener { _, _, i, _ ->
                // get the selected feature layer source
                val selectedSource = FeatureLayerSource.values().find { it.menuPosition == i }
                // check if the same feature is selected
                if (previousSource != null && (previousSource == selectedSource)) {
                    // same feature layer selected, return
                    return@setOnItemClickListener
                }
                // set the feature layer source using the selected source
                when (selectedSource) {
                    FeatureLayerSource.SERVICE_FEATURE_TABLE -> loadFeatureServiceURL()
                    FeatureLayerSource.PORTAL_ITEM -> loadPortalItem()
                    FeatureLayerSource.GEODATABASE -> loadGeodatabase()
                    FeatureLayerSource.GEOPACKAGE -> loadGeopackage()
                    FeatureLayerSource.SHAPEFILE -> loadShapefile()
                }
                // update the previous feature layer source
                previousSource = selectedSource
            }
        }
    }

    /**
     * Load a feature layer with a URL
     */
    private fun loadFeatureServiceURL() {
        // initialize the service feature table using a URL
        val serviceFeatureTable =
            ServiceFeatureTable(resources.getString(R.string.sample_service_url)).apply {
                // set user credentials to authenticate with the service
                credential = UserCredential("viewer01", "I68VGU^nMurF")
                // NOTE: Never hardcode login information in a production application
                // This is done solely for the sake of the sample
            }
        // create a feature layer with the feature table
        val featureLayer = FeatureLayer(serviceFeatureTable)
        val viewpoint = Viewpoint(41.773519, -88.143104, 4000.0)
        // set the feature layer on the map
        setFeatureLayer(featureLayer, viewpoint)
    }

    /**
     * Load a feature layer with a portal item
     */
    private fun loadPortalItem() {
        // set the portal
        val portal = Portal("https://www.arcgis.com", false)
        // create the portal item with the item ID for the Portland tree service data
        val portalItem = PortalItem(portal, "1759fd3e8a324358a0c58d9a687a8578")
        // create the feature layer with the item and layer ID
        val featureLayer = FeatureLayer(portalItem)
        // set the viewpoint to Portland, Oregon
        val viewpoint = Viewpoint(45.5266, -122.6219, 6000.0)
        // set the feature layer on the map
        setFeatureLayer(featureLayer, viewpoint)
    }

    /**
     * Load a feature layer with a local geodatabase file
     */
    private fun loadGeodatabase() {
        // locate the .geodatabase file in the device
        val geodatabaseFile = File(getExternalFilesDir(null), "/LA_Trails.geodatabase")
        // instantiate the geodatabase with the file path
        val geodatabase = Geodatabase(geodatabaseFile.path)
        // load the geodatabase
        geodatabase.loadAsync()
        geodatabase.addDoneLoadingListener {
            if (geodatabase.loadStatus == LoadStatus.LOADED) {
                // get the feature table with the name
                val geodatabaseFeatureTable = geodatabase.getGeodatabaseFeatureTable("Trailheads")
                // create a feature layer with the feature table
                val featureLayer = FeatureLayer(geodatabaseFeatureTable)
                // set the viewpoint to Malibu, California
                val viewpoint = Viewpoint(34.0772, -118.7989, 600000.0)
                // set the feature layer on the map
                setFeatureLayer(featureLayer, viewpoint)
            } else {
                showError(geodatabase.loadError.message)
            }
        }
    }

    /**
     * Load a feature layer with a local geopackage file
     */
    private fun loadGeopackage() {
        // locate the .gpkg file in the device
        val geopackageFile = File(getExternalFilesDir(null), "/AuroraCO.gpkg")
        // instantiate the geopackage with the file path
        val geoPackage = GeoPackage(geopackageFile.path)
        // load the geopackage
        geoPackage.loadAsync()
        geoPackage.addDoneLoadingListener {
            if (geoPackage.loadStatus == LoadStatus.LOADED) {
                // get the first feature table in the geopackage
                val geoPackageFeatureTable = geoPackage.geoPackageFeatureTables.first()
                // create a feature layer with the feature table
                val featureLayer = FeatureLayer(geoPackageFeatureTable)
                // set the viewpoint to Denver, CO
                val viewpoint = Viewpoint(39.7294, -104.8319, 500000.0)
                // set the feature layer on the map
                setFeatureLayer(featureLayer, viewpoint)
            } else {
                showError(geoPackage.loadError.message)
            }
        }
    }

    /**
     * Load a feature layer with a local shapefile file
     */
    private fun loadShapefile() {
        try {
            // locate the shape file in device
            val file = File(
                getExternalFilesDir(null),
                "/ScottishWildlifeTrust_reserves/ScottishWildlifeTrust_ReserveBoundaries_20201102.shp"
            )
            // create a shapefile feature table from a named bundle resource
            val shapeFileTable = ShapefileFeatureTable(file.path)
            // create a feature layer for the shapefile feature table
            val featureLayer = FeatureLayer(shapeFileTable)
            // set the viewpoint to Scotland
            val viewpoint = Viewpoint(56.641344, -3.889066, 6000000.0)
            // set the feature layer on the map
            setFeatureLayer(featureLayer, viewpoint)
        } catch (e: Exception) {
            showError(e.message)
        }
    }

    /**
     * Sets the map using the loaded [layer] at the given [viewpoint]
     */
    private fun setFeatureLayer(layer: FeatureLayer, viewpoint: Viewpoint) {
        // clears the existing layer on the map
        mapView.map.operationalLayers.clear()
        // adds the new layer to the map
        mapView.map.operationalLayers.add(layer)
        // updates the viewpoint to the given viewpoint
        mapView.setViewpoint(viewpoint)
    }

    private fun showError(message: String?) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, message.toString())
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
