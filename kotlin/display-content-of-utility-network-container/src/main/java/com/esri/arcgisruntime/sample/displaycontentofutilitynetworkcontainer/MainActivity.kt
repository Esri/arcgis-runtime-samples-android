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

package com.esri.arcgisruntime.sample.displaycontentofutilitynetworkcontainer

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.SubtypeFeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.sample.displaycontentofutilitynetworkcontainer.databinding.ActivityMainBinding
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.UserCredential
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.Symbol
import com.esri.arcgisruntime.utilitynetworks.UtilityAssociationType
import com.esri.arcgisruntime.utilitynetworks.UtilityElement
import com.esri.arcgisruntime.utilitynetworks.UtilityNetwork


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val legendButton: Button by lazy {
        activityMainBinding.legendButton
    }

    private val progressBar: ProgressBar by lazy {
        activityMainBinding.progressBar
    }

    private val exitButton: Button by lazy {
        activityMainBinding.exitButton
    }

    private lateinit var selectedContainerFeature: ArcGISFeature
    private lateinit var graphicsOverlay: GraphicsOverlay
    private lateinit var boundingBoxSymbol: SimpleLineSymbol
    private lateinit var attachmentSymbol: SimpleLineSymbol
    private lateinit var connectivitySymbol: SimpleLineSymbol
    private lateinit var utilityNetwork: UtilityNetwork
    private lateinit var previousViewpoint: Viewpoint

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // create a new graphics overlay to display container view contents
        graphicsOverlay = GraphicsOverlay()
        mapView.graphicsOverlays.add(graphicsOverlay)

        // create three new simple line symbols for displaying container view features
        boundingBoxSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.YELLOW, 3F)
        attachmentSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DOT, Color.BLUE, 3F)
        connectivitySymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DOT, Color.RED, 3F)

        //TODO
        // set image views for the association and bounding box symbols to display them in the legend
        //attachmentImageView.setImage(attachmentSymbol);
        //connectivityImageView.setImage(connectivitySymbol);
        //boundingBoxImageView.setImage(boundingBoxSymbol);

        // set user credentials to authenticate with the feature service and webmap url
        // NOTE: a licensed user is required to perform utility network operations
        // NOTE: Never hardcode login information in a production application. This is done solely for the sake of the sample.
        val userCredential = UserCredential("viewer01", "I68VGU^nMurF")
        val authenticationChallengeHandler =
            AuthenticationChallengeHandler {
                AuthenticationChallengeResponse(
                    AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
                    userCredential
                )
            }
        AuthenticationManager.setAuthenticationChallengeHandler(authenticationChallengeHandler)

        // create a new map from the web map URL (includes ArcGIS Pro subtype group layers with only container features visible)
        val map =
            ArcGISMap("https://sampleserver7.arcgisonline.com/portal/home/item.html?id=813eda749a9444e4a9d833a4db19e1c8")
        // the feature service url contains a utility network used to find associations shown in this sample
        val featureServiceURL =
            "https://sampleserver7.arcgisonline.com/server/rest/services/UtilityNetwork/NapervilleElectric/FeatureServer"

        // create a utility network, add it to the map's collection of utility networks, and load it
        utilityNetwork = UtilityNetwork(featureServiceURL)
        map.utilityNetworks.add(utilityNetwork)
        utilityNetwork.addDoneLoadingListener {
            // show an error if the utility network did not load
            if (utilityNetwork.loadStatus != LoadStatus.LOADED) {
                logError("If utility status did not load")
            }
        }
        utilityNetwork.loadAsync()

        // hide the progress indicator once the map view draw status has completed
        mapView.addDrawStatusChangedListener { listener: DrawStatusChangedEvent ->
            if (listener.drawStatus == DrawStatus.COMPLETED) {
                progressBar.visibility = View.GONE
            }
        }

        // set the map to the mapview and set the map view's viewpoint
        mapView.map = map
        mapView.setViewpoint(Viewpoint(41.801504, -88.163718, 4e3))

        mapView.onTouchListener =
            object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
                @SuppressLint("ClickableViewAccessibility")
                override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
                    motionEvent?.let { event ->
                        // create a point from where the user clicked
                        android.graphics.Point(event.x.toInt(), event.y.toInt())
                            .let { point ->
                                // add a new feature to the service feature table
                                handleMapViewClicked(point)
                            }
                    }
                    return super.onSingleTapConfirmed(motionEvent)
                }
            }

        exitButton.setOnClickListener {
            //TODO vBox.setVisible(false)
            graphicsOverlay.graphics.clear()
            mapView.isEnabled = true
            mapView.setViewpointAsync(previousViewpoint)
            mapView.map.operationalLayers.forEach { layer ->
                layer.isVisible = true
            }
        }
    }

    private fun handleMapViewClicked(mapPoint: android.graphics.Point) {
        // identify the feature clicked on
        val identifyLayerResultsFuture = mapView.identifyLayersAsync(mapPoint, 10.0, false)
        identifyLayerResultsFuture.addDoneListener {
            try {
                // get the result of the query
                val identifyLayerResults = identifyLayerResultsFuture.get()
                // check that results have been returned
                if (identifyLayerResults.isNotEmpty()) {
                    identifyLayerResults.forEach { layerResult ->
                        // check if the layer result is a subtype feature layer
                        if (layerResult.layerContent is SubtypeFeatureLayer) {
                            // loop through each sub layer result
                            layerResult.sublayerResults.forEach { sublayerResult ->
                                // filter the sublayer result's elements to find the first one which is an ArcGIS feature
                                selectedContainerFeature =
                                    sublayerResult.elements.filterIsInstance<ArcGISFeature>()
                                        .first()
                            }
                        }
                    }
                    // create a container element using the selected feature
                    if (selectedContainerFeature != null) {
                        val containerElement =
                            utilityNetwork.createElement(selectedContainerFeature)
                        // get the containment associations from this element to display its content
                        val containmentAssociationsFuture = utilityNetwork.getAssociationsAsync(
                            containerElement,
                            UtilityAssociationType.CONTAINMENT
                        )
                        containmentAssociationsFuture.addDoneListener {
                            try {
                                // get and store a list of elements from the result of the query
                                val contentElements: MutableList<UtilityElement> = mutableListOf()
                                // get the list of containment associations and loop through them to get their elements
                                val containmentAssociations = containmentAssociationsFuture.get()
                                containmentAssociations.forEach { association ->
                                    val utilityElement =
                                        if (association.fromElement.objectId == containerElement.objectId) association.toElement
                                        else association.fromElement
                                    contentElements.add(utilityElement)
                                }

                                // check the list of elements isn't empty, and store the current viewpoint (this will be used later
                                // when exiting the container view
                                if (contentElements.isNotEmpty()) {
                                    previousViewpoint =
                                        mapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY)
                                    mapView.map.operationalLayers.forEach { layer ->
                                        layer.isVisible = false
                                    }

                                    // enable container view vbox and disable interaction with the map view to avoid navigating away from container view
                                    mapView.isEnabled = false
                                    //TODO vBox.setVisible(true);

                                    // fetch the features from the elements
                                    val fetchFeaturesFuture =
                                        utilityNetwork.fetchFeaturesForElementsAsync(contentElements)
                                    fetchFeaturesFuture.addDoneListener {
                                        try {
                                            // get the content features and give them each a symbol, and add them as a graphic to the graphics overlay
                                            val contentFeatures = fetchFeaturesFuture.get()
                                            contentFeatures.forEach { content ->
                                                val symbol: Symbol =
                                                    content.featureTable.layerInfo.drawingInfo.renderer.getSymbol(
                                                        content
                                                    )
                                                graphicsOverlay.graphics.add(
                                                    Graphic(
                                                        content.geometry,
                                                        symbol
                                                    )
                                                )
                                            }
                                            val firstGraphic = graphicsOverlay.graphics[0].geometry
                                            val containerViewScale =
                                                containerElement.assetType.containerViewScale
                                            if (graphicsOverlay.graphics.size == 1 && firstGraphic is Point) {
                                                mapView.setViewpointCenterAsync(
                                                    firstGraphic,
                                                    containerViewScale
                                                ).addDoneListener {
                                                    // the bounding box, which defines the container view, may be computed using the extent of the features
                                                    // it contains or centered around its geometry at the container's view scale
                                                    val boundingBox =
                                                        mapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).targetGeometry
                                                    identifyAssociationsWithExtent(boundingBox)
                                                    logError("This feature has no associations")
                                                }
                                            } else {
                                                val boundingBox: Geometry = GeometryEngine.buffer(
                                                    graphicsOverlay.extent,
                                                    0.05
                                                )
                                                identifyAssociationsWithExtent(boundingBox)
                                            }
                                        } catch (e: Exception) {
                                            logError("Error fetching features for elements")
                                        }
                                    }
                                } else {
                                    logError("No content elements found")
                                }
                            } catch (e: Exception) {
                                logError("Error getting containment associations")
                            }
                        }
                    } else {
                        logError("No feature found")
                    }
                }
            } catch (e: Exception) {
                logError("Error getting result of the query")
            }
        }
    }

    /**
     * Get associations for the specified geometry and display its associations
     * using [boundingBox] the geometry from which to get associations.
     */
    private fun identifyAssociationsWithExtent(boundingBox: Geometry?) {
        // adds a graphic representing the bounding box of the associations identified and zooms to its extent
        graphicsOverlay.graphics.add(Graphic(boundingBox, boundingBoxSymbol))
        mapView.setViewpointGeometryAsync(GeometryEngine.buffer(graphicsOverlay.extent, 0.05))

        // get the associations for this extent to display how content features are attached or connected.
        val extentAssociations = utilityNetwork.getAssociationsAsync(graphicsOverlay.extent)
        extentAssociations.addDoneListener {
            try {
                extentAssociations.get().forEach { association ->
                    // assign the appropriate symbol if the association is an attachment or connectivity type
                    val symbol: Symbol =
                        if (association.associationType === UtilityAssociationType.ATTACHMENT) attachmentSymbol
                        else connectivitySymbol
                    graphicsOverlay.graphics.add(Graphic(association.geometry, symbol))
                }
            } catch (e: Exception) {
                logError("Error getting extent associations")
            }
        }
    }

    private fun logError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, message)
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
