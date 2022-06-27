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
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.GeoPackage
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
import com.google.android.material.textfield.TextInputLayout
import java.io.File


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private var previousSource: FeatureLayerSource? = null

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val bottomMenu: TextInputLayout by lazy {
        activityMainBinding.bottomMenu
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)
        // set the map to be displayed in the layout's MapView
        mapView.map = map

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.feature_layer_items)
        )
        val featureLayersMenu = (bottomMenu.editText as? AutoCompleteTextView)
        featureLayersMenu?.setAdapter(adapter)
        featureLayersMenu?.setOnItemClickListener { _, _, i, _ ->
            // get the selected feature layer source
            val selectedSource = FeatureLayerSource.values().first { it.menuPosition == i }
            // check if the same feature is selected
            if (previousSource != null && (previousSource == selectedSource)) {
                // same feature layer selected, return
                return@setOnItemClickListener
            }
            // set the feature layer source using the selected source
            when (selectedSource) {
                FeatureLayerSource.SERVICE_FEATURE_TABLE -> loadFeatureServiceURL()
                FeatureLayerSource.PORTAL_ITEM -> portalItem()
                FeatureLayerSource.GEODATABASE -> geodatabase()
                FeatureLayerSource.GEOPACKAGE -> geopackage()
                FeatureLayerSource.SHAPEFILE -> shapefile()
            }
            // update the previous feature layer source
            previousSource = selectedSource
        }
    }

    /**
     * Load a feature layer with a URL
     */
    private fun loadFeatureServiceURL() {
        // initialize the service feature table using a URL.
        val serviceFeatureTable =
            ServiceFeatureTable(resources.getString(R.string.sample_service_url))
        // create a feature layer with the feature table.
        val featureLayer = FeatureLayer(serviceFeatureTable)
        val viewpoint = Viewpoint(41.773519, -88.143104, 4000.0)
        setFeatureLayer(featureLayer, viewpoint)
    }

    /**
     * Load a feature layer with a portal item
     */
    private fun portalItem() {
        // set the portal
        val portal = Portal("https://www.arcgis.com", false)
        // create the portal item with the item ID for the Portland tree service data
        val portalItem = PortalItem(portal, "1759fd3e8a324358a0c58d9a687a8578")
        // create the feature layer with the item and layer ID
        val featureLayer = FeatureLayer(portalItem)
        // set the viewpoint to Portland, Oregon
        val viewpoint = Viewpoint(45.5266, -122.6219, 6000.0)
        setFeatureLayer(featureLayer, viewpoint)

    }

    /**
     * Load a feature layer with a local shapefile
     */
    private fun shapefile() {
        // locate the shape file in device
        val file = File(
            getExternalFilesDir(null),
            "/ScottishWildlifeTrust_reserves/ScottishWildlifeTrust_ReserveBoundaries_20201102.shp"
        )
        // Create a shapefile feature table from a named bundle resource.
        val shapeFileTable = ShapefileFeatureTable(file.path)
        // Create a feature layer for the shapefile feature table
        val featureLayer = FeatureLayer(shapeFileTable)
        val viewpoint = Viewpoint(56.641344, -3.889066, 6000000.0)
        setFeatureLayer(featureLayer, viewpoint)
    }

    private fun geopackage() {
        val file = File(getExternalFilesDir(null), "/AuroraCO.gpkg")
        val geoPackage = GeoPackage(file.path)
        geoPackage.loadAsync()
        geoPackage.addDoneLoadingListener {
            if (geoPackage.loadStatus == LoadStatus.LOADED) {
                val featureLayer = FeatureLayer(geoPackage.geoPackageFeatureTables.first())
                val viewpoint = Viewpoint(39.7294, -104.8319, 500000.0)
                setFeatureLayer(featureLayer,viewpoint)
            }
        }
    }

    private fun geodatabase() {
        TODO("Not yet implemented")
    }

    private fun setFeatureLayer(layer: FeatureLayer, viewpoint: Viewpoint) {
        mapView.map.operationalLayers.clear()
        mapView.map.operationalLayers.add(layer)
        mapView.setViewpoint(viewpoint)
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

    enum class FeatureLayerSource(val menuPosition: Int) {
        SERVICE_FEATURE_TABLE(0),
        PORTAL_ITEM(1),
        GEODATABASE(2),
        GEOPACKAGE(3),
        SHAPEFILE(4)
    }
}
