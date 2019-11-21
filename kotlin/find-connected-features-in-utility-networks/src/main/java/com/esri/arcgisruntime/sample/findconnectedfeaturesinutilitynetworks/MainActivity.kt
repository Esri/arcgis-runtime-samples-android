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
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val graphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // setup the map view
    mapView.apply {

      // add a map with streets night vector basemap
      map = ArcGISMap(Basemap.createStreetsNightVector()).apply {
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
      }

      // set the viewpoint to a section in the southeast of the network
      setViewpointAsync(Viewpoint(
          Envelope(-9813547.35557238, 5129980.36635111, -9813185.0602376, 5130215.41254146,
              SpatialReferences.getWebMercator())))

      // add a graphics overlay
      graphicsOverlays.add(graphicsOverlay)

      // handle taps on the map view
      onTouchListener = object : DefaultMapViewOnTouchListener(applicationContext, mapView) {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
          // only pass taps to identify nearest utility element once the utility network has loaded
          if (utilityNetwork.loadStatus == LoadStatus.LOADED) {
            identifyNearestUtilityElement(
                android.graphics.Point(e.x.roundToInt(), e.y.roundToInt()))
          }
          return super.onSingleTapConfirmed(e)
        }
      }
    }

    // load the utility network
    utilityNetwork.addDoneLoadingListener {
      if (utilityNetwork.loadStatus == LoadStatus.LOADED) {
        // update the status text
        statusTextView.text = getString(R.string.click_to_add_points)
      } else {
        reportError("Error loading utility network: " + utilityNetwork.loadError.cause?.message)
      }
    }
    utilityNetwork.loadAsync()
  }

  /**
   * Uses the tapped point to identify any utility elements in the utility network at the tapped
   * location. Based on the selection mode, the tapped utility element is added either to the
   * starting locations or barriers for the trace parameters. An appropriate graphic is created at
   * the tapped location to mark the element as either a starting location or barrier.
   */
  private fun identifyNearestUtilityElement(screenPoint: android.graphics.Point) {
    // get the clicked map point
    val mapPoint = mapView.screenToLocation(screenPoint)
    // identify the feature to be used
    val identifyLayerResultFuture = mapView.identifyLayersAsync(screenPoint, 10.0, false)
    identifyLayerResultFuture.addDoneListener {
      // get the result of the query
      val identifyResults = identifyLayerResultFuture.get()
      // if the identify returns a result, retrieve the geoelement as an ArcGISFeature
      (identifyResults.getOrNull(0)?.elements?.get(0) as? ArcGISFeature)?.let { identifiedFeature ->
        // get the network source of the identified feature
        (utilityNetwork.definition.getNetworkSource(
            identifiedFeature.featureTable.tableName)).let { utilityNetworkSource ->
          // check if the network source is a junction or an edge
          if (utilityNetworkSource.sourceType == UtilityNetworkSource.Type.JUNCTION) {
            GlobalScope.launch {
              // create a utility element with the identified feature
              createUtilityElement(identifiedFeature, utilityNetworkSource)?.let { utilityElement ->
                // add the utility element to the map
                addUtilityElementToMap(identifiedFeature, mapPoint, utilityElement)
                // show the utility element name in the UI
                showTerminalNameInStatusLabel(utilityElement.terminal)
              }
            }
          } else {
            //  create a utility element with the identified feature
            val utilityElement = utilityNetwork.createElement(identifiedFeature, null).apply {
              // calculate how far the clicked location is along the edge feature
              this.fractionAlongEdge = GeometryEngine.fractionAlong(
                  GeometryEngine.removeZ(identifiedFeature.geometry) as Polyline, mapPoint, -1.0)
            }.also {
              // update the status label text
              statusTextView.text = getString(R.string.fraction_message, it.fractionAlongEdge)
            }
            // set the trace location graphic to the nearest coordinate to the tapped point
            addUtilityElementToMap(identifiedFeature, mapPoint, utilityElement)
          }
        }
      }
    }
  }

  private fun addUtilityElementToMap(identifiedFeature: ArcGISFeature, mapPoint: Point?,
                                     utilityElement: UtilityElement) {
    Graphic(
        GeometryEngine.nearestCoordinate(identifiedFeature.geometry, mapPoint).coordinate).apply {
      // add the element to the appropriate list (starting locations or barriers), and add the
      // appropriate symbol to the graphic
      symbol = if (startingLocationsRadioButton.isChecked) {
        startingLocations.add(utilityElement)
        mStartingPointSymbol
      } else {
        barriers.add(utilityElement)
        mBarrierPointSymbol
      }
    }.apply {
      graphicsOverlay.graphics.add(this)
    }
  }

  /**
   * Uses a UtilityNetworkSource to create a UtilityElement object out of an ArcGISFeature.
   *
   * @param identifiedFeature an ArcGISFeature object that will be used to create a UtilityElement
   * @param networkSource     the UtilityNetworkSource to which the created UtilityElement is associated
   * @return the created UtilityElement
   */
  private suspend fun createUtilityElement(identifiedFeature: ArcGISFeature,
                                           networkSource: UtilityNetworkSource): UtilityElement? {
    // find the code matching the asset group name in the feature's attributes
    val assetGroupCode =
        identifiedFeature.attributes[identifiedFeature.featureTable.subtypeField.toLowerCase()] as Int

    // find the network source's asset group with the matching code
    return networkSource.assetGroups.filter { it.code == assetGroupCode }[0].assetTypes
        // find the asset group type code matching the feature's asset type code
        .filter { it.code == identifiedFeature.attributes["assettype"].toString().toInt() }[0]
        .let { utilityAssetType ->
          // get the list of terminals for the feature
          val terminals = utilityAssetType.terminalConfiguration.terminals
          // if there is only one terminal, use it to create a utility element
          when (terminals.size) {
            1 -> {
              utilityNetwork.createElement(identifiedFeature, terminals[0])
            }
            // if there is more than one terminal, prompt the user to select one
            else -> {
              // get a list of terminal names from the terminals
              val terminalNames = ArrayList<String>()
              utilityAssetType.terminalConfiguration.terminals.mapTo(terminalNames) { it.name }
              // pass the terminal names to a dialog
              val utilityTerminalSelectionDialog =
                  UtilityTerminalSelectionDialog.newInstance(terminalNames)
              utilityTerminalSelectionDialog.show(supportFragmentManager, "terminal_fragment")
              // use a coroutine to set utility element after user has picked a terminal
              suspendCoroutine<UtilityElement> { cont ->
                utilityTerminalSelectionDialog.setOnClickListener(object :
                    UtilityTerminalSelectionDialog.OnButtonClickedListener {
                  override fun onContinueClicked(terminalIndex: Int) {
                    cont.resume(
                        utilityNetwork.createElement(identifiedFeature, terminals[terminalIndex]))
                  }
                })
              }
            }
          }
        }
  }

  /**
   * Shows the name of a UtilityTerminal in the status label in the UI.
   *
   * @param terminal
   */
  private fun showTerminalNameInStatusLabel(terminal: UtilityTerminal) {
    statusTextView.text =
        getString(R.string.terminal_name, if (terminal.name != "") terminal.name else "default")
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
    disableButtons()


    // create utility trace parameters for a connected trace
    with(UtilityTraceParameters(UtilityTraceType.CONNECTED, startingLocations)) {
      // if any barriers have been created, add them to the parameters
      this.barriers.addAll(barriers)
      // run the utility trace and get the results
      val utilityTraceResultsFuture = utilityNetwork.traceAsync(this)
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
                      featureLayer.selectFeaturesAsync(this, FeatureLayer.SelectionMode.NEW).addDoneListener {
                        // when done, update status text, enable buttons and hide progress indicator
                        statusTextView.text = getString(R.string.trace_completed)
                        enableButtons()
                        progressIndicator.visibility = View.GONE
                      }
                    }
                  }
            }
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
  }

  /**
   * Enables both buttons and hides the progress indicator after a trace is complete.
   */
  private fun enableButtons() {
    // enable the UI
    resetButton.isEnabled = true
    traceButton.isEnabled = true
  }

  /**
   * Enables both buttons and hides the progress indicator after a trace is complete.
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
    startingLocations.clear()
    barriers.clear()
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
    alert(error)
    error(error)
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
