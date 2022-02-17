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

package com.esri.arcgisruntime.sample.showdevicelocationusingindoorpositioning

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.data.QueryParameters.OrderBy
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.location.IndoorsLocationDataSource
import com.esri.arcgisruntime.location.LocationDataSource
import com.esri.arcgisruntime.location.LocationDataSource.StatusChangedListener
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.LayerList
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.showdevicelocationusingindoorpositioning.databinding.ActivityMainBinding
import com.esri.arcgisruntime.security.UserCredential
import java.text.DecimalFormat
import java.util.*
import java.util.stream.Collectors


class MainActivity : AppCompatActivity(), LocationDataSource.LocationChangedListener,
    StatusChangedListener {

    private val TAG = MainActivity::class.java.simpleName

    private var mCurrentFloor: Int? = null

    // Provides an indoor or outdoor position based on device sensor data (radio, GPS, motion sensors).
    private var mIndoorsLocationDataSource: IndoorsLocationDataSource? = null

    private val setupResultCompletionHandler: ResultsCallback =
        object : ResultsCallback{
            override fun onSuccess() {
                // Start location display after successful setup
                startLocationDisplay()
            }

            override fun onError(exception: Exception?) {
                if(exception != null){
                    Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, exception.message.toString())
                }
            }
        }

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val progressBar: ProgressBar by lazy {
        activityMainBinding.progressBar
    }

    private val textView: TextView by lazy {
        activityMainBinding.textView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // check for location permissions
        // if permissions is allowed, the device's current location is shown
        checkPermissions()
    }

    /**
     * Set up the connection between the device and the portal item
     */
    private fun connectToPortal() {
        val portal = Portal("https://viennardc.maps.arcgis.com/", true)
        portal.credential = UserCredential("tester_viennardc", "password.testing12345")
        portal.addDoneLoadingListener {
            if(portal.loadStatus == LoadStatus.LOADED){
                val portalItem = PortalItem(portal, "89f88764c29b48218366855d7717d266")
                setupMap(portalItem)
            }else{
                Toast.makeText(this, portal.loadError.message, Toast.LENGTH_SHORT).show()
                Log.e(TAG,portal.loadError.message.toString())
            }
        }
        portal.loadAsync()
    }

    /**
     * Set the [mapView] using the [portalItem] then invokes [loadTables]
     */
    private fun setupMap(portalItem: PortalItem) {
        mapView.apply {
            map = ArcGISMap(portalItem)
            map.addDoneLoadingListener {
                if (map.loadStatus == LoadStatus.LOADED){
                    val featureTables = map.tables
                    // if the portalItem does not contain any featureTables
                    if(featureTables.isEmpty()){
                        val message = "Map does not contain feature tables"
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                        Log.e(TAG,message)
                    }else{
                        setUpLoadTables(featureTables)
                    }
                }else{
                    val error = map.loadError
                    Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            }
            map.loadAsync()
        }
    }

    /**
     * Set up the ResultsCallback for when all tables are loaded
     */
    private fun setUpLoadTables(featureTables: MutableList<FeatureTable>) {
        // portalItem contains featureTables so perform load on each featureTable
        loadTables(featureTables, object : ResultsCallback {
            override fun onSuccess() {
                setupIndoorsLocationDataSource(featureTables, object : ResultsCallback{
                    override fun onSuccess() {
                        // Start location display after successful setup
                        startLocationDisplay()
                    }

                    override fun onError(exception: Exception?) {
                        if(exception != null){
                            Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_SHORT).show()
                            Log.e(TAG, exception.message.toString())
                        }
                    }
                })
            }
            override fun onError(exception: Exception?) {
                val message = "Failed to load feature tables"
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                Log.e(TAG,message)
            }
        })
    }

    /**
     * Recursively loads each [featureTables] and calls [resultsCallback] once each table is loaded
     */
    private fun loadTables(featureTables: MutableList<FeatureTable>, resultsCallback: ResultsCallback) {
        val iterator = featureTables.iterator()
        if(iterator.hasNext()){
            val table = iterator.next()
            table.addDoneLoadingListener {
                if (table.loadStatus == LoadStatus.LOADED) {
                    // skip the first table from the featureTables list since it is loaded
                    val newFeatureTables = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        // this can be executed in one line in API >= 24
                        featureTables.stream().skip(1).collect(Collectors.toList())
                    } else {
                        // if using API < 24 use if-else
                        if (featureTables.size>1)
                            featureTables.subList(1,featureTables.size)
                        else
                            featureTables.subList(0,0)
                    }
                    // recursively call loadTables() with the list of featureTables to load
                    loadTables(newFeatureTables, resultsCallback)
                } else {
                    resultsCallback.onError(table.loadError)
                }
            }
            table.loadAsync()
        }else{
            // once all featureTables is loaded the featureTables list will be empty
            resultsCallback.onSuccess()
        }
    }

    /**
     * Sets up the [mIndoorsLocationDataSource] using the positioningTable
     */
    private fun setupIndoorsLocationDataSource(featureTables: List<FeatureTable>, resultsCallback: ResultsCallback) {
        // positioningTable needs to be present
        var positioningTable: FeatureTable? = null
        for(featureTable in featureTables){
            if(featureTable.tableName.equals("ips_positioning")){
                positioningTable = featureTable
                // since positioning table is found, exit loop
                //TODO Handle if positioning table is not found
                continue
            }
        }

        if (positioningTable != null){
            val serviceFeatureTable = positioningTable as ServiceFeatureTable
            // when multiple entries are available, IndoorsLocationDataSource constructor function
            // looks up the entry with the most recent date and takes this positioning data.

            //Custom initialization
            val queryParameters = QueryParameters()
            queryParameters.maxFeatures = 1
            queryParameters.whereClause = "1 = 1"

            val orderByFields = queryParameters.orderByFields
            val dateCreatedFieldName = getDateCreatedFieldName(serviceFeatureTable.fields);
            orderByFields.add(OrderBy(dateCreatedFieldName, QueryParameters.SortOrder.DESCENDING))

            val resultFuture = serviceFeatureTable.queryFeaturesAsync(queryParameters)
            val result = resultFuture.get()
            val featureIterator: Iterator<Feature> = result.iterator()
            val feature: Feature

            if(featureIterator.hasNext()){
                feature = featureIterator.next()
            } else{
                // positioningTable has no entries
                resultsCallback.onError(Exception("Map does not support IPS."))
                return
            }

            // The ID that identifies a row in the positioning table.

            // The ID that identifies a row in the positioning table.
            val globalID = Objects.requireNonNull(feature.attributes[serviceFeatureTable.globalIdField]).toString()
            val positioningId = UUID.fromString(globalID)

            // Setting up IndoorsLocationDataSource with positioning, pathways tables and positioning ID.
            // positioningTable - the "ips_positioning" feature table from an IPS-enabled map.
            // pathwaysTable - An ArcGISFeatureTable that contains pathways as per the ArcGIS Indoors Information Model.
            //   Setting this property enables path snapping of locations provided by the IndoorsLocationDataSource.
            // positioningID - an ID which identifies a specific row in the positioningTable that should be used for setting up IPS.
            mIndoorsLocationDataSource = IndoorsLocationDataSource(this, serviceFeatureTable, getPathwaysTable(),positioningId)
            resultsCallback.onSuccess()
        }
    }

    /**
     * Find the exact formatting of the name "DateCreated" in the list of ServiceFeatureTable fields.
     */
    private fun getDateCreatedFieldName(fields: List<Field>): String? {
        for(field in fields){
            if(field.name.equals("DateCreated", ignoreCase = true) || field.name.equals("Date_Created", ignoreCase = true)){
                return field.name
            }
        }
        return ""
    }

    /**
     * Retrieves the PathwaysTable
     */
    private fun getPathwaysTable(): ArcGISFeatureTable? {
        for(layer in mapView.map.operationalLayers){
            if(layer.name.equals("Pathways")){
                return (layer as FeatureLayer).featureTable as ArcGISFeatureTable
            }
        }

        // if pathways table not found in map's operationalLayers
        val message = "PathwaysTable not found"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e(TAG,message)
        return null
    }

    /**
     * Invokes when the app is closed or LocationDataSource is stopped.
     */
    private fun stopLocationDisplay() {
        mapView.locationDisplay.stop()
        mapView.locationDisplay.locationDataSource.removeStatusChangedListener(this)
        mapView.locationDisplay.locationDataSource.removeLocationChangedListener(this)
    }

    /**
     * Sets up the location listeners, the navigation mode, and display's the devices location as a blue dot
     */
    private fun startLocationDisplay() {
        val locationDisplay: LocationDisplay = mapView.locationDisplay
        locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
        locationDisplay.locationDataSource = mIndoorsLocationDataSource
        // these listeners will receive location, heading and status updates from the location data source.
        locationDisplay.locationDataSource.addStatusChangedListener(this)
        locationDisplay.locationDataSource.addLocationChangedListener(this)
        // asynchronously start of the location display,
        // which will in-turn start IndoorsLocationDataSource to start receiving IPS updates.
        locationDisplay.startAsync()
    }

    /**
     * Handles the data received on a [locationChangedEvent]
     * like currentFloor, positionSource, transmitterCount, networkCount and horizontalAccuracy
     */
    override fun locationChanged(locationChangedEvent: LocationDataSource.LocationChangedEvent?) {
        val location = locationChangedEvent?.location

        // retrieve information about the location of the device
        val decimalFormat = DecimalFormat(".##")
        val floor =
            if (location?.additionalSourceProperties?.get("floor") != null)
                location.additionalSourceProperties["floor"].toString()
            else ""
        val positionSource =
            if (location?.additionalSourceProperties?.get(LocationDataSource.Location.KEY_POSITION_SOURCE) != null)
                location.additionalSourceProperties[LocationDataSource.Location.KEY_POSITION_SOURCE].toString()
            else ""
        val transmitterCount =
            if (location?.additionalSourceProperties?.get("transmitterCount") != null)
                location.additionalSourceProperties["transmitterCount"].toString()
            else ""
        val networkCount =
            if (location?.additionalSourceProperties?.get(LocationDataSource.Location.KEY_SATELLITE_COUNT) != null)
                location.additionalSourceProperties[LocationDataSource.Location.KEY_SATELLITE_COUNT].toString()
            else ""

        if (mapView.map != null && mapView.map.loadStatus == LoadStatus.LOADED) {
            val newFloor = floor.toInt()
            setupLayers(newFloor)
        } else {
            Log.e(TAG, "The map is not loaded yet!")
        }

        var message = "" +
                "Floor: $floor\n" +
                "Position source: $positionSource\n" +
                "Horizontal accuracy: "+ location?.let {decimalFormat.format(it.horizontalAccuracy)} + "m\n"

        Log.e("IPS:", message)
        if (positionSource == LocationDataSource.Location.POSITION_SOURCE_GNSS) {
            message += "Satellite count: $networkCount"
        } else if (positionSource == "BLE") {
            message += "Transmitter count: $transmitterCount"
        }

        textView.text = message
    }

    private fun setupLayers(floor: Int) {
        if (mCurrentFloor == null || mCurrentFloor != floor) {
            mCurrentFloor = floor
        } else {
            return
        }
        val layerList: LayerList = mapView.map.operationalLayers
        for (layer in layerList) {
            val name = layer.name
            if (layer is FeatureLayer && (name == "Details" || name == "Units" || name == "Levels")) {
                layer.definitionExpression = "VERTICAL_ORDER = $floor"
            }
        }
    }


    /**
     * Handle status changes of IndoorsLocationDataSource using the [statusChangedEvent]
     */
    override fun statusChanged(statusChangedEvent: LocationDataSource.StatusChangedEvent?) {
        when (statusChangedEvent?.status) {
            LocationDataSource.Status.STARTING -> progressBar.visibility = View.VISIBLE
            LocationDataSource.Status.STARTED -> progressBar.visibility = View.GONE
            LocationDataSource.Status.FAILED_TO_START -> {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Failed to start IndoorsLocationDataSource. Error: " + (mIndoorsLocationDataSource?.error
                        ?.localizedMessage), Toast.LENGTH_LONG
                ).show()
            }
            LocationDataSource.Status.STOPPED -> {
                progressBar.visibility = View.GONE
                stopLocationDisplay()
                Toast.makeText(
                    this,
                    "IndoorsLocationDataSource stopped due to an internal error",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Check for location permissions, if not received then request for one
     */
    private fun checkPermissions() {
        val requestCode = 1
        val requestPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (ContextCompat.checkSelfPermission(this, requestPermissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, requestPermissions, requestCode)
        } else {
            // permission already given, so no need to request
            connectToPortal()
        }
    }

    /**
     * Result of the user from location permissions request
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // if location permissions accepted, start setting up IndoorsLocationDataSource
            connectToPortal()
        } else {
            val message = "Location permission is not granted"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.e(TAG,message)
        }
    }

    internal interface ResultsCallback{
        fun onSuccess()
        fun onError(exception: Exception?)
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
        stopLocationDisplay()
        mapView.dispose()
        super.onDestroy()
    }
}
