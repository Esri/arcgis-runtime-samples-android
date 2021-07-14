/*
 *  Copyright 2019 Esri
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

package com.esri.arcgisruntime.sample.traceutilitynetwork

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.security.UserCredential
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import com.esri.arcgisruntime.utilitynetworks.UtilityElement
import com.esri.arcgisruntime.utilitynetworks.UtilityElementTraceResult
import com.esri.arcgisruntime.utilitynetworks.UtilityNetwork
import com.esri.arcgisruntime.utilitynetworks.UtilityNetworkSource
import com.esri.arcgisruntime.utilitynetworks.UtilityTerminal
import com.esri.arcgisruntime.utilitynetworks.UtilityTier
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceParameters
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceType
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.utility_network_controls_layout.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private var mediumVoltageTier: UtilityTier? = null

    private val graphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }

    private val featureServiceUrl =
        "https://sampleserver7.arcgisonline.com/server/rest/services/UtilityNetwork/NapervilleElectric/FeatureServer"

    private val serviceGeodatabase by lazy {
        ServiceGeodatabase(featureServiceUrl).apply {
            // define user credentials for authenticating with the service
            // NOTE: a licensed user is required to perform utility network operations
            credential = UserCredential("viewer01", "I68VGU^nMurF")
        }
    }

    private val utilityNetwork: UtilityNetwork by lazy {
        UtilityNetwork(featureServiceUrl).apply {
            // define user credentials for authenticating with the service
            // NOTE: a licensed user is required to perform utility network operations
            credential = UserCredential("viewer01", "I68VGU^nMurF")
        }
    }

    // create lists for starting locations and barriers
    private val utilityElementStartingLocations: MutableList<UtilityElement> by lazy { ArrayList() }
    private val utilityElementBarriers: MutableList<UtilityElement> by lazy { ArrayList() }

    // create symbols for the starting point and barriers
    private val startingPointSymbol: SimpleMarkerSymbol by lazy {
        SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.GREEN, 25f)
    }
    private val barrierPointSymbol: SimpleMarkerSymbol by lazy {
        SimpleMarkerSymbol(SimpleMarkerSymbol.Style.X, Color.RED, 25f)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        serviceGeodatabase.loadAsync()
        serviceGeodatabase.addDoneLoadingListener {
            serviceGeodatabase.loadStatus
            // create electrical distribution line layer from the service geodatabase
            val electricalDistributionFeatureLayer =
                FeatureLayer(serviceGeodatabase.getTable(3)).apply {
                    // define a solid line for medium voltage lines
                    val mediumVoltageValue = UniqueValueRenderer.UniqueValue(
                        "N/A",
                        "Medium voltage",
                        SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.CYAN, 3f),
                        listOf(5)
                    )
                    // define a dashed line for low voltage lines
                    val lowVoltageValue = UniqueValueRenderer.UniqueValue(
                        "N/A",
                        "Low voltage",
                        SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.CYAN, 3f),
                        listOf(3)
                    )
                    // create and apply a solid light gray renderer
                    renderer =
                        UniqueValueRenderer(
                            listOf("ASSETGROUP"),
                            listOf(mediumVoltageValue, lowVoltageValue),
                            "",
                            SimpleLineSymbol()
                        )
                }

            // create electrical device layer from the service geodatabase
            val electricalDeviceFeatureLayer = FeatureLayer(serviceGeodatabase.getTable(0))


            // setup the map view
            mapView.apply {
                // add a map with streets night vector basemap
                map = ArcGISMap(BasemapStyle.ARCGIS_STREETS_NIGHT).apply {
                    operationalLayers.apply {
                        add(electricalDistributionFeatureLayer)
                        add(electricalDeviceFeatureLayer)
                    }
                    // add the utility network to the map
                    utilityNetworks.add(utilityNetwork)
                }

                // set the viewpoint to a section in the southeast of the network
                setViewpointAsync(
                    Viewpoint(
                        Envelope(
                            -9813547.35557238,
                            5129980.36635111,
                            -9813185.0602376,
                            5130215.41254146,
                            SpatialReferences.getWebMercator()
                        )
                    )
                )

                // set the selection color for features in the map view
                selectionProperties.color = Color.YELLOW

                // add a graphics overlay
                graphicsOverlays.add(graphicsOverlay)

                // handle taps on the map view
                onTouchListener =
                    object : DefaultMapViewOnTouchListener(applicationContext, mapView) {
                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            // only pass taps to identify nearest utility element once the utility network has loaded
                            if (utilityNetwork.loadStatus == LoadStatus.LOADED) {
                                identifyNearestUtilityElement(
                                    android.graphics.Point(e.x.roundToInt(), e.y.roundToInt())
                                )
                                return true
                            }
                            return false
                        }
                    }
            }

            // load the utility network
            utilityNetwork.addDoneLoadingListener {
                if (utilityNetwork.loadStatus == LoadStatus.LOADED) {
                    // update the status text
                    statusTextView.text = getString(R.string.click_to_add_points)

                    // get the utility tier used for traces in this network, in this case "Medium Voltage Radial"
                    val domainNetwork =
                        utilityNetwork.definition.getDomainNetwork("ElectricDistribution")
                    mediumVoltageTier = domainNetwork.getTier("Medium Voltage Radial")

                } else {
                    reportError("Error loading utility network: " + utilityNetwork.loadError.cause?.message)
                }
            }
            utilityNetwork.loadAsync()
        }

        // add all utility trace types to the trace type spinner as strings
        traceTypeSpinner.adapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_item,
            arrayOf("CONNECTED", "SUBNETWORK", "UPSTREAM", "DOWNSTREAM")
        )
    }

    /**
     * Uses the tapped point to identify any utility elements in the utility network at the tapped
     * location. Based on the selection mode, the tapped utility element is added either to the
     * starting locations or barriers for the trace parameters. An appropriate graphic is created at
     * the tapped location to mark the element as either a starting location or barrier.
     *
     * @param screenPoint used to identify utility elements in the utility network
     */
    private fun identifyNearestUtilityElement(screenPoint: android.graphics.Point) {
        // get the clicked map point
        val mapPoint = mapView.screenToLocation(screenPoint)
        // identify the feature to be used
        val identifyLayerResultFuture = mapView.identifyLayersAsync(screenPoint, 10.0, false)
        identifyLayerResultFuture.addDoneListener {
            // get the result of the query
            try {
                val identifyResults = identifyLayerResultFuture.get()
                // if the identify returns a result, retrieve the geoelement as an ArcGISFeature
                (identifyResults.getOrNull(0)?.elements?.get(0) as? ArcGISFeature)?.let { identifiedFeature ->
                    // get the network source of the identified feature
                    val utilityNetworkSource =
                        utilityNetwork.definition.getNetworkSource(identifiedFeature.featureTable.tableName)
                    // check if the network source is a junction or an edge
                    when (utilityNetworkSource.sourceType) {
                        UtilityNetworkSource.Type.JUNCTION -> {
                            // create a utility element with the identified feature
                            createUtilityElement(identifiedFeature, utilityNetworkSource)
                        }
                        UtilityNetworkSource.Type.EDGE -> {
                            //  create a utility element with the identified feature
                            utilityNetwork.createElement(identifiedFeature, null).apply {
                                // calculate how far the clicked location is along the edge feature
                                fractionAlongEdge = GeometryEngine.fractionAlong(
                                    GeometryEngine.removeZ(identifiedFeature.geometry) as Polyline,
                                    mapPoint,
                                    -1.0
                                )
                                // set the trace location graphic to the nearest coordinate to the tapped point
                                addUtilityElementToMap(identifiedFeature, mapPoint, this)
                            }.also {
                                // update the status label text
                                statusTextView.text =
                                    getString(R.string.fraction_message, it.fractionAlongEdge)
                            }
                        }
                        else -> error("Unexpected utility network source type!")
                    }
                }
            } catch (e: Exception) {
                reportError("Error getting identify results: " + e.message)
            }
        }
    }

    /**
     * Add utility element to either the starting locations or barriers list and add a graphic
     * representing it to the graphics overlay.
     *
     * @param identifiedFeature the feature identified by a tap
     * @param mapPoint the map location of the tap
     * @param utilityElement to be added to the map
     */
    private fun addUtilityElementToMap(
        identifiedFeature: ArcGISFeature,
        mapPoint: Point,
        utilityElement: UtilityElement
    ) {
        graphicsOverlay.graphics.add(
            Graphic(
                GeometryEngine.nearestCoordinate(
                    identifiedFeature.geometry,
                    mapPoint
                ).coordinate
            ).apply {
                // add the element to the appropriate list (starting locations or barriers), and add the
                // appropriate symbol to the graphic
                symbol = if (startingLocationsRadioButton.isChecked) {
                    utilityElementStartingLocations.add(utilityElement)
                    startingPointSymbol
                } else {
                    utilityElementBarriers.add(utilityElement)
                    barrierPointSymbol
                }
            })
    }

    /**
     * Uses a UtilityNetworkSource to create a UtilityElement object out of an ArcGISFeature.
     *
     * @param identifiedFeature an ArcGISFeature object that will be used to create a UtilityElement
     * @param networkSource     the UtilityNetworkSource to which the created UtilityElement is associated
     */
    private fun createUtilityElement(
        identifiedFeature: ArcGISFeature,
        networkSource: UtilityNetworkSource
    ) {
        // find the code matching the asset group name in the feature's attributes
        val assetGroupCode =
            identifiedFeature.attributes[identifiedFeature.featureTable.subtypeField] as Int

        // find the network source's asset group with the matching code
        networkSource.assetGroups.filter { it.code == assetGroupCode }[0].assetTypes
            // find the asset group type code matching the feature's asset type code
            .filter { it.code == identifiedFeature.attributes["assettype"].toString().toInt() }[0]
            .let { utilityAssetType ->
                // get the list of terminals for the feature
                val terminals = utilityAssetType.terminalConfiguration.terminals

                // if there is only one terminal, use it to create a utility element
                when (terminals.size) {
                    1 -> {
                        // create a utility element
                        utilityNetwork.createElement(identifiedFeature, terminals[0]).also {
                            // add the utility element to the map
                            addUtilityElementToMap(
                                identifiedFeature,
                                identifiedFeature.geometry as Point,
                                it
                            )
                        }
                    }
                    // if there is more than one terminal, prompt the user to select one
                    else -> {
                        // get a list of terminal names from the terminals
                        val terminalNames =
                            utilityAssetType.terminalConfiguration.terminals.map { it.name }
                        AlertDialog.Builder(this).apply {
                            setTitle("Select utility terminal:")
                            setItems(terminalNames.toTypedArray()) { _, which ->
                                // create a utility element
                                utilityNetwork.createElement(identifiedFeature, terminals[which])
                                    .also {
                                        // add the utility element to the map
                                        addUtilityElementToMap(
                                            identifiedFeature,
                                            identifiedFeature.geometry as Point,
                                            it
                                        )
                                        // show the utility element name in the UI
                                        showTerminalNameInStatusLabel(it.terminal)
                                    }
                            }
                        }.show()
                    }
                }

            }
    }

    /**
     * Shows the name of a UtilityTerminal in the status label in the UI.
     *
     * @param terminal to show information about
     */
    private fun showTerminalNameInStatusLabel(terminal: UtilityTerminal) {
        statusTextView.text =
            getString(
                R.string.terminal_name,
                if (!terminal.name.isNullOrEmpty()) terminal.name else "default"
            )
    }

    /**
     * Uses the elements selected as starting locations and (optionally) barriers to perform a connected trace,
     * then selects all connected elements found in the trace to highlight them.
     */
    fun traceUtilityNetwork(view: View) {
        // check that the utility trace parameters are valid
        if (utilityElementStartingLocations.isEmpty()) {
            reportError("No starting locations provided for trace.")
            return
        }

        // show the progress indicator and update the status text
        progressIndicator.visibility = View.VISIBLE
        statusTextView.text = getString(R.string.find_connected_features_message)
        disableButtons()

        // create utility trace parameters for the given trace type
        val traceType = UtilityTraceType.valueOf(traceTypeSpinner.selectedItem.toString())
        // create trace parameters
        val traceParameters =
            UtilityTraceParameters(traceType, utilityElementStartingLocations).apply {
                // if any barriers have been created, add them to the parameters
                barriers.addAll(utilityElementBarriers)
                // set the trace configuration using the tier from the utility domain network
                traceConfiguration = mediumVoltageTier?.defaultTraceConfiguration
            }
        // run the utility trace and get the results
        val utilityTraceResultsFuture = utilityNetwork.traceAsync(traceParameters)
        utilityTraceResultsFuture.addDoneListener {
            try {
                // get the utility trace result's first result as a utility element trace result
                (utilityTraceResultsFuture.get()[0] as? UtilityElementTraceResult)?.let { utilityElementTraceResult ->
                    // ensure the result is not empty
                    if (utilityElementTraceResult.elements.isNotEmpty()) {
                        // iterate through the map's feature layers
                        mapView.map.operationalLayers.filterIsInstance<FeatureLayer>()
                            .forEach { featureLayer ->

                                // clear previous selection
                                featureLayer.clearSelection()

                                // create query parameters to find features who's network source name matches the layer's feature table name
                                with(QueryParameters()) {
                                    utilityElementTraceResult.elements.filter { it.networkSource.name == featureLayer.featureTable.tableName }
                                        .forEach { utilityElement ->
                                            this.objectIds.add(utilityElement.objectId)
                                        }

                                    // select features that match the query
                                    featureLayer.selectFeaturesAsync(
                                        this,
                                        FeatureLayer.SelectionMode.NEW
                                    )
                                        .addDoneListener {
                                            // when done, update status text, enable buttons and hide progress indicator
                                            statusTextView.text =
                                                getString(R.string.trace_completed)
                                            enableButtons()
                                            progressIndicator.visibility = View.GONE
                                        }
                                }
                            }
                    } else {
                        Toast.makeText(this, "No elements in trace result", Toast.LENGTH_LONG)
                            .show()
                        progressIndicator.visibility = View.GONE
                        enableButtons()
                    }
                }
            } catch (e: Exception) {
                statusTextView.text = getString(R.string.failed_message)
                progressIndicator.visibility = View.GONE
                reportError("Error running connected trace: " + e.message)
            }
        }
    }

    /**
     * Enables both buttons.
     */
    private fun enableButtons() {
        // enable the UI
        resetButton.isEnabled = true
        traceButton.isEnabled = true
    }

    /**
     * Disables both buttons.
     */
    private fun disableButtons() {
        // enable the UI
        resetButton.isEnabled = false
        traceButton.isEnabled = false
    }

    /**
     * Restores the sample to the startup-state by resetting the status text, hiding the progress indicator, clearing
     * the trace parameters, de-selecting all features and removing any graphics
     */
    fun reset(view: View) {
        statusTextView.text = getString(R.string.add_utility_element)
        progressIndicator.visibility = View.GONE

        // clear the utility trace parameters
        utilityElementStartingLocations.clear()
        utilityElementBarriers.clear()
        // clear any selected features in the map's feature layers
        mapView.map.operationalLayers.filterIsInstance<FeatureLayer>().forEach {
            it.clearSelection()
        }
        // clear the graphics overlay
        graphicsOverlay.graphics.clear()
        // enable the trace button
        traceButton.isEnabled = true
    }

    /**
     * Report the given error to the user by toast and log.
     *
     * @param error as a string
     */
    private fun reportError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(this::class.java.simpleName, error)
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
