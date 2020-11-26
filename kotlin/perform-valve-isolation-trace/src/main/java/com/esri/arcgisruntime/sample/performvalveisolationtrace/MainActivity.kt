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
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
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
import com.esri.arcgisruntime.utilitynetworks.UtilityNetworkDefinition
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceConfiguration
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceFilter
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceParameters
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceType
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.spinner_text_item.view.*
import java.util.UUID

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  // create a graphics overlay for the starting location and add it to the map view
  private val startingLocationGraphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private lateinit var utilityNetwork: UtilityNetwork

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

    // load the utility network data from the feature service and create feature layers
    val distributionLineFeatureTable = ServiceFeatureTable(getString(R.string.distribution_line_url))
    val distributionLineLayer = FeatureLayer(distributionLineFeatureTable)
    val deviceFeatureTable = ServiceFeatureTable(getString(R.string.device_url))
    val deviceLayer = FeatureLayer(deviceFeatureTable)

    // create a map with the utility network distribution line and device layers
    val map = ArcGISMap(BasemapStyle.ARCGIS_STREETS_NIGHT).apply {
      // add the feature layers to the map
      operationalLayers.addAll(listOf(distributionLineLayer, deviceLayer))
      // create and load the utility network
      addDoneLoadingListener {
        createUtilityNetwork()
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
      }
    }
    // show the options sheet when the floating action button is clicked
    fab.setOnClickListener {
      fab.isExpanded = !fab.isExpanded
    }
  }

  /**
   * Create and load a utility network from the string resource url and initialize a starting point
   * from it.
   */
  private fun createUtilityNetwork(
  ) {
    // create a utility network from the url and load it
    utilityNetwork = UtilityNetwork(getString(R.string.utility_network_url), mapView.map)
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

                populateCategorySpinner(networkDefinition)

                trace_button.setOnClickListener {
                  fab.isExpanded = false
                  performTrace(utilityNetwork, traceConfiguration, startingLocation)
                }
              }
            } else {
              val message = "Starting location features not found."
              Log.i(TAG, message)
              Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
              ).show()
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
  }

  /**
   * Performs a valve isolation trace according to the defined trace configuration and starting location, and selects the resulting features on the map.
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
    // NOTE: UtilityNetworkAttributeComparison or UtilityCategoryComparisonOperator.DOES_NOT_EXIST
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
    val utilityTraceResultsFuture = utilityNetwork.traceAsync(traceParameters)
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
                featureLayer.selectFeaturesAsync(queryParameters, FeatureLayer.SelectionMode.NEW)
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

  /**
   * Initialize the category selection spinner using a utility network definition.
   *
   * @param networkDefinition the utility network definition to populate the spinner
   */
  private fun populateCategorySpinner(
    networkDefinition: UtilityNetworkDefinition
  ) {
    // populate the spinner with utility categories as the data and their names as the text
    spinner.adapter =
      object : ArrayAdapter<UtilityCategory>(
        this,
        R.layout.spinner_text_item,
        networkDefinition.categories
      ) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
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
