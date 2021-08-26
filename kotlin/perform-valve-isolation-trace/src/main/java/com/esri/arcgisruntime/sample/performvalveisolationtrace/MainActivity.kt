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

package com.esri.arcgisruntime.sample.performvalveisolationtrace

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.security.UserCredential
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.utilitynetworks.UtilityCategory
import com.esri.arcgisruntime.utilitynetworks.UtilityCategoryComparison
import com.esri.arcgisruntime.utilitynetworks.UtilityCategoryComparisonOperator
import com.esri.arcgisruntime.utilitynetworks.UtilityElement
import com.esri.arcgisruntime.utilitynetworks.UtilityElementTraceResult
import com.esri.arcgisruntime.utilitynetworks.UtilityNetwork
import com.esri.arcgisruntime.utilitynetworks.UtilityNetworkDefinition
import com.esri.arcgisruntime.utilitynetworks.UtilityNetworkSource
import com.esri.arcgisruntime.utilitynetworks.UtilityTerminal
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceConfiguration
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceFilter
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceParameters
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceType
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.spinner_text_item.view.*
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val featureServiceUrl =
        "https://sampleserver7.arcgisonline.com/server/rest/services/UtilityNetwork/NapervilleGas/FeatureServer"

    // create a graphics overlay for the starting location and add it to the map view
    private val startingLocationGraphicsOverlay by lazy {
        GraphicsOverlay()
    }

    private val filterBarriersGraphicsOverlay by lazy {
        GraphicsOverlay()
    }

    // objects that implement Loadable must be class fields to prevent being garbage collected before loading
    private val utilityNetwork by lazy {
        UtilityNetwork(featureServiceUrl)
    }

    private var utilityTraceParameters: UtilityTraceParameters? = null

    private val serviceGeodatabase by lazy {
        ServiceGeodatabase(featureServiceUrl)
    }

    // create and apply renderers for the starting point graphics overlay
    private val startingPointSymbol: SimpleMarkerSymbol by lazy {
        SimpleMarkerSymbol(
            SimpleMarkerSymbol.Style.CROSS,
            Color.GREEN,
            25f
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        loadServiceGeodatabase()

        // create a map with the utility network distribution line and device layers
        val map = ArcGISMap(BasemapStyle.ARCGIS_STREETS_NIGHT).apply {
            // create and load the utility network
            addDoneLoadingListener {
                utilityNetworks.add(utilityNetwork)
            }
        }

        mapView.apply {
            this.map = map
            // set the starting location graphic overlay's renderer and add it to the map view
            startingLocationGraphicsOverlay.renderer = SimpleRenderer(startingPointSymbol)
            graphicsOverlays.add(startingLocationGraphicsOverlay)

            // set the viewpoint to a specific location in Naperville, Illinois
            setViewpointAsync(Viewpoint(Point(-9812712.321100, 5124260.390000, 0.000100), 5000.0))

            // make sure the fab doesn't obscure the attribution bar
            addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                val layoutParams = fab.layoutParams as CoordinatorLayout.LayoutParams
                layoutParams.bottomMargin += bottom - oldBottom
            }
            // close the options sheet when the map is tapped
            onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
                override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                    if (fab.isExpanded) {
                        fab.isExpanded = false
                    }
                    return super.onTouch(view, event)
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    // only pass taps to identify nearest utility element once the utility network has loaded
                    if (utilityNetwork.loadStatus == LoadStatus.LOADED) {
                        identifyNearestUtilityElement(
                            android.graphics.Point(e.x.roundToInt(), e.y.roundToInt())
                        )
                        traceTypeSpinner.isEnabled = false
                        return true
                    }
                    return false
                }
            }
        }
        // show the options sheet when the floating action button is clicked
        fab.setOnClickListener {
            fab.isExpanded = !fab.isExpanded
        }
    }

    /**
     * Load the service geodatabase and initialize the layers.
     */
    private fun loadServiceGeodatabase() {
        // set user credentials to authenticate with the service
        // NOTE: a licensed user is required to perform utility network operations
        serviceGeodatabase.credential = UserCredential("viewer01", "I68VGU^nMurF")
        serviceGeodatabase.addDoneLoadingListener {
            if (serviceGeodatabase.loadStatus == LoadStatus.LOADED) {
                // The  gas device layer ./0 and gas line layer ./3 are created from the service geodatabase.
                val gasDeviceLayerTable = serviceGeodatabase.getTable(0)
                val gasLineLayerTable = serviceGeodatabase.getTable(3)
                // load the utility network data from the feature service and create feature layers
                val deviceLayer = FeatureLayer(gasDeviceLayerTable)
                val distributionLineLayer = FeatureLayer(gasLineLayerTable)
                // add the utility network feature layers to the map for display
                mapView.map.operationalLayers.add(deviceLayer)
                mapView.map.operationalLayers.add(distributionLineLayer)
                loadUtilityNetwork()
            } else {
                val error =
                    "Error loading service geodatabase: " + serviceGeodatabase.loadError.cause?.message
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                Log.e(TAG, error)
            }
        }
        serviceGeodatabase.loadAsync()
    }

    /**
     * Create and load a utility network from the string resource url and initialize a starting point
     * from it.
     */
    private fun loadUtilityNetwork(): UtilityNetwork {
        utilityNetwork.loadAsync()
        utilityNetwork.addDoneLoadingListener {
            if (utilityNetwork.loadStatus == LoadStatus.LOADED) {
                // get a trace configuration from a tier
                val networkDefinition = utilityNetwork.definition
                val domainNetwork = networkDefinition.getDomainNetwork("Pipeline")
                val tier = domainNetwork.getTier("Pipe Distribution System")
                val traceConfiguration = tier.traceConfiguration

                // create a trace filter
                traceConfiguration.filter = UtilityTraceFilter()

                // get a default starting location
                val networkSource = networkDefinition.getNetworkSource("Gas Device")
                val assetGroup = networkSource.getAssetGroup("Meter")
                val assetType = assetGroup.getAssetType("Customer")
                val startingLocation = utilityNetwork.createElement(
                    assetType,
                    UUID.fromString("98A06E95-70BE-43E7-91B7-E34C9D3CB9FF")
                )
                // create new base trace parameters
                utilityTraceParameters = UtilityTraceParameters(
                    UtilityTraceType.ISOLATION,
                    Collections.singletonList(startingLocation)
                )

                // get a list of features for the starting location element
                val elementFeaturesFuture =
                    utilityNetwork.fetchFeaturesForElementsAsync(listOf(startingLocation))
                elementFeaturesFuture.addDoneListener {
                    try {
                        val startingLocationFeatures = elementFeaturesFuture.get()
                        if (startingLocationFeatures.isNotEmpty()) {
                            // get the geometry of the first feature for the starting location as a point
                            (startingLocationFeatures[0].geometry as? Point)?.let { startingLocationGeometryPoint ->

                                // create a graphic for the starting location and add it to the graphics overlay
                                val startingLocationGraphic =
                                    Graphic(startingLocationGeometryPoint, startingPointSymbol)
                                startingLocationGraphicsOverlay.graphics.add(startingLocationGraphic)

                                // create a graphics overlay for filter barriers and add it to the map view
                                mapView.graphicsOverlays.add(filterBarriersGraphicsOverlay)

                                // create and apply a renderer for the filter barriers graphics overlay
                                val barrierPointSymbol = SimpleMarkerSymbol(
                                    SimpleMarkerSymbol.Style.CROSS, Color.RED, 25f
                                )
                                filterBarriersGraphicsOverlay.renderer = SimpleRenderer(
                                    barrierPointSymbol
                                )

                                populateCategorySpinner(networkDefinition)

                                traceButton.setOnClickListener {
                                    fab.isExpanded = false
                                    performTrace(
                                        utilityNetwork,
                                        traceConfiguration
                                    )
                                }

                                resetButton.setOnClickListener {
                                    reset()
                                }
                            }
                        } else {
                            val message = "Starting location features not found."
                            Log.i(TAG, message)
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        val error = "Error loading starting location feature: ${e.message}"
                        Log.e(TAG, error)
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                val error = "Error loading utility network: ${utilityNetwork.loadError}"
                Log.e(TAG, error)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
        return utilityNetwork
    }

    private fun identifyNearestUtilityElement(screenPoint: android.graphics.Point) {

        traceControlsTextView.text = getString(R.string.add_another_filter_barrier)

        // ensure the utility network is loaded before processing clicks on the map view
        if (utilityNetwork.loadStatus == LoadStatus.LOADED) {

            // show the progress indicator
            progressBar.visibility = View.VISIBLE

            // identify the feature to be used
            val identifyLayerResultsFuture = mapView.identifyLayersAsync(screenPoint, 10.0, false)
            identifyLayerResultsFuture.addDoneListener {
                try {
                    // get the result of the query
                    val identifyLayerResults = identifyLayerResultsFuture.get()

                    // return if no features are identified
                    if (identifyLayerResults.isNotEmpty()) {

                        // retrieve the identify result elements as ArcGISFeatures
                        val elements = identifyLayerResults.map { it.elements[0] as ArcGISFeature }

                        // create utility elements from the list of ArcGISFeature elements
                        val utilityElements = elements.map { utilityNetwork.createElement(it) }

                        // get a reference to the closest junction if there is one
                        val junction =
                            utilityElements.firstOrNull { it.networkSource.sourceType == UtilityNetworkSource.Type.JUNCTION }

                        // get a reference to the closest edge if there is one
                        val edge =
                            utilityElements.firstOrNull { it.networkSource.sourceType == UtilityNetworkSource.Type.EDGE }

                        // preferentially select junctions, otherwise an edge
                        val utilityElement = junction ?: edge

                        // retrieve the first result and get its contents
                        if (junction != null) {

                            // check if the feature has a terminal configuration and multiple terminals
                            if (junction.assetType.terminalConfiguration != null) {
                                val utilityTerminalConfiguration =
                                    junction.assetType.terminalConfiguration
                                val terminals =
                                    utilityTerminalConfiguration.terminals
                                if (terminals.size > 1) {
                                    // prompt the user to select a terminal for this feature
                                    promptForTerminalSelection(junction, terminals)
                                }
                            }
                        } else if (edge != null) {

                            // get the geometry of the identified feature as a polyline, and remove the z component
                            val polyline =
                                GeometryEngine.removeZ(elements[0].geometry) as Polyline

                            // get the clicked map point
                            val mapPoint = mapView.screenToLocation(screenPoint)

                            // compute how far the clicked location is along the edge feature
                            val fractionAlongEdge =
                                GeometryEngine.fractionAlong(polyline, mapPoint, -1.0)
                            if (fractionAlongEdge.isNaN()) {
                                Toast.makeText(
                                    this,
                                    "Cannot add starting location or barrier here.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@addDoneListener
                            }

                            // set the fraction along edge
                            edge.fractionAlongEdge = fractionAlongEdge

                            // update the status label text
                            Toast.makeText(
                                this,
                                "Fraction along edge: " + edge.fractionAlongEdge.roundToInt(),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        // add the element to the list of filter barriers
                        utilityTraceParameters?.filterBarriers?.add(utilityElement)

                        // get the clicked map point
                        val mapPoint = mapView.screenToLocation(screenPoint)

                        // find the closest coordinate on the selected element to the clicked point
                        val proximityResult =
                            GeometryEngine.nearestCoordinate(elements[0].geometry, mapPoint)

                        // create a graphic for the new utility element
                        val utilityElementGraphic = Graphic().apply {
                            // set the graphic's geometry to the coordinate on the element and add it to the graphics overlay
                            geometry = proximityResult.coordinate
                        }

                        // add utility element graphic to the filter barriers graphics overlay
                        filterBarriersGraphicsOverlay.graphics.add(utilityElementGraphic)
                    }
                } catch (e: Exception) {
                    val error = "Error identifying tapped feature: " + e.message
                    Log.e(TAG, error)
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                } finally {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Prompts the user to select a terminal from a provided list.
     *
     * @param terminals a list of terminals for the user to choose from
     * @return the user's selected terminal
     */
    private fun promptForTerminalSelection(
        utilityElement: UtilityElement,
        terminals: List<UtilityTerminal>
    ) {
        // get a list of terminal names from the terminals
        val terminalNames = terminals.map { it.name }
        AlertDialog.Builder(this).apply {
            setTitle("Select utility terminal:")
            setItems(terminalNames.toTypedArray()) { _, which ->
                // apply the selected terminal
                val terminal = terminals[which]
                utilityElement.terminal = terminal
                // show the terminals name in the status label
                val terminalName = if (terminal.name != null) terminal.name else "default"
                Toast.makeText(
                    this@MainActivity,
                    "Feature added at terminal: $terminalName",
                    Toast.LENGTH_LONG
                ).show()
            }.show()
        }
    }

    /**
     * Performs a valve isolation trace according to the defined trace configuration and starting
     * location, and selects the resulting features on the map.
     *
     * @param utilityNetwork the utility network to perform the trace on
     * @param traceConfiguration the trace configuration to apply to the trace
     */
    private fun performTrace(
        utilityNetwork: UtilityNetwork,
        traceConfiguration: UtilityTraceConfiguration
    ) {
        progressBar.visibility = View.VISIBLE
        // create a category comparison for the trace
        // NOTE: UtilityNetworkAttributeComparison or UtilityCategoryComparisonOperator.DOES_NOT_EXIST
        // can also be used. These conditions can be joined with either UtilityTraceOrCondition or UtilityTraceAndCondition.
        val utilityCategoryComparison = UtilityCategoryComparison(
            traceTypeSpinner.selectedItem as UtilityCategory,
            UtilityCategoryComparisonOperator.EXISTS
        )
        // set the category comparison to the barriers of the configuration's trace filter
        traceConfiguration.apply {
            filter = UtilityTraceFilter()
            filter.barriers = utilityCategoryComparison
            // set the configuration to include or leave out isolated features
            isIncludeIsolatedFeatures = includeIsolatedSwitch.isChecked
        }

        // build parameters for the isolation trace
        utilityTraceParameters!!.traceConfiguration = traceConfiguration

        // run the trace and get the result
        val utilityTraceResultsFuture = utilityNetwork.traceAsync(utilityTraceParameters)
        utilityTraceResultsFuture.addDoneListener {
            try {
                // get the first element of the trace result if it is not null
                (utilityTraceResultsFuture.get()[0] as? UtilityElementTraceResult)?.let { utilityElementTraceResult ->
                    if (utilityElementTraceResult.elements.isNotEmpty()) {
                        // iterate over the map's feature layers
                        mapView.map.operationalLayers.filterIsInstance<FeatureLayer>()
                            .forEach { featureLayer ->
                                // clear any selections from a previous trace
                                featureLayer.clearSelection()

                                val queryParameters = QueryParameters()
                                // for each utility element in the trace, check if its network source is the same as
                                // the feature table, and if it is, add it to the query parameters to be selected
                                utilityElementTraceResult.elements.filter { it.networkSource.name == featureLayer.featureTable.tableName }
                                    .forEach { utilityElement ->
                                        queryParameters.objectIds.add(utilityElement.objectId)
                                    }

                                // select features that match the query
                                featureLayer.selectFeaturesAsync(
                                    queryParameters,
                                    FeatureLayer.SelectionMode.NEW
                                )
                            }
                    } else {
                        // iterate over the map's feature layers
                        mapView.map.operationalLayers.filterIsInstance<FeatureLayer>()
                            .forEach { featureLayer ->
                                // clear any selections from a previous trace
                                featureLayer.clearSelection()
                            }
                        // trace result is empty
                        val message = "Utility Element Trace Result had no elements!"
                        Log.i(TAG, message)
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
                // hide the progress bar when the trace is completed or failed
                progressBar.visibility = View.GONE
            } catch (e: Exception) {
                val error = "Error loading utility trace results: ${e.message}"
                Log.e(TAG, error)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun reset() {
        traceControlsTextView.text = getString(R.string.choose_category_for_filter_barrier)
        traceTypeSpinner.isEnabled = true
        mapView.map.operationalLayers.forEach { layer ->
            (layer as? FeatureLayer)?.clearSelection()
        }
        utilityTraceParameters?.filterBarriers?.clear()
        filterBarriersGraphicsOverlay.graphics.clear()
    }

    /**
     * Initialize the category selection spinner using a utility network definition.
     *
     * @param networkDefinition the utility network definition to populate the spinner
     */
    private fun populateCategorySpinner(
        networkDefinition: UtilityNetworkDefinition
    ) {
        // populate the spinner with utility categories as the data and their names as the text
        traceTypeSpinner.adapter =
            object : ArrayAdapter<UtilityCategory>(
                this,
                R.layout.spinner_text_item,
                networkDefinition.categories
            ) {
                override fun getView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val spinnerItem = LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.spinner_text_item, parent, false)
                    spinnerItem.textView.text = (getItem(position) as UtilityCategory).name
                    return spinnerItem
                }

                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View = getView(position, convertView, parent)
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
