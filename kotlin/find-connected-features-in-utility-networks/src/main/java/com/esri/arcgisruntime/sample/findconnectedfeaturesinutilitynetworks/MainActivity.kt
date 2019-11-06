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
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.internal.jni.it
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
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val graphicsOverlay: GraphicsOverlay = GraphicsOverlay()
  private val utilityNetwork: UtilityNetwork by lazy {
    UtilityNetwork(getString(R.string.naperville_utility_network_service), mapView.map)
  }

  // create lists for starting locations and barriers
  private val startingLocations: MutableList<UtilityElement> by lazy { ArrayList<UtilityElement>() }
  private val barriers: MutableList<UtilityElement> by lazy { ArrayList<UtilityElement>() }

  // create symbols for the starting point and barriers
  private val mStartingPointSymbol: SimpleMarkerSymbol by lazy {
    SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.GREEN, 20f)
  }
  private val mBarrierPointSymbol: SimpleMarkerSymbol by lazy {
    SimpleMarkerSymbol(SimpleMarkerSymbol.Style.X, Color.RED, 20f)
  }
  private var mUtilityTraceParameters: UtilityTraceParameters? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // setup the map view
    mapView.let {
      // add a map with streets night vector basemap
      it.map = ArcGISMap(Basemap.createStreetsNightVector()).apply {
        operationalLayers.apply {
          // create electrical distribution line layer
          add(FeatureLayer(ServiceFeatureTable(
              getString(R.string.naperville_utility_network_service) + "/115")).apply {
            // create and apply a renderer solid light gray renderer
            renderer =
                SimpleRenderer(SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.LTGRAY, 2f))
          })
          // create electrical device layer
          add(FeatureLayer(
              ServiceFeatureTable(getString(R.string.naperville_utility_network_service) + "/100")))
        }
        // set the viewpoint to a section in the southeast of the network
        it.setViewpointAsync(Viewpoint(
            Envelope(-9813547.35557238, 5129980.36635111, -9813185.0602376, 5130215.41254146,
                SpatialReferences.getWebMercator())))
        // add a graphics overlay
        it.graphicsOverlays.add(graphicsOverlay)
        // handle taps on the map view
        it.onTouchListener = object : DefaultMapViewOnTouchListener(applicationContext, mapView) {
          override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            // only pass taps to identify nearest utility element once the utility network has loaded
            if (utilityNetwork.loadStatus == LoadStatus.LOADED) {
              identifyNearestUtilityElement(
                  android.graphics.Point(e!!.x.roundToInt(), e.y.roundToInt()))
            }
            return super.onSingleTapConfirmed(e)
          }
        }
      }
    }

    // load the utility network
    utilityNetwork.loadAsync()
    utilityNetwork.addDoneLoadingListener {
      if (utilityNetwork.loadStatus == LoadStatus.LOADED) {
        // update the status text
        statusTextView.text = "Click on the network lines or points to add a utility element."
      } else {
        reportError("Error loading utility network: " + utilityNetwork.loadError.cause?.message)
      }
    }
  }

  /**
   * Uses the clicked map point to identify any utility elements in the utility network at the clicked location. Based
   * on the selection mode, the clicked utility element is added either to the starting locations or barriers for the
   * trace parameters. An appropriate graphic is created at the clicked location to mark the element as either a
   * starting location or barrier.
   */
  private fun identifyNearestUtilityElement(screenPoint: android.graphics.Point) {
    // get the clicked map point
    val mapPoint = mapView.screenToLocation(screenPoint)
    // identify the feature to be used
    mapView.identifyLayersAsync(screenPoint, 10.0, false).let { identifyLayerResultFuture ->
      // get the result of the query
      val identifyLayerResults = identifyLayerResultFuture.get()
      // retrieve the geoelement as an ArcGISFeature
      (identifyLayerResults[0].elements[0] as? ArcGISFeature)?.let { arcGISFeature ->
        // get the network source of the identified feature
        (utilityNetwork.definition.getNetworkSource(
            arcGISFeature.featureTable.tableName)).let { utilityNetworkSource ->
          var utilityElement: UtilityElement? = null
          // check if the network source is a junction or an edge
          if (utilityNetworkSource.sourceType == UtilityNetworkSource.Type.JUNCTION) {
            //  create a utility element with the identified feature
            utilityElement = createUtilityElement(arcGISFeature, utilityNetworkSource)
          } else if (utilityNetworkSource.sourceType == UtilityNetworkSource.Type.EDGE) {
            // get the geometry of the identified feature as a polyline, and remove the z component
            //  create a utility element with the identified feature
            utilityElement = utilityNetwork.createElement(arcGISFeature, null).apply {
              // calculate how far the clicked location is along the edge feature
              fractionAlongEdge = GeometryEngine.fractionAlong(
                  GeometryEngine.removeZ(arcGISFeature.geometry) as Polyline, mapPoint, -1.0)
            }.also {
              // update the status label text
              statusTextView.text = getString(R.string.fraction_message, it.fractionAlongEdge)
            }
          }
          if (utilityElement != null) {
            // set the trace location graphic to the nearest coordinate to the tapped point
            graphicsOverlay.graphics.add(Graphic(
                GeometryEngine.nearestCoordinate(arcGISFeature.geometry,
                    mapPoint).coordinate).apply {
              // add the element to the appropriate list, and add the appropriate symbol to the graphic
              symbol = if (startingLocationsRadioButton.isChecked) {
                startingLocations.add(utilityElement)
                mStartingPointSymbol
              } else {
                barriers.add(utilityElement)
                mBarrierPointSymbol
              }
            })
          }
        }
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

    var utilityElement: UtilityElement

    // get the attributes of the identified feature
    val attributes = identifiedFeature.attributes

    // get the name of the utility asset group's attribute field from the feature
    val assetGroupFieldName = identifiedFeature.featureTable.subtypeField

    // find the code matching the asset group name in the feature's attributes
    val assetGroupCode = attributes[assetGroupFieldName.toLowerCase()] as Int

    // iterate through the network source's asset groups to find the group with the matching code
    with(networkSource.assetGroups) {
      for (assetGroup in this) {
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
              val utilityElement = when {
                terminals.size == 1 -> {
                  utilityNetwork.createElement(identifiedFeature, terminals[0])
                  // show the name of the terminal in the status label
                  showTerminalNameInStatusLabel(terminals[0])
                }
                // if there is more than one terminal, prompt the user to select one
                terminals.size > 1 -> {
                  val terminalNames = ArrayList<String>()
                  assetType.terminalConfiguration.terminals.mapTo(terminalNames) { it.name }
                  UtilityTerminalSelectionDialog.newInstance(terminalNames)

                  // show the terminal selection dialog and capture the user selection
                  val selectedTerminalOptional = utilityTerminalSelectionDialog.showAndWait()

                  // use the selected terminal
                  if (selectedTerminalOptional.isPresent()) {
                    UtilityTerminal selectedTerminal = selectedTerminalOptional.get()
                    utilityElement =
                        utilityNetwork.createElement(identifiedFeature, selectedTerminal)
                    showTerminalNameInStatusLabel(selectedTerminal);
                  }
                }
                else ->
              }
            }
          }
        }
      }
    }
    return utilityElement

  }

  suspend fun

  /**
   * Shows the name of a UtilityTerminal in the status label in the UI.
   *
   * @param terminal
   */
  private fun showTerminalNameInStatusLabel(terminal: UtilityTerminal) {
    val terminalName = if (terminal.name != "") terminal.name else "default"
    statusTextView.text = "Terminal: $terminalName"
  }

  /**
   * Uses the elements selected as starting locations and (optionally) barriers to perform a connected trace,
   * then selects all connected elements found in the trace to highlight them.
   */
  fun traceUtilityNetwork(view: View) {

    // check that the utility trace parameters are valid
    if (startingLocations.isEmpty()) {
      reportError("No starting locations provided for trace.")
      return
    }

    // show the progress indicator and update the status text
    progressIndicator.visibility = View.VISIBLE
    statusTextView.text = getString(R.string.find_connected_features_message)

    // disable buttons
    resetButton.isEnabled = false
    traceButton.isEnabled = false

    // create utility trace parameters for a connected trace
    mUtilityTraceParameters = UtilityTraceParameters(UtilityTraceType.CONNECTED, startingLocations)

    // if any barriers have been created, add them to the parameters
    if (barriers.isNotEmpty()) {
      mUtilityTraceParameters!!.barriers.addAll(barriers)
    }

    // run the utility trace and get the results
    val utilityTraceResultsFuture = utilityNetwork
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
                utilityElementGroups[networkSourceName]?.add(utilityElement)
              }
            }

            // get the feature layer for the utility element
            for ((networkSourceName, utilityElements) in utilityElementGroups) {
              // get the layer for the utility element
              val layer = mapView!!.map.operationalLayers[0] as FeatureLayer

              if (layer.featureTable.tableName == networkSourceName) {
                // convert the elements to features to highlight the result
                val fetchUtilityFeaturesFuture =
                    utilityNetwork.fetchFeaturesForElementsAsync(utilityElements)
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
                    enableButtons()
                  } catch (e: InterruptedException) {
                    reportError(
                        "Error fetching corresponding features for utility elements: " + e.message)
                  } catch (e: ExecutionException) {
                    reportError(
                        "Error fetching corresponding features for utility elements: " + e.message)
                  }
                }
              }
            }
          }
        } else {
          statusTextView.text = getString(R.string.failed_message)
          reportError("Trace result not a utility element.")
        }
        // enable the UI
        enableButtons()
      } catch (e: InterruptedException) {
        statusTextView.text = getString(R.string.failed_message)
        reportError("Error running utility network connected trace.")
      } catch (e: ExecutionException) {
        statusTextView.text = getString(R.string.failed_message)
        reportError("Error running utility network connected trace.")
      }
    }
  }

  /**
   * Enables both buttons and hides the progress indicator after a trace is complete.
   */
  private fun enableButtons() {
    // enable the UI
    resetButton.isEnabled = true
    traceButton.isEnabled = true
    // hide the progress indicator
    progressIndicator.visibility = View.GONE
  }

  /**
   * Restores the sample to the startup-state by resetting the status text, hiding the progress indicator, clearing
   * the trace parameters, de-selecting all features and removing any graphics
   */
  fun reset(view: View) {
    statusTextView.text = "Click on the network lines or points to add a utility element."
    progressIndicator.visibility = View.GONE

    // clear the utility trace parameters
    startingLocations.clear()
    barriers.clear()
    mUtilityTraceParameters = null
    // clear any selected features in all map layers
    for (layer in mapView!!.map.operationalLayers) {
      if (layer is FeatureLayer) {
        layer.clearSelection()
      }
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
    alert(error)
    error(error)
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
