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

package com.esri.arcgisruntime.sample.browseogcapifeatureservice

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.FeatureTable
import com.esri.arcgisruntime.data.OgcFeatureCollectionTable
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.OgcFeatureCollectionInfo
import com.esri.arcgisruntime.layers.OgcFeatureService
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.symbology.Renderer
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import kotlinx.android.synthetic.main.activity_main.*

// URL to the OAFeat service
private const val serviceUrl = "https://demo.ldproxy.net/daraa"

class MainActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // initialize the text box with a service URL
        serviceEditText.setText(serviceUrl)
        // create a map with topographic basemap
        mapView.map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        // load the OGC feature service
        loadOgcFeatureService()

        // set up ui behavior
        setupUI()
    }

    /**
     * Loads a new OGC Feature Service with the URL in the edit text box. Use the service's info to
     * get titles for all feature collections. Add those titles to the list view and on tap call
     * loadLayer.
     */
    private fun loadOgcFeatureService() {

        // create the OGC API - Features service using the landing URL
        val service = OgcFeatureService(serviceEditText.text.toString())

        // load the OAFeat service
        service.addDoneLoadingListener {
            if (service.loadStatus == LoadStatus.LOADED) {
                // get the service metadata
                val serviceInfo = service.serviceInfo

                // get a list of available collections
                val featureCollectionInfos = serviceInfo.featureCollectionInfos

                // get a list of the collection titles
                val featureCollectionTitles = featureCollectionInfos.map { it.title }

                // create an adapter to show the feature collection titles
                val featureCollectionTitleAdapter = ArrayAdapter(
                    this,
                    R.layout.feature_collection_title_list_item,
                    featureCollectionTitles
                )

                featureCollectionTitleListView.apply {
                    // add the adapter to the list view
                    adapter = featureCollectionTitleAdapter
                    // set an on item click listener for items in the list view
                    setOnItemClickListener { _, _, position, _ ->
                        // load the selected collection
                        loadLayer(featureCollectionInfos[position])
                    }
                }

                // expand the layers list
                layerFAB.isExpanded = true
            } else {
                val error = "Error loading OGC feature service: " + service.loadError.message
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                Log.e(TAG, error)
            }
        }
        service.loadAsync()
    }

    /**
     * Load and query features from the given OGC Feature Collection. Create a Feature Layer from
     * the OGC feature collection table and add it to the map's operational layers.
     *
     * @param selectedCollectionInfo used to create an OgcFeatureCollectionTable
     */
    private fun loadLayer(selectedCollectionInfo: OgcFeatureCollectionInfo) {
        // create the OGC feature collection table
        val ogcFeatureCollectiontable = OgcFeatureCollectionTable(selectedCollectionInfo)

        // populate the OGC feature collection table
        val queryParameters = QueryParameters().apply {
            maxFeatures = 1000
        }

        ogcFeatureCollectiontable.apply {
            // set the feature request mode to manual (only manual is currently supported). In this mode,
            // you must manually populate the table - panning and zooming won't request features
            // automatically
            featureRequestMode = ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE

            // populate the table from the service
            populateFromServiceAsync(queryParameters, false, null)

            addDoneLoadingListener {
                if (ogcFeatureCollectiontable.loadStatus == LoadStatus.LOADED) {
                    // create a feature layer from the OGC feature collection table
                    val ogcFeatureLayer = FeatureLayer(ogcFeatureCollectiontable).apply {
                        // Choose a renderer for the layer based on the table.
                        renderer = getRendererForTable(ogcFeatureCollectiontable) ?: this.renderer
                    }

                    mapView.map.operationalLayers.apply {
                        // clear previous layers from the map
                        clear()
                        // add the layer to the map
                        add(ogcFeatureLayer)
                    }

                    // zoom to the extent of the selected collection
                    val collectionExtent = selectedCollectionInfo.extent
                    if (!collectionExtent.isEmpty) {
                        mapView.setViewpointGeometryAsync(collectionExtent, 100.0)
                    }

                    // hide the layer list
                    layerFAB.isExpanded = false
                } else {
                    val error =
                        "Error loading OGC Feature Collection table: " + ogcFeatureCollectiontable.loadError.message
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                    Log.e(TAG, error)
                }
            }
        }
    }

    /**
     * Return a simple renderer for points, lines and polygons.
     *
     * @param featureTable of features to return simple renderer for
     */
    private fun getRendererForTable(featureTable: FeatureTable): Renderer? {
        return when (featureTable.geometryType) {
            GeometryType.POINT, GeometryType.MULTIPOINT -> return SimpleRenderer(
                SimpleMarkerSymbol(
                    SimpleMarkerSymbol.Style.CIRCLE,
                    Color.BLUE,
                    5f
                )
            )
            GeometryType.POLYGON, GeometryType.ENVELOPE -> return SimpleRenderer(
                SimpleFillSymbol(
                    SimpleFillSymbol.Style.SOLID,
                    Color.BLUE,
                    null
                )
            )
            GeometryType.POLYLINE -> return SimpleRenderer(
                SimpleLineSymbol(
                    SimpleLineSymbol.Style.SOLID,
                    Color.BLUE,
                    1f
                )
            )
            else -> null
        }
    }

    /**
     * Sets up UI behavior. Closes expandable floating action button on touching the map view.
     * Moves floating action button on attribution view expanded. Expands floating action button on
     * tap.
     */
    private fun setupUI() {
        mapView.apply {
            // create a touch listener
            onTouchListener = object : DefaultMapViewOnTouchListener(applicationContext, mapView) {
                // close the options sheet when the map is tapped
                override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                    if (layerFAB.isExpanded) {
                        layerFAB.isExpanded = false
                    }
                    return super.onTouch(view, motionEvent)
                }
            }
            // ensure the floating action button moves to be above the attribution view
            addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                val heightDelta = bottom - oldBottom
                (layerFAB.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin += heightDelta
            }
        }

        // show the options sheet when the floating action button is clicked
        layerFAB.setOnClickListener {
            layerFAB.isExpanded = !layerFAB.isExpanded
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
