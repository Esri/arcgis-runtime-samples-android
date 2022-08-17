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
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.FeatureTable
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.data.ArcGISFeatureTable
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
import java.util.UUID

class MainActivity : AppCompatActivity(), LocationDataSource.LocationChangedListener,
    StatusChangedListener {

    private val TAG = MainActivity::class.java.simpleName

    private var currentFloor: Int? = null

    // Provides an indoor or outdoor position based on device sensor data (radio, GPS, motion sensors).
    private var mIndoorsLocationDataSource: IndoorsLocationDataSource? = null

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
        // load the portal and create a map from the portal item
        val portal = Portal("https://www.arcgis.com/", false)
        val portalItem = PortalItem(portal, "8fa941613b4b4b2b8a34ad4cdc3e4bba")
        setupMap(portalItem)
    }

    /**
     * Set the [mapView] using the [portalItem] then invokes [loadTables]
     */
    private fun setupMap(portalItem: PortalItem) {
        mapView.map = ArcGISMap(portalItem)
        val map = mapView.map
        map.addDoneLoadingListener {
            if (mapView.map.loadStatus == LoadStatus.LOADED) {
                val featureTables = map.tables
                // check if the portalItem contains featureTables
                if (featureTables.isNotEmpty()) {
                    setUpLoadTables(featureTables)
                } else {
                    val message = "Map does not contain feature tables"
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, message)
                }
            } else {
                val message = "Error loading map: " + map.loadError.message
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error loading map: " + map.loadError.message)
            }
        }
    }

    /**
     * Set up the ResultsCallback for when all tables are loaded
     */
    private fun setUpLoadTables(featureTables: MutableList<FeatureTable>) {
        // portalItem contains featureTables, so perform load on each featureTable
        loadTables(featureTables, object : ResultsCallback {
            override fun onSuccess() {
                // set up the data source using the feature tables, then start the data source
                setupIndoorsLocationDataSource(featureTables)
            }

            override fun onError(exception: Exception?) {
                val message = "Failed to load feature tables: " + (exception?.message ?: "")
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                Log.e(TAG, message)
            }
        })
    }

    /**
     * Recursively loads each [featureTables] and calls [resultsCallback] once each table is loaded
     */
    private fun loadTables(
        featureTables: MutableList<FeatureTable>,
        resultsCallback: ResultsCallback
    ) {
        val iterator = featureTables.iterator()
        if (iterator.hasNext()) {
            val table = iterator.next()
            table.addDoneLoadingListener {
                if (table.loadStatus == LoadStatus.LOADED) {
                    // skip the first table from the featureTables list since it is loaded
                    val newFeatureTables = featureTables.subList(1, featureTables.size)
                    // recursively call loadTables() with the updated list of featureTables to load
                    loadTables(newFeatureTables, resultsCallback)
                } else {
                    resultsCallback.onError(table.loadError)
                }
            }
            table.loadAsync()
        } else {
            // once all featureTables is loaded the featureTables list will be empty
            resultsCallback.onSuccess()
        }
    }

    /**
     * Sets up the [mIndoorsLocationDataSource] using the positioningTable
     */
    private fun setupIndoorsLocationDataSource(featureTables: List<FeatureTable>) {
        // positioningTable needs to be present
        val positioningTable = featureTables.firstOrNull { it.tableName.equals("ips_positioning") }

        if (positioningTable != null) {
            val serviceFeatureTable = positioningTable as ServiceFeatureTable
            // when multiple entries are available, IndoorsLocationDataSource constructor function
            // looks up the entry with the most recent date and takes this positioning data
            // set up queryParameters to grab one result.
            val dateCreatedFieldName = getDateCreatedFieldName(serviceFeatureTable.fields)
            if (dateCreatedFieldName == null) {
                val message = "The service table does not contain \"DateCreated\" fields."
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                Log.e(TAG, message)
                return
            }
            val queryParameters = QueryParameters().apply {
                maxFeatures = 1
                whereClause = "1 = 1"
                // find and sort out the orderByFields by most recent first
                orderByFields.add(
                    OrderBy(
                        dateCreatedFieldName,
                        QueryParameters.SortOrder.DESCENDING
                    )
                )
            }
            // perform search query using the queryParameters
            val resultFuture = serviceFeatureTable.queryFeaturesAsync(queryParameters)
            resultFuture.addDoneListener {
                val featureIterator: Iterator<Feature> = resultFuture.get().iterator()
                // check if serviceFeatureTable contains positioning data
                if (featureIterator.hasNext()) {
                    // The ID that identifies a row in the positioning table.
                    val globalID =
                        featureIterator.next().attributes[serviceFeatureTable.globalIdField].toString()
                    val positioningId = UUID.fromString(globalID)
                    // Setting up IndoorsLocationDataSource with positioning, pathways tables and positioning ID.
                    // positioningTable - the "ips_positioning" feature table from an IPS-enabled map.
                    // pathwaysTable - An ArcGISFeatureTable that contains pathways as per the ArcGIS Indoors Information Model.
                    // Setting this property enables path snapping of locations provided by the IndoorsLocationDataSource.
                    // positioningID - an ID which identifies a specific row in the positioningTable that should be used for setting up IPS.
                    mIndoorsLocationDataSource = IndoorsLocationDataSource(
                        this,
                        serviceFeatureTable,
                        getPathwaysTable(),
                        positioningId
                    )
                    // start the location display (blue dot)
                    startLocationDisplay()
                } else {
                    val message = "The positioning table contain no data."
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    Log.e(TAG, message)
                }
            }
        } else {
            val message = "Positioning Table not found in FeatureTables"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            Log.e(TAG, message)
        }
    }

    /**
     * Find the exact formatting of the name "DateCreated" in the list of ServiceFeatureTable fields.
     */
    private fun getDateCreatedFieldName(fields: List<Field>): String? {
        val field = fields.find {
            it.name.equals(
                "DateCreated",
                ignoreCase = true
            ) || it.name.equals("Date_Created", ignoreCase = true)
        }
        return field?.name
    }

    /**
     * Retrieves the PathwaysTable
     */
    private fun getPathwaysTable(): ArcGISFeatureTable? {
        return try {
            val pathwaysFeatureLayer =
                mapView.map.operationalLayers.firstOrNull { it.name.equals("Pathways") } as? FeatureLayer
            pathwaysFeatureLayer?.featureTable as? ArcGISFeatureTable
        } catch (e: Exception) {
            // if pathways table not found in map's operationalLayers
            val message = "PathwaysTable not found"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            Log.e(TAG, message)
            null
        }
    }

    /**
     * Invokes when the app is closed or LocationDataSource is stopped.
     */
    private fun stopLocationDisplay() {
        mapView.locationDisplay.stop()
        mapView.locationDisplay.locationDataSource.apply {
            removeStatusChangedListener(this@MainActivity)
            removeLocationChangedListener(this@MainActivity)
        }
    }

    /**
     * Sets up the location listeners, the navigation mode, and display's the devices location as a blue dot
     */
    private fun startLocationDisplay() {
        val locationDisplay: LocationDisplay = mapView.locationDisplay.apply {
            autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
            locationDataSource = mIndoorsLocationDataSource
        }
        // these listeners will receive location, heading and status updates from the location data source.
        locationDisplay.locationDataSource.apply {
            addStatusChangedListener(this@MainActivity)
            addLocationChangedListener(this@MainActivity)
        }
        // asynchronously start of the location display,
        // which will in-turn start IndoorsLocationDataSource to start receiving IPS updates.
        locationDisplay.startAsync()
    }

    /**
     * Handles the data received on a [locationChangedEvent]
     * like currentFloor, positionSource, transmitterCount, networkCount and horizontalAccuracy
     */
    override fun locationChanged(locationChangedEvent: LocationDataSource.LocationChangedEvent?) {
        // get the location properties of the LocationDataSource
        val locationProperties = locationChangedEvent?.location?.additionalSourceProperties
        if (locationProperties == null) {
            Toast.makeText(
                this,
                "LocationDataSource does not have any property-fields",
                Toast.LENGTH_LONG
            ).show()
            Log.e(TAG, "LocationDataSource does not have any property-fields")
            return
        }
        // retrieve information about the location of the device
        val floor = (locationProperties["floor"] ?: "").toString()
        val positionSource =
            (locationProperties[LocationDataSource.Location.KEY_POSITION_SOURCE] ?: "").toString()
        val transmitterCount = (locationProperties["transmitterCount"] ?: "").toString()
        val networkCount =
            (locationProperties[LocationDataSource.Location.KEY_SATELLITE_COUNT] ?: "").toString()

        // check if current floor hasn't been set or if the floor has changed
        val newFloor = floor.toInt()
        if (currentFloor == null || currentFloor != newFloor) {
            currentFloor = newFloor
            // set up the floor layer with the newFloor
            setupLayers()
        }
        // set up the message with floor properties to be displayed to the textView
        var locationPropertiesMessage =
            "Floor: $floor, Position-source: $positionSource, " +
                "Horizontal-accuracy: " + locationChangedEvent.location.let {
                DecimalFormat(".##").format(
                    it.horizontalAccuracy
                )
            } + "m, "
        if (positionSource == LocationDataSource.Location.POSITION_SOURCE_GNSS) {
            locationPropertiesMessage += "Satellite-count: $networkCount"
        } else if (positionSource == "BLE") {
            locationPropertiesMessage += "Transmitter-count: $transmitterCount"
        }
        textView.text = locationPropertiesMessage
    }

    /**
     * Set up the floor layer when the device location is updated
     */
    private fun setupLayers() {
        // update layer's definition express with the current floor
        val layerList: LayerList = mapView.map.operationalLayers
        layerList.forEach { layer ->
            val name = layer.name
            if (layer is FeatureLayer && (name == "Details" || name == "Units" || name == "Levels")) {
                layer.definitionExpression = "VERTICAL_ORDER = $currentFloor"
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
                Log.e(TAG, "Failed to start IndoorsLocationDataSource")
                Toast.makeText(this, "Failed to start IndoorsLocationDataSource", Toast.LENGTH_LONG)
                    .show()
            }
            LocationDataSource.Status.STOPPED -> {
                progressBar.visibility = View.GONE
                stopLocationDisplay()
                Log.e(TAG, "IndoorsLocationDataSource stopped due to an internal error")
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
        if (ContextCompat.checkSelfPermission(
                this,
                requestPermissions[0]
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, requestPermissions, requestCode)
        } else {
            // permission already given, so no need to request
            connectToPortal()
        }
    }

    /**
     * Result of the user from location permissions request
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // if location permissions accepted, start setting up IndoorsLocationDataSource
            connectToPortal()
        } else {
            val message = "Location permission is not granted"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.e(TAG, message)
            progressBar.visibility = View.GONE
        }
    }

    internal interface ResultsCallback {
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
