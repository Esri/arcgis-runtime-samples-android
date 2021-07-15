package com.esri.arcgisruntime.sample.query_with_cql_filters

import android.graphics.Color
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.OgcFeatureCollectionTable
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.TimeExtent
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.query_with_cql_filters.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*


class MainActivity : AppCompatActivity() {

    // Keep loadable in scope to avoid garbage collection
    private lateinit var ogcFeatureCollectionTable: OgcFeatureCollectionTable

    // List of CQL where clauses
    private val cqlQueryList: List<String> = listOf(
        "F_CODE = 'AP010'",
        "{ \"eq\" : [ { \"property\" : \"F_CODE\" }, \"AP010\" ] }",
        "F_CODE LIKE 'AQ%'",
        "{\"and\":[{\"eq\":[{ \"property\" : \"F_CODE\" }, \"AP010\"]},{ \"before\":" +
                "[{ \"property\" : \"ZI001_SDV\"},\"2013-01-01\"]}]}",
        ""
    )

    // Current selected where query
    private var cqlQueryListPosition = 4

    // Number of features query should return. Default is 1000
    private var maxFeatures = 1000

    // When set to true, query searches between fromDate-toDate
    private var isDateFilter = false

    // Defines date range in queryParameters.timeExtent
    private var fromDate = Calendar.getInstance()
    private var toDate = Calendar.getInstance()

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val fab: FloatingActionButton by lazy {
        activityMainBinding.fab
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)


        // Authentication with an API key or named user is required to
        // access basemaps and other location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // Create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        // Set the map to be displayed in the layout's MapView
        mapView.map = map

        // Define strings for the service URL and collection id
        // Note that the service defines the collection id which can be
        // accessed via OgcFeatureCollectionInfo.getCollectionId().
        val serviceUrl = "https://demo.ldproxy.net/daraa"
        val collectionId = "TransportationGroundCrv"

        // Create an OGC feature collection table from the service url and collection ID
        ogcFeatureCollectionTable = OgcFeatureCollectionTable(serviceUrl, collectionId)

        // Set the feature request mode to manual
        // In this mode, the table must be manually populated
        // and panning and zooming won't request features automatically
        ogcFeatureCollectionTable.featureRequestMode =
            ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE

        ogcFeatureCollectionTable.addDoneLoadingListener {
            if (ogcFeatureCollectionTable.loadStatus == LoadStatus.LOADED) {

                // Create a feature layer and set a renderer to it to visualize the OGC API features
                val featureLayer = FeatureLayer(ogcFeatureCollectionTable)
                val simpleRenderer = SimpleRenderer(
                    SimpleLineSymbol(
                        SimpleLineSymbol.Style.SOLID,
                        Color.MAGENTA,
                        3f
                    )
                )
                featureLayer.renderer = simpleRenderer

                // Add the layer to the map
                map.operationalLayers.add(featureLayer)

                // Zoom to the dataset extent
                val datasetExtent = ogcFeatureCollectionTable.extent
                if (datasetExtent != null && !datasetExtent.isEmpty) {
                    mapView.setViewpointGeometryAsync(datasetExtent)
                }

                // Create a query based on the current visible extent
                val visibleExtentQuery = QueryParameters()
                visibleExtentQuery.geometry = datasetExtent

                // Set a limit of 3000 on the number of returned features per request,
                // the default on some services could be as low as 10
                visibleExtentQuery.maxFeatures = maxFeatures

                try {
                    // Populate the table with the query, leaving existing table entries intact
                    // Setting the outfields parameter to null requests all fields
                    ogcFeatureCollectionTable.populateFromServiceAsync(
                        visibleExtentQuery,
                        false,
                        null
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Failed to load OGC Feature Collection Table: " +
                                e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Failed to load OGC Feature Collection Table" +
                            ogcFeatureCollectionTable.loadError.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Load the table
        ogcFeatureCollectionTable.loadAsync()

        // Handles CQL Filters in a Bottom Sheet View
        fab.setOnClickListener {
            openBottomSheetView()
        }
    }

    /**
     * Handles CQL Filters in a Bottom Sheet View
     */
    private fun openBottomSheetView() {

        // Resets views in bottomSheet to default values.
        resetBottomSheetValues()

        // Creates a new BottomSheetDialog
        val dialog = BottomSheetDialog(this)

        // Inflates layout file
        val view = layoutInflater.inflate(R.layout.cql_filters_layout, null)

        // Sets the Where Clause for CQL filter
        view.findViewById<ConstraintLayout>(R.id.whereClauseLayout).setOnClickListener {

            // Creates a dialog to choose a where clause
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialog.setTitle("AlertDialog")
            val checkedItem = cqlQueryListPosition

            alertDialog.setSingleChoiceItems(
                cqlQueryList.toTypedArray(), checkedItem
            ) { dialog, which ->

                // Updates the selected where clause
                cqlQueryListPosition = which
                view.findViewById<TextView>(R.id.cqlQueryTextView).text = cqlQueryList[which]

                // Dismiss dialog
                dialog.dismiss()
            }

            // Displays the where clause dialog
            val alert: AlertDialog = alertDialog.create()
            alert.show()
        }

        // Sets the view to the default value of max features (1000)
        view.findViewById<EditText>(R.id.maxFeaturesEditText).setText(maxFeatures.toString())

        // Sets from date to Jun-13-2011 by default
        view.findViewById<DatePicker>(R.id.fromDatePicker).updateDate(2011, 5, 13)
        // Sets to date to Jan-7-2012 by default
        view.findViewById<DatePicker>(R.id.toDatePicker).updateDate(2012, 0, 7)

        // Sets up filters for the query when Apply is clicked.
        view.findViewById<TextView>(R.id.apply_tv).setOnClickListener {

            // Retrieves the max features
            val maxFeaturesText =
                view.findViewById<EditText>(R.id.maxFeaturesEditText).text.toString()
            maxFeatures = when {
                maxFeaturesText == "" -> 1000
                maxFeaturesText.toInt() <= 0 -> 1000
                else -> maxFeaturesText.toInt()
            }

            // Retrieves if date filter is selected
            isDateFilter = view.findViewById<SwitchCompat>(R.id.date_switch).isChecked

            // Retrieves from & to dates from the DatePicker
            val fromDatePicker = view.findViewById<DatePicker>(R.id.fromDatePicker)
            val toDatePicker = view.findViewById<DatePicker>(R.id.toDatePicker)
            fromDate.set(fromDatePicker.year, fromDatePicker.month, fromDatePicker.dayOfMonth)
            toDate.set(toDatePicker.year, toDatePicker.month, toDatePicker.dayOfMonth)

            // Dismiss bottom sheet view
            dialog.dismiss()

            // Runs the query using the selected filters
            runQuery()
        }

        // Dismiss bottom sheet view when cancel is clicked
        view.findViewById<TextView>(R.id.cancel_tv).setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)

        // Sets bottom sheet content view to layout
        dialog.setContentView(view)

        // Displays bottom sheet view
        dialog.show()
    }

    /**
     * Resets views in bottomSheet to default values.
     */
    private fun resetBottomSheetValues() {
        cqlQueryListPosition = 4
        maxFeatures = 1000
        isDateFilter = false
        fromDate.set(2011, 6, 13)
        toDate.set(2012, 1, 7)
    }

    /**
     * Populates features from provided query parameters, and displays the result on the map.
     */
    private fun runQuery() {
        val queryParameters = QueryParameters()

        // Set the query parameter's where clause with the the selected query
        queryParameters.whereClause = cqlQueryList[cqlQueryListPosition]

        // Sets the max features to the number entered in the text field
        queryParameters.maxFeatures = maxFeatures

        // If date filter is selected, retrieve the date selected from the date picker
        // and set it to the query parameters time extent
        if (isDateFilter) {
            // set the query parameters time extent
            queryParameters.timeExtent = TimeExtent(fromDate, toDate)
        }

        // Populate the table with the query, clear existing table entries
        // and set the outfields parameter to null requests all fields
        val result = ogcFeatureCollectionTable.populateFromServiceAsync(
            queryParameters,
            true,
            null)
        result.addDoneListener {

            // Create a new list to store returned geometries in
            val featureGeometryList: MutableList<Geometry> = ArrayList()

            // Iterate through each result to get its geometry and add it to the geometry list
            result.get().iterator().forEach { feature ->
                featureGeometryList.add(feature.geometry)
                feature.geometry
            }

            if (featureGeometryList.isNotEmpty()) {
                // Zoom to the total extent of the geometries returned by the query
                val totalExtent = GeometryEngine.combineExtents(featureGeometryList)
                mapView.setViewpointGeometryAsync(totalExtent, 20.0)
            }

            // Display number of features returned
            showResultDialog(ogcFeatureCollectionTable.totalFeatureCount)
        }
    }

    /**
     * Function to show the number of features returned
     */
    private fun showResultDialog(totalFeatureCount: Long) {

        // Build an alert dialog
        val dialogBuilder = AlertDialog.Builder(this)

        // Display message using OGC Feature Collection Table
        dialogBuilder.setMessage("Query returned $totalFeatureCount features")
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }

        // Create dialog box
        val alert = dialogBuilder.create()
        alert.setTitle("Query Result")

        // Shows alert dialog
        alert.show()
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