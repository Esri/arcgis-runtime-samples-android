package com.esri.arcgisruntime.sample.performvalveisolationtrace

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
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
    val map = ArcGISMap(Basemap.createStreetsNightVector()).also { mapView.map = it }

    // load the utility network data from the feature service and create feature layers
    val distributionLineFeatureTable = ServiceFeatureTable("${R.string.featureServiceURL}/3")
    val distributionLineLayer = FeatureLayer(distributionLineFeatureTable)
    val deviceFeatureTable = ServiceFeatureTable("${R.string.featureServiceURL}/0")
    val deviceLayer = FeatureLayer(deviceFeatureTable)

    // add the feature layers to the map
    map.operationalLayers.addAll(arrayOf(distributionLineLayer, deviceLayer))

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
      createUtilityNetwork(startingPointSymbol, startingLocationGraphicsOverlay)
    }
  }

  private fun createUtilityNetwork(
    startingPointSymbol: SimpleMarkerSymbol,
    startingLocationGraphicsOverlay: GraphicsOverlay
  ) {
    val utilityNetwork = UtilityNetwork(getString(R.string.featureServiceURL), mapView.map)
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

                spinner.adapter = ArrayAdapter<UtilityCategory>(
                  this,
                  android.R.layout.simple_spinner_item,
                  networkDefinition.categories
                )

                trace_button.setOnClickListener {
                  fab.isExpanded = false
                  handleTraceClick(utilityNetwork, traceConfiguration, startingLocation)
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

  private fun handleTraceClick(utilityNetwork: UtilityNetwork, traceConfiguration: UtilityTraceConfiguration, startingLocation: UtilityElement) {
    try {
      // clear previous selections from the layers
      mapView.map.operationalLayers.forEach { layer ->
        // TODO: in the java they cast the layer to featurelayer before clearing selection
        if (layer is FeatureLayer) layer.clearSelection()
      }

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
      traceConfiguration.isIncludeIsolatedFeatures = include_isolated_switch.isSelected

      // build parameters for the isolation trace
      val traceParameters = UtilityTraceParameters(UtilityTraceType.ISOLATION, listOf(startingLocation))
      traceParameters.traceConfiguration = traceConfiguration

      // run the trace and get the result
      val utilityTraceResultsFuture = utilityNetwork.traceAsync(traceParameters);
      utilityTraceResultsFuture.addDoneListener{
        try {
          val utilityTraceResults = utilityTraceResultsFuture.get()

          if (utilityTraceResults[0] is UtilityElementTraceResult) {
            val utilityElementTraceResult = utilityTraceResults[0] as UtilityElementTraceResult

            if (utilityElementTraceResult.elements.isNotEmpty()) {
              Log.i(TAG, "I think the trace was successful ${utilityElementTraceResult.elements.size}")

              mapView.map.operationalLayers.forEach {layer ->
                if (layer is FeatureLayer) {
                  val queryParameters = QueryParameters()
                  utilityElementTraceResult.elements.forEach{utilityElement ->
                    val networkSourceName = utilityElement.networkSource.name
                    val featureTableName = (layer as FeatureLayer).featureTable.tableName

                    if (networkSourceName == featureTableName) {
                      queryParameters.objectIds.add(utilityElement.objectId)
                    }
                  }
                  val featureQueryResultListenableFuture = (layer as FeatureLayer).selectFeaturesAsync(queryParameters, FeatureLayer.SelectionMode.NEW)

                  featureQueryResultListenableFuture.addDoneListener {

                    progressBar.visibility = View.GONE
                  }
                }
              }
            }
          }
        }catch (e: Exception) {
          //TODO
        }
      }

    } catch (e: Exception) {
      //TODO
    }
  }
}
