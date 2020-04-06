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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.utilitynetworks.UtilityCategory
import com.esri.arcgisruntime.utilitynetworks.UtilityCategoryComparison
import com.esri.arcgisruntime.utilitynetworks.UtilityCategoryComparisonOperator
import com.esri.arcgisruntime.utilitynetworks.UtilityElement
import com.esri.arcgisruntime.utilitynetworks.UtilityElementTraceResult
import com.esri.arcgisruntime.utilitynetworks.UtilityNetwork
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceConfiguration
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceFilter
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceParameters
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceType
import kotlinx.android.synthetic.main.activity_main.*
import java.util.UUID

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a basemap and set it to the map view
    val map = ArcGISMap(Basemap.createStreetsNightVector())

    // load the utility network data from the feature service and create feature layers
    val distributionLineFeatureTable =
      ServiceFeatureTable(getString(R.string.distribution_line_url))
    val distributionLineLayer = FeatureLayer(distributionLineFeatureTable)
    val deviceFeatureTable =
      ServiceFeatureTable(getString(R.string.device_url))
    val deviceLayer = FeatureLayer(deviceFeatureTable)

    // add the feature layers to the map
    map.operationalLayers.addAll(arrayOf(distributionLineLayer, deviceLayer))

    mapView.map = map

    // make sure the fab doesn't obscure the attribution bar
    mapView.addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
      val layoutParams = fab.layoutParams as CoordinatorLayout.LayoutParams
      layoutParams.bottomMargin += bottom - oldBottom
    }

    // show the options sheet when the floating action button is clicked
    fab.setOnClickListener {
      fab.isExpanded = !fab.isExpanded
    }

    // close the options sheet when the map is tapped
    mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
      override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (fab.isExpanded) {
          fab.isExpanded = false
        }
        return super.onTouch(view, event)
      }
    }

    // create and load the utility network
    map.addDoneLoadingListener {
      createUtilityNetwork()
    }
  }

  /**
   * Creates a utility network from the string resource url and loads it.
   * Initializes the starting point and UI.
   */
  private fun createUtilityNetwork(
  ) {
    // create a graphics overlay for the starting location and add it to the map view
    val startingLocationGraphicsOverlay = GraphicsOverlay().also {
      mapView.graphicsOverlays.add(it)
    }

    // create and apply renderers for the starting point graphics overlay
    val startingPointSymbol = SimpleMarkerSymbol(
      SimpleMarkerSymbol.Style.CROSS,
      Color.GREEN,
      25f
    )
    startingLocationGraphicsOverlay.renderer = SimpleRenderer(startingPointSymbol)

    // create a utility network from the url and load it
    val utilityNetwork = UtilityNetwork(getString(R.string.utility_network_url), mapView.map)
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

        // get the first feature for the starting location, and get its geometry
        val elementFeaturesFuture =
          utilityNetwork.fetchFeaturesForElementsAsync(listOf(startingLocation))

        elementFeaturesFuture.addDoneListener {
          try {
            val startingLocationFeatures = elementFeaturesFuture.get()

            if (startingLocationFeatures.isNotEmpty()) {
              val startingLocationGeometry = startingLocationFeatures[0].geometry

              if (startingLocationGeometry is Point) {
                val startingLocationGeometryPoint = startingLocationFeatures[0].geometry as Point

                // create a graphic for the starting location and add it to the graphics overlay
                val startingLocationGraphic =
                  Graphic(startingLocationGeometry, startingPointSymbol)
                startingLocationGraphicsOverlay.graphics.add(startingLocationGraphic)

                // set the map's viewpoint to the starting location
                mapView.setViewpointAsync(Viewpoint(startingLocationGeometryPoint, 3000.0))

                // populate the spinner with utility categories as the data and their names as the text
                spinner.adapter = object : ArrayAdapter<UtilityCategory>(
                  this,
                  android.R.layout.simple_spinner_item,
                  networkDefinition.categories
                ) {
                  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val textView = TextView(this@MainActivity)
                    textView.text = (getItem(position) as UtilityCategory).name
                    return textView
                  }

                  override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                  ): View = getView(position, convertView, parent)
                }

                trace_button.setOnClickListener {
                  fab.isExpanded = false
                  performTrace(utilityNetwork, traceConfiguration, startingLocation)
                }
              } else {
                Toast.makeText(
                  this,
                  "Error: Starting location geometry must be point.",
                  Toast.LENGTH_LONG
                ).show()
              }
            } else {
              Toast.makeText(
                this,
                "Error: Starting location features not found.",
                Toast.LENGTH_LONG
              ).show()
            }
          } catch (e: Exception) {
            val message = "Error loading starting location feature: ${e.message}"
            Log.e(TAG, message)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
          }
        }
      } else {
        Toast.makeText(this, "Error loading utility network.", Toast.LENGTH_LONG).show()
      }
    }
  }

  /**
   * Performs a valve isolation trace according to the defined trace configuration and starting location.
   * Selects the resulting features on the map.
   *
   * @param utilityNetwork the utility network to perform the trace on
   * @param traceConfiguration the trace configuration to apply to the trace
   * @param startingLocation the utility element to begin the trace from
   */
  private fun performTrace(
    utilityNetwork: UtilityNetwork,
    traceConfiguration: UtilityTraceConfiguration,
    startingLocation: UtilityElement
  ) {
    progressBar.visibility = View.VISIBLE
    // create a category comparison for the trace
    // NOTE: UtilityNetworkAttributeComparison or UtilityCategoryComparison with Operator.DoesNotExists
    // can also be used. These conditions can be joined with either UtilityTraceOrCondition or UtilityTraceAndCondition.
    val categoryComparison = UtilityCategoryComparison(
      spinner.selectedItem as UtilityCategory,
      UtilityCategoryComparisonOperator.EXISTS
    )
    // set the category comparison to the barriers of the configuration's trace filter
    traceConfiguration.filter.barriers = categoryComparison

    // set the configuration to include or leave out isolated features
    traceConfiguration.isIncludeIsolatedFeatures = include_isolated_switch.isChecked

    // build parameters for the isolation trace
    val traceParameters =
      UtilityTraceParameters(UtilityTraceType.ISOLATION, listOf(startingLocation))
    traceParameters.traceConfiguration = traceConfiguration

    // run the trace and get the result
    val utilityTraceResultsFuture = utilityNetwork.traceAsync(traceParameters);
    utilityTraceResultsFuture.addDoneListener {
      try {
        (utilityTraceResultsFuture.get()[0] as? UtilityElementTraceResult)?.let { utilityElementTraceResult ->
          if (utilityElementTraceResult.elements.isNotEmpty()) {
            Log.i(
              TAG,
              "I think the trace was successful ${utilityElementTraceResult.elements.size}"
            )
            // clear any selections from a previous trace
            mapView.map.operationalLayers.filterIsInstance<FeatureLayer>()
              .forEach { featureLayer ->
                featureLayer.clearSelection()

                with(QueryParameters()) {
                  utilityElementTraceResult.elements.filter { it.networkSource.name == featureLayer.featureTable.tableName }
                    .forEach { utilityElement ->
                      this.objectIds.add(utilityElement.objectId)
                    }

                  // select features that match the query
                  featureLayer.selectFeaturesAsync(this, FeatureLayer.SelectionMode.NEW)
                }
              }
          } else {
            // trace result is empty
            Toast.makeText(
              this,
              "Utility Element Trace Result had no elements!",
              Toast.LENGTH_LONG
            ).show()
          }
        }
        // hide the progress bar when the trace is completed or failed
        progressBar.visibility = View.GONE
      } catch (e: Exception) {
        val message = "Error loading utility trace results: ${e.message}"
        Log.e(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
      }
    }
  }
}
