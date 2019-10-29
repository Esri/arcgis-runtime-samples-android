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

package com.esri.arcgisruntime.sample.findconnectedfeaturesinutilitynetworks

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.utilitynetworks.UtilityElement
import com.esri.arcgisruntime.utilitynetworks.UtilityElementTraceResult
import com.esri.arcgisruntime.utilitynetworks.UtilityNetwork
import com.esri.arcgisruntime.utilitynetworks.UtilityNetworkSource
import com.esri.arcgisruntime.utilitynetworks.UtilityTerminal
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceParameters
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceType
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.utility_network_controls_layout.*
import org.jetbrains.anko.alert
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val TAG: String? = MainActivity::class.simpleName

  private lateinit var mGraphicsOverlay: GraphicsOverlay
  private lateinit var mUtilityNetwork: UtilityNetwork
  private lateinit var mStartingLocations: ArrayList<UtilityElement>
  private lateinit var mBarriers: ArrayList<UtilityElement>
  private lateinit var mStartingPointSymbol: SimpleMarkerSymbol
  private lateinit var mBarrierPointSymbol: SimpleMarkerSymbol
  private lateinit var mUtilityTraceParameters: UtilityTraceParameters
  private lateinit var mDistributionLineFeatureService: ServiceFeatureTable
  private lateinit var mElectricDeviceFeatureService: ServiceFeatureTable

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a basemap and set it to the map view
    val map = ArcGISMap(Basemap.createStreetsNightVector())
    mapView.map = map
    mapView.setViewpointAsync(Viewpoint(
        Envelope(-9813547.35557238, 5129980.36635111, -9813185.0602376, 5130215.41254146,
            SpatialReferences.getWebMercator())))

    // create symbols for the starting point and barriers
    mStartingPointSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.GREEN, 20f)
    mBarrierPointSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.X, Color.RED, 20f)

    // load the utility network data from the feature service and create feature layers
    mDistributionLineFeatureService =
        ServiceFeatureTable(getString(R.string.naperville_utility_network_service) + "/115")
    val distributionLineLayer = FeatureLayer(mDistributionLineFeatureService!!)

    mElectricDeviceFeatureService =
        ServiceFeatureTable(getString(R.string.naperville_utility_network_service) + "/100")
    val electricDeviceLayer = FeatureLayer(mElectricDeviceFeatureService!!)

    // create and apply a renderer for the electric distribution lines feature layer
    distributionLineLayer.renderer =
        SimpleRenderer(SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.LTGRAY, 2f))

    // add the feature layers to the map
    map.operationalLayers.addAll(Arrays.asList(distributionLineLayer, electricDeviceLayer))

    // create a graphics overlay and add it to the map view
    mGraphicsOverlay = GraphicsOverlay()
    mapView.graphicsOverlays.add(mGraphicsOverlay)

    // create a list of starting locations for the trace
    mStartingLocations = ArrayList()
    mBarriers = ArrayList()

    // create and load the utility network
    mUtilityNetwork = UtilityNetwork(getString(R.string.naperville_utility_network_service), map)
    mUtilityNetwork.loadAsync()
    mUtilityNetwork.addDoneLoadingListener {
      if (mUtilityNetwork.loadStatus == LoadStatus.LOADED) {

        // hide the progress indicator
        //TODO - progressIndicator.setVisible(false);

        // update the status text
        statusTextView.text = "Click on the network lines or points to add a utility element."

        // listen to clicks on the map view
        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {

          override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            handleMapViewClicked(android.graphics.Point(e!!.x.roundToInt(), e.y.roundToInt()))
            return super.onSingleTapConfirmed(e)
          }
        }
      } else {
        val error =
            "Error loading utility network: " + mUtilityNetwork!!.loadError.cause.message
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
      }
    }

    // get references to buttons
    resetButton.setOnClickListener { v -> reset() }
    traceButton.setOnClickListener { v -> traceUtilityNetwork() }
  }

  /**
   * Uses the clicked map point to identify any utility elements in the utility network at the clicked location. Based
   * on the selection mode, the clicked utility element is added either to the starting locations or barriers for the
   * trace parameters. An appropriate graphic is created at the clicked location to mark the element as either a
   * starting location or barrier.
   */
  private fun handleMapViewClicked(screenPoint: android.graphics.Point) {

    // show the progress indicator
    //TODO - progressIndicator.setVisible(true);

    // get the clicked map point
    val mapPoint = mapView!!.screenToLocation(screenPoint)

    // identify the feature to be used
    val identifyLayerResultsFuture = mapView!!
        .identifyLayersAsync(screenPoint, 10.0, false)
    identifyLayerResultsFuture.addDoneListener {
      try {
        // get the result of the query
        val identifyLayerResults = identifyLayerResultsFuture.get()

        // return if no features are identified
        if (identifyLayerResults.isEmpty()) {
          return@addDoneListener
        } else {
          // retrieve the first result and get it's contents
          val firstResult = identifyLayerResults[0]
          val layerContent = firstResult.layerContent
          // check that the result is a feature layer and has elements
          if (layerContent is FeatureLayer && !firstResult.elements.isEmpty()) {
            // retrieve the geoelements in the feature layer
            val identifiedElement = firstResult.elements[0]
            if (identifiedElement is ArcGISFeature) {
              // get the feature

              // get the network source of the identified feature
              val networkSource = mUtilityNetwork!!
                  .definition
                  .getNetworkSource(identifiedElement.featureTable.tableName)

              var utilityElement: UtilityElement? = null

              // check if the network source is a junction or an edge
              if (networkSource.sourceType == UtilityNetworkSource.Type.JUNCTION) {
                //  create a utility element with the identified feature
                utilityElement = createUtilityElement(identifiedElement, networkSource)
              } else if (networkSource.sourceType == UtilityNetworkSource.Type.EDGE && identifiedElement.geometry.geometryType == GeometryType.POLYLINE) {

                //  create a utility element with the identified feature
                utilityElement = mUtilityNetwork!!.createElement(identifiedElement, null)

                // get the geometry of the identified feature as a polyline, and remove the z component
                val polyline = GeometryEngine.removeZ(identifiedElement.geometry) as Polyline

                // compute how far the clicked location is along the edge feature
                utilityElement!!.fractionAlongEdge =
                    GeometryEngine.fractionAlong(polyline, mapPoint, -1.0)

                // update the status label text
                statusTextView.text = "Fraction along edge: " + utilityElement.fractionAlongEdge
              }// check if the network source is an edge

              if (utilityElement != null) {
                // create a graphic for the new utility element
                val traceLocationGraphic = Graphic()

                // find the closest coordinate on the selected element to the clicked point
                val proximityResult = GeometryEngine
                    .nearestCoordinate(identifiedElement.geometry, mapPoint)

                // set the graphic's geometry to the coordinate on the element
                traceLocationGraphic.geometry = proximityResult.coordinate
                mGraphicsOverlay.graphics.add(traceLocationGraphic)

                // add the element to the appropriate list, and add the appropriate symbol to the graphic
                if (startingLocationsRadioButton!!.isChecked) {
                  mStartingLocations.add(utilityElement)
                  traceLocationGraphic.symbol = mStartingPointSymbol
                } else {
                  mBarriers.add(utilityElement)
                  traceLocationGraphic.symbol = mBarrierPointSymbol
                }
              }
            }
          }
        }
      } catch (ex: InterruptedException) {
        val error = "Error identifying tapped features: " + ex.message
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
      } catch (ex: ExecutionException) {
        val error = "Error identifying tapped features: " + ex.message
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
      } finally {
        //TODO - progressIndicator.setVisible(false);
      }
    }
  }

  /**
   * Uses a UtilityNetworkSource to create a UtilityElement object out of an ArcGISFeature.
   *
   * @param identifiedFeature an ArcGISFeature object that will be used to create a UtilityElement.
   * @param networkSource     the UtilityNetworkSource to which the created UtilityElement is associated.
   * @return the created UtilityElement.
   */
  private fun createUtilityElement(identifiedFeature: ArcGISFeature,
                                   networkSource: UtilityNetworkSource): UtilityElement? {
    var utilityElement: UtilityElement? = null

    // get the attributes of the identified feature
    val attributes = identifiedFeature.attributes

    // get the name of the utility asset group's attribute field from the feature
    val assetGroupFieldName = identifiedFeature.featureTable.subtypeField

    // find the code matching the asset group name in the feature's attributes
    val assetGroupCode = attributes[assetGroupFieldName.toLowerCase()] as Int

    // iterate through the network source's asset groups to find the group with the matching code
    val assetGroups = networkSource.assetGroups
    for (assetGroup in assetGroups) {
      if (assetGroup.code == assetGroupCode) {

        // get the code for the feature's asset type from it's attributes
        val assetTypeCode = attributes["assettype"]!!.toString()

        // iterate through the asset group's asset types to find the type matching the feature's asset type code
        val utilityAssetTypes = assetGroup.assetTypes
        for (assetType in utilityAssetTypes) {
          if (assetType.code == Integer.parseInt(assetTypeCode)) {

            // get the list of terminals for the feature
            val terminals = assetType.terminalConfiguration.terminals

            // if there is only one terminal, use it to create a utility element
            if (terminals.size == 1) {
              utilityElement = mUtilityNetwork!!.createElement(identifiedFeature, terminals[0])
              // show the name of the terminal in the status label
              showTerminalNameInStatusLabel(terminals[0])

              // if there is more than one terminal, prompt the user to select one
            } else if (terminals.size > 1) {
              // create a dialog for terminal selection
              /*
              UtilityTerminalSelectionDialog utilityTerminalSelectionDialog = new UtilityTerminalSelectionDialog(terminals);

              // show the terminal selection dialog and capture the user selection
              Optional<UtilityTerminal> selectedTerminalOptional = utilityTerminalSelectionDialog.showAndWait();

              // use the selected terminal
              if (selectedTerminalOptional.isPresent()) {
                UtilityTerminal selectedTerminal = selectedTerminalOptional.get();
                utilityElement = mUtilityNetwork.createElement(identifiedFeature, selectedTerminal);
                showTerminalNameInStatusLabel(selectedTerminal);
              }*/
            }
          }
        }
      }
    }

    return utilityElement
  }

  /**
   * Shows the name of a UtilityTerminal in the status label in the UI.
   *
   * @param terminal
   */
  private fun showTerminalNameInStatusLabel(terminal: UtilityTerminal) {
    val terminalName = if (terminal.name != null) terminal.name else "default"
    statusTextView.text = "Terminal: $terminalName"
  }

  /**
   * Uses the elements selected as starting locations and (optionally) barriers to perform a connected trace,
   * then selects all connected elements found in the trace to highlight them.
   */
  private fun traceUtilityNetwork() {

    // check that the utility trace parameters are valid
    if (mStartingLocations!!.isEmpty()) {
      reportError("No starting locations provided for trace.")
      return
    }

    // show the progress indicator and update the status text
    //progressIndicator.setVisible(true);
    statusTextView.text = "Finding connected features..."

    // disable the UI
    resetButton.isEnabled = false
    traceButton.isEnabled = false

    // create utility trace parameters for a connected trace
    mUtilityTraceParameters =
        UtilityTraceParameters(UtilityTraceType.CONNECTED, mStartingLocations!!)

    // if any barriers have been created, add them to the parameters
    if (!mBarriers!!.isEmpty()) {
      mUtilityTraceParameters!!.barriers.addAll(mBarriers!!)
    }

    // run the utility trace and get the results
    val utilityTraceResultsFuture = mUtilityNetwork!!
        .traceAsync(mUtilityTraceParameters!!)
    utilityTraceResultsFuture.addDoneListener {
      try {
        val utilityTraceResults = utilityTraceResultsFuture.get()

        if (utilityTraceResults[0] is UtilityElementTraceResult) {
          val utilityElementTraceResult = utilityTraceResults[0] as UtilityElementTraceResult

          if (!utilityElementTraceResult.elements.isEmpty()) {
            // clear the previous selection from the layer
            for (layer in mapView!!.map.operationalLayers) {
              if (layer is FeatureLayer) {
                layer.clearSelection()
              }
            }

            // group the utility elements by their network source
            val utilityElementGroups = HashMap<String, MutableList<UtilityElement>>()
            for (utilityElement in utilityElementTraceResult.elements) {
              val networkSourceName = utilityElement.networkSource.name
              if (!utilityElementGroups.containsKey(utilityElement.networkSource.name)) {
                val list = ArrayList<UtilityElement>()
                list.add(utilityElement)
                utilityElementGroups[networkSourceName] = list
              } else {
                utilityElementGroups[networkSourceName].add(utilityElement)
              }
            }

            // get the feature layer for the utility element
            for ((networkSourceName, utilityElements) in utilityElementGroups) {
              // get the layer for the utility element
              val layer = mapView!!.map.operationalLayers[0] as FeatureLayer

              if (layer.featureTable.tableName == networkSourceName) {
                mDistributionLineFeatureService.addDoneLoadingListener {
                  mElectricDeviceFeatureService.addDoneLoadingListener {
                    // convert the elements to features to highlight the result
                    val fetchUtilityFeaturesFuture = mUtilityNetwork.fetchFeaturesForElementsAsync(utilityElements)
                    fetchUtilityFeaturesFuture.addDoneListener {
                      try {
                        val features = fetchUtilityFeaturesFuture.get()
                        // select all the features to highlight them
                        for (feature in features) {
                          layer.selectFeature(feature)
                        }
                        // update the status label
                        statusTextView.text = "Trace completed."
                        // enable the UI
                        enableUI()
                      } catch (e: InterruptedException) {
                        reportError("Error fetching corresponding features for utility elements: " + e.message)
                      } catch (e: ExecutionException) {
                        reportError("Error fetching corresponding features for utility elements: " + e.message)
                      }
                    }
                  }
                }
              }
            }
          }
        } else {
          statusTextView.text = "Trace failed."
          reportError("Trace result not a utility element.")
        }
        // enable the UI
        enableUI()
      } catch (e: InterruptedException) {
        statusTextView.text = "Trace failed."
        reportError("Error running utility network connected trace.")
      } catch (e: ExecutionException) {
        statusTextView.text = "Trace failed."
        reportError("Error running utility network connected trace.")
      }
    }
  }

  /**
   * Enables both buttons and hides the progress indicator after a trace is complete.
   */
  private fun enableUI() {

    // enable the UI
    resetButton.isEnabled = true
    traceButton.isEnabled = true

    // hide the progress indicator
    //progressIndicator.setVisible(false);
  }

  /**
   * Restores the sample to the startup-state by resetting the status text, hiding the progress indicator, clearing
   * the trace parameters, de-selecting all features and removing any graphics
   */
  private fun reset() {
    statusTextView.text = "Click on the network lines or points to add a utility element."
    //progressIndicator.setVisible(false);

    // clear the utility trace parameters
    mStartingLocations.clear()
    mBarriers.clear()
    mUtilityTraceParameters = null
    // clear any selected features in all map layers
    for (layer in mapView!!.map.operationalLayers) {
      if (layer is FeatureLayer) {
        layer.clearSelection()
      }
    }
    // clear the graphics overlay
    mGraphicsOverlay.graphics.clear()
    // enable the trace button
    traceButton.isEnabled = true
  }

  /**
   * Report the given error to the user by toast and log.
   *
   * @param error as a string
   */
  private fun reportError(error: String) {
    alert(error)
    Log.e(TAG, error)
  }

  override fun onPause() {
    mapView!!.pause()
    super.onPause()
  }

  override fun onResume() {
    super.onResume()
    mapView!!.resume()
  }

  override fun onDestroy() {
    mapView!!.dispose()
    super.onDestroy()
  }
}
