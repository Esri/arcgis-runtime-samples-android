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
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.esri.arcgisruntime.sample.displaycontentofutilitynetworkcontainer.databinding.UtilityNetworkLegendBinding
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.UserCredential
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.Symbol
import com.esri.arcgisruntime.utilitynetworks.UtilityAssociation
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

    private val exitButton: Button by lazy {
        activityMainBinding.exitButton
    }

    private val legendButton: Button by lazy {
        activityMainBinding.legendButton
    }

    private val progressBar: ProgressBar by lazy {
        activityMainBinding.progressBar
    }

    // create graphic overlay to display the utility network associations
    private val graphicsOverlay: GraphicsOverlay = GraphicsOverlay()

    // instance of the legend alert dialog
    private var legendDialog: AlertDialog? = null

    // create three new simple line symbols for displaying container view features
    private val boundingBoxSymbol: SimpleLineSymbol =
        SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.YELLOW, 3F)
    private val attachmentSymbol: SimpleLineSymbol =
        SimpleLineSymbol(SimpleLineSymbol.Style.DOT, Color.BLUE, 3F)
    private val connectivitySymbol: SimpleLineSymbol =
        SimpleLineSymbol(SimpleLineSymbol.Style.DOT, Color.RED, 3F)

    // the feature service url contains a utility network used to find associations shown in this sample
    private val utilityNetwork: UtilityNetwork =
        UtilityNetwork("https://sampleserver7.arcgisonline.com/server/rest/services/UtilityNetwork/NapervilleElectric/FeatureServer")

    // use the previous viewpoint when exiting the container view
    private var previousViewpoint: Viewpoint? = null

    // needed to avoid the DefaultMapViewOnTouchListener accessibility warning
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // set user credentials to authenticate with the feature service and webmap url
        // NOTE: a licensed user is required to perform utility network operations
        // NOTE: Never hardcode login information in a production application. This is done solely for the sake of the sample.
        val authenticationChallengeHandler =
            AuthenticationChallengeHandler {
                AuthenticationChallengeResponse(
                    AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
                    UserCredential("viewer01", "I68VGU^nMurF")
                )
            }
        AuthenticationManager.setAuthenticationChallengeHandler(authenticationChallengeHandler)

        // create a new map from the web map URL (includes ArcGIS Pro subtype group layers with only container features visible)
        val map =
            ArcGISMap("https://sampleserver7.arcgisonline.com/portal/home/item.html?id=813eda749a9444e4a9d833a4db19e1c8")
                .apply {
                    // add the utility network to the map's collection of utility networks, and load it
                    utilityNetworks.add(utilityNetwork)
                }

        // loads the features elements and the associations of the utility network
        utilityNetwork.apply {
            addDoneLoadingListener {
                // handle error if the utility network did not load
                if (utilityNetwork.loadStatus != LoadStatus.LOADED) {
                    onError("Error loading the utility network. Check URL used")
                }
            }
            loadAsync()
        }

        mapView.apply {
            // set the map to the MapView and set the MapView's viewpoint
            this.map = map
            setViewpoint(Viewpoint(41.801504, -88.163718, 4e3))
            // hide the progress indicator once the map view is done loading
            map.addDoneLoadingListener {
                if (map.loadStatus == LoadStatus.LOADED) {
                    progressBar.visibility = View.GONE
                } else if (map.loadStatus == LoadStatus.FAILED_TO_LOAD) {
                    progressBar.visibility = View.GONE
                    // handle error if map failed to load
                    map.loadError.message?.let { onError("Map failed to load: $it") }
                }
            }
            // add graphics overlay to display container view contents
            graphicsOverlays.add(graphicsOverlay)
        }

        // handle when map is clicked by retrieving the point
        mapView.onTouchListener =
            object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
                override fun onSingleTapConfirmed(event: MotionEvent?): Boolean {
                    if (event != null) {
                        // create a point from where the user clicked
                        val screenPoint = android.graphics.Point(event.x.toInt(), event.y.toInt())
                        // identify and handle the feature of the clicked at point
                        handleMapViewClicked(screenPoint)
                    }
                    return super.onSingleTapConfirmed(event)
                }
            }
        // set up the attachmentSymbol, connectivitySymbol and
        // the boundingBoxSymbol bitmaps for the legend view
        setUpLegendView()
        // sets the viewpoint, layers, and the graphics when user exits the container view
        exitButton.setOnClickListener {
            handleExitButtonClick()
        }
        // display the legend alert dialog when the button is clicked
        legendButton.setOnClickListener {
            legendDialog?.show()
        }
    }

    /**
     * Called when the user exits the container view.
     * Clears graphics, resets the viewpoint
     * and displays the operationalLayers
     */
    private fun handleExitButtonClick() {
        graphicsOverlay.graphics.clear()
        mapView.setViewpointAsync(previousViewpoint)
        mapView.map.operationalLayers.forEach { layer ->
            layer.isVisible = true
        }
        // exits from the container view
        handleContainerView(false)
    }

    /**
     * Display legend, exit button and disable interaction
     * when container view [isVisible]
     */
    private fun handleContainerView(isVisible: Boolean) {
        if (isVisible) {
            // enable buttons
            exitButton.isEnabled = true
            legendButton.isEnabled = true
            // disable map interactions
            mapView.interactionOptions.apply {
                isPanEnabled = false
                isZoomEnabled = false
                isRotateEnabled = false
            }
        } else {
            // disable button
            exitButton.isEnabled = false
            legendButton.isEnabled = false
            // enable map interactions
            mapView.interactionOptions.apply {
                isPanEnabled = true
                isZoomEnabled = true
                isRotateEnabled = true
            }
        }
    }

    /**
     * Set up the attachmentSymbol, connectivitySymbol and
     * the boundingBoxSymbol bitmaps for the legend view
     */
    private fun setUpLegendView() {
        // inflate the layout and get references to each of its components
        val dialogBinding = UtilityNetworkLegendBinding.inflate(LayoutInflater.from(this))
        // set the image bitmaps using the line symbols to the ImageViews
        dialogBinding.apply {
            attachmentImageView.setImageBitmap(
                attachmentSymbol.createSwatchAsync(0x00000000, 1F).get()
            )
            connectivityImageView.setImageBitmap(
                connectivitySymbol.createSwatchAsync(0x00000000, 1F).get()
            )
            boundingImageView.setImageBitmap(
                boundingBoxSymbol.createSwatchAsync(0x00000000, 1F).get()
            )
        }

        // create the alert dialog and bind it to the inflated layout
        legendDialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Legend")
            .create()
    }

    /**
     * Identifies the feature of the clicked [mapPoint], gets the selected feature's sublayerResults
     * and retrieves the utility network associations of the selected ArcGISFeature
     */
    private fun handleMapViewClicked(mapPoint: android.graphics.Point) {
        try {
            // identify the feature clicked on
            val identifyLayerResultsFuture = mapView.identifyLayersAsync(mapPoint, 10.0, false)
            identifyLayerResultsFuture.addDoneListener {
                // get the result of the query
                val identifyLayerResults = identifyLayerResultsFuture.get()
                // if no layer identified then return, else display progressBar
                if (identifyLayerResults.isEmpty()) return@addDoneListener
                progressBar.visibility = View.VISIBLE
                // finds the first layer where the LayerContent is a SubtypeFeatureLayer
                val layerResult =
                    identifyLayerResults.find { layerResult -> layerResult.layerContent is SubtypeFeatureLayer }
                    // user clicked on an empty space on map with no feature
                        ?: return@addDoneListener
                // filter the sublayer result's elements to find the first one which is an ArcGIS feature
                val selectedContainerFeature =
                    layerResult.sublayerResults.first().elements.filterIsInstance<ArcGISFeature>()
                        .first()
                // create a container element using the selected feature
                val containerElement = utilityNetwork.createElement(selectedContainerFeature)
                // get the containment associations from this element to display its content
                val containmentAssociationsFuture = utilityNetwork.getAssociationsAsync(
                    containerElement,
                    UtilityAssociationType.CONTAINMENT
                )
                containmentAssociationsFuture.addDoneListener {
                    // displays the features, symbols and the container element's associations
                    handleContainmentAssociations(
                        containmentAssociationsFuture.get(),
                        containerElement
                    )
                }
            }
        } catch (e: Exception) {
            onError("Error getting result: ${e.message}")
        }
    }

    /**
     * Displays the features, symbols and the container element's associations
     */
    private fun handleContainmentAssociations(
        containmentAssociations: List<UtilityAssociation>,
        containerElement: UtilityElement
    ) {
        // get and store a list of elements from the result of the query
        val contentElements: MutableList<UtilityElement> = mutableListOf()
        // get the list of containment associations and loop through them to get their elements
        containmentAssociations.forEach { association ->
            val utilityElement =
                if (association.fromElement.objectId == containerElement.objectId) association.toElement
                else association.fromElement
            contentElements.add(utilityElement)
        }

        // save the viewpoint and hide the current operationalLayers
        previousViewpoint = mapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY)
        mapView.map.operationalLayers.forEach { layer -> layer.isVisible = false }

        // fetch the features from the elements
        val fetchFeaturesFuture = utilityNetwork.fetchFeaturesForElementsAsync(contentElements)
        fetchFeaturesFuture.addDoneListener {
            // get the content features and give them each a symbol, and add them as a graphic to the graphics overlay
            fetchFeaturesFuture.get().forEach { content ->
                val symbol: Symbol =
                    content.featureTable.layerInfo.drawingInfo.renderer.getSymbol(content)
                graphicsOverlay.graphics.add(Graphic(content.geometry, symbol))
            }
            val firstGraphic = graphicsOverlay.graphics[0].geometry
            val containerViewScale = containerElement.assetType.containerViewScale
            if (graphicsOverlay.graphics.size == 1 && firstGraphic is Point) {
                mapView.setViewpointCenterAsync(firstGraphic, containerViewScale)
                    .addDoneListener {
                        // the bounding box, which defines the container view, may be computed using the extent of the features
                        // it contains or centered around its geometry at the container's view scale
                        val boundingBox =
                            mapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).targetGeometry
                        // identifies and displays associations for the specified geometry
                        identifyAssociationsWithExtent(boundingBox)
                        // since size is 1, no associations found
                        Toast.makeText(this, "This feature has no associations", Toast.LENGTH_SHORT)
                            .show()
                    }
            } else {
                // associations found, create a bounding box around them
                val boundingBox: Geometry =
                    GeometryEngine.buffer(graphicsOverlay.extent, 0.05)
                // get associations for the specified geometry and display its associations
                identifyAssociationsWithExtent(boundingBox)
            }
        }
    }

    /**
     * Get associations for the specified geometry and display its associations
     * using [boundingBox] the geometry from which to get associations.
     */
    private fun identifyAssociationsWithExtent(boundingBox: Geometry?) {
        // display container view and hide progressBar
        handleContainerView(true)
        progressBar.visibility = View.GONE
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
                    // add the symbol for each association in the container
                    graphicsOverlay.graphics.add(Graphic(association.geometry, symbol))
                }
            } catch (e: Exception) {
                onError("Error getting extent associations")
            }
        }
    }

    private fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, message)
        progressBar.visibility = View.GONE
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
