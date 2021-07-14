package com.esri.arcgisruntime.sample.query_with_cql_filters

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.TimeExtent
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.sample.query_with_cql_filters.databinding.ActivityMainBinding
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*


class MainActivity : AppCompatActivity() {

    // objects that implement Loadable must be class fields to prevent being garbage collected before loading
    private lateinit var portal: Portal

    // keep loadable in scope to avoid garbage collection
    private lateinit var ogcFeatureCollectionTable: OgcFeatureCollectionTable

    private val cqlQueryList: List<String> = listOf(
        "F_CODE = 'AP010'",
        "{ \"eq\" : [ { \"property\" : \"F_CODE\" }, \"AP010\" ] }",
        "F_CODE LIKE 'AQ%'",
        "{\"and\":[{\"eq\":[{ \"property\" : \"F_CODE\" }, \"AP010\"]},{ \"before\":" +
                "[{ \"property\" : \"ZI001_SDV\"},\"2013-01-01\"]}]}",
        ""
    )

    private var cqlQueryListPosition = 4

    private var maxFeatures = 1000

    private var isDateFilter = false

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


        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        //[DocRef: Name=Create map-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
        // create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)
        //[DocRef: END]

        //[DocRef: Name=Set map-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
        // set the map to be displayed in the layout's MapView
        mapView.map = map
        //[DocRef: END]

        showCredentialDialog()

        // define strings for the service URL and collection id
        // note that the service defines the collection id which can be accessed via OgcFeatureCollectionInfo.getCollectionId().
        val serviceUrl = "https://demo.ldproxy.net/daraa"
        val collectionId = "TransportationGroundCrv"

        // create an OGC feature collection table from the service url and collection id
        ogcFeatureCollectionTable = OgcFeatureCollectionTable(serviceUrl, collectionId)

        // set the feature request mode to manual
        // in this mode, the table must be manually populated - panning and zooming won't request features automatically
        ogcFeatureCollectionTable.featureRequestMode =
            ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE

        ogcFeatureCollectionTable.addDoneLoadingListener {
            if (ogcFeatureCollectionTable.loadStatus == LoadStatus.LOADED) {

                // create a feature layer and set a renderer to it to visualize the OGC API features
                val featureLayer = FeatureLayer(ogcFeatureCollectionTable)
                val simpleRenderer = SimpleRenderer(
                    SimpleLineSymbol(
                        SimpleLineSymbol.Style.SOLID,
                        Color.MAGENTA,
                        3f
                    )
                )
                featureLayer.renderer = simpleRenderer

                // add the layer to the map
                map.operationalLayers.add(featureLayer)

                // zoom to the dataset extent
                val datasetExtent = ogcFeatureCollectionTable.extent
                if (datasetExtent != null && !datasetExtent.isEmpty) {
                    mapView.setViewpointGeometryAsync(datasetExtent)
                }

                // create a query based on the current visible extent
                val visibleExtentQuery = QueryParameters()
                visibleExtentQuery.geometry = datasetExtent

                // set a limit of 3000 on the number of returned features per request, the default on some services could be as low as 10
                visibleExtentQuery.maxFeatures = 1000

                try {
                    // populate the table with the query, leaving existing table entries intact
                    // setting the outfields parameter to null requests all fields
                    ogcFeatureCollectionTable.populateFromServiceAsync(
                        visibleExtentQuery,
                        false,
                        null
                    ).addDoneListener {
                        Toast.makeText(this, "OGC Loaded", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load:" + e.message, Toast.LENGTH_LONG).show()
                }


            } else {
                Toast.makeText(
                    this,
                    "Failed to load OGC Feature Collection Table",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // load the table
        ogcFeatureCollectionTable.loadAsync()

        fab.setOnClickListener {
            openBottomSheetView()
        }


    }

    private fun openBottomSheetView() {

        resetBottomDialogValues()

        //Launch fragment for CQL Filters
        // on below line we are creating a new bottom sheet dialog.
        val dialog = BottomSheetDialog(this)

        // on below line we are inflating a layout file which we have created.
        val view = layoutInflater.inflate(R.layout.cql_filters_layout, null)

        view.findViewById<ConstraintLayout>(R.id.whereClauseLayout).setOnClickListener {

            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialog.setTitle("AlertDialog")
            val checkedItem = cqlQueryListPosition
            alertDialog.setSingleChoiceItems(
                cqlQueryList.toTypedArray(), checkedItem
            ) { dialog, which ->
                cqlQueryListPosition = which
                view.findViewById<TextView>(R.id.cqlQueryTextView).text = cqlQueryList[which]
                dialog.dismiss()
            }
            val alert: AlertDialog = alertDialog.create()
            alert.setCanceledOnTouchOutside(false)
            alert.show()

        }

        view.findViewById<EditText>(R.id.maxFeaturesEditText).setText(maxFeatures.toString())

        view.findViewById<DatePicker>(R.id.fromDatePicker).updateDate(2011,5,13)
        view.findViewById<DatePicker>(R.id.toDatePicker).updateDate(2012,0,7)

        view.findViewById<TextView>(R.id.apply_tv).setOnClickListener {
            val maxFeaturesText = view.findViewById<EditText>(R.id.maxFeaturesEditText).text.toString()
            maxFeatures = when {
                maxFeaturesText == "" -> 1000
                maxFeaturesText.toInt() <= 0 -> 1000
                else -> maxFeaturesText.toInt()
            }

            isDateFilter = view.findViewById<SwitchCompat>(R.id.date_switch).isChecked

            val fromDatePicker = view.findViewById<DatePicker>(R.id.fromDatePicker)
            fromDate.set(fromDatePicker.year, fromDatePicker.month, fromDatePicker.dayOfMonth)

            val toDatePicker = view.findViewById<DatePicker>(R.id.toDatePicker)
            toDate.set(toDatePicker.year, toDatePicker.month, toDatePicker.dayOfMonth)

            Log.e("Max", maxFeatures.toString())
            Log.e("date", isDateFilter.toString())

            dialog.dismiss()

            runQuery()
        }
        view.findViewById<TextView>(R.id.cancel_tv).setOnClickListener { dialog.dismiss() }

        // below line is use to set cancelable to avoid
        // closing of dialog box when clicking on the screen.
        dialog.setCancelable(false)

        // on below line we are setting
        // content view to our view.
        dialog.setContentView(view)

        // Expand the BottomSheet layout
        BottomSheetBehavior.from(view).state = BottomSheetBehavior.STATE_EXPANDED;

        // on below line we are calling
        // a show method to display a dialog.
        dialog.show()
    }

    private fun resetBottomDialogValues() {
        cqlQueryListPosition = 4
        maxFeatures = 1000
        isDateFilter = false
        fromDate.set(2011,6, 13)
        toDate.set(2012,1,7)
    }

    private fun runQuery() {
        val queryParameters = QueryParameters()

        // set the query parameter's where clause with the CQL query in the combo box
        queryParameters.whereClause = cqlQueryList[cqlQueryListPosition]

        // set the max features to the number entered in the text field
        queryParameters.maxFeatures = maxFeatures

        // if the time extent checkbox is selected, retrieve the date selected from the date picker and set it to the
        // query parameters time extent
        if (isDateFilter) {
            // set the query parameters time extent
            queryParameters.timeExtent = TimeExtent(fromDate,toDate)
        }

        // populate the table with the query, clear existing table entries and set the outfields parameter to null requests all fields
        ogcFeatureCollectionTable.populateFromServiceAsync(queryParameters,true,null).addDoneListener {

            // display number of features returned
            showResultDialog(ogcFeatureCollectionTable.totalFeatureCount)
        }
    }

    private fun showCredentialDialog() {
        // Set the DefaultAuthenticationChallengeHandler to allow authentication with the portal.
        val handler = DefaultAuthenticationChallengeHandler(this)
        AuthenticationManager.setAuthenticationChallengeHandler(handler)
        // Set loginRequired to true always prompt for credential,
        // When set to false to only login if required by the portal
        portal = Portal("https://www.arcgis.com", true)
        portal.addDoneLoadingListener {
            when (portal.loadStatus) {
                LoadStatus.LOADED -> {
                    Toast.makeText(this, "Portal loaded", Toast.LENGTH_LONG).show()
                }
                LoadStatus.FAILED_TO_LOAD -> {
                    Toast.makeText(this, "Portal failed to load", Toast.LENGTH_LONG).show()
                }
            }
        }
        portal.loadAsync()
    }

    private fun showResultDialog(totalFeatureCount: Long) {

        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this)

        // set message of alert dialog
        dialogBuilder.setMessage("Query returned $totalFeatureCount features")
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("Query Result")

        // show alert dialog
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