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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ArcGISFeatureTable
import com.esri.arcgisruntime.data.FeatureTable
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.location.IndoorsLocationDataSource
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.showdevicelocationusingindoorpositioning.databinding.ActivityMainBinding
import com.esri.arcgisruntime.security.UserCredential


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    // Provides an indoor or outdoor position based on device sensor data (radio, GPS, motion sensors).
    private var mIndoorsLocationDataSource: IndoorsLocationDataSource? = null

    private val mPortal = Portal("https://ips4u.mapsdevext.arcgis.com", true)

    private val mCredential = UserCredential("mobile_testers", "apptesting.mobile1234")

    private val MAP_ID = "47fe2531c0694624a2fa3b02fb2b2cf3"

    private val DETAILS_LAYER = "Details"

    private val UNITS_LAYER = "Units"

    private val LEVELS_LAYER = "Levels"

    private val VERTICAL_ORDER_EXPRESSION = "VERTICAL_ORDER = "

    // Name of the positioning table saved in the feature service.
    private val IPS_POSITIONING_TABLE_NAME = "ips_positioning"

    // Name of the pathways table saved in the feature service.
    private val PATHWAYS_TABLE_NAME = "Pathways"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other location services
        //ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        checkPermissions()
    }

    private fun connectToPortal() {
        mPortal.credential = mCredential
        mPortal.addDoneLoadingListener {
            if(mPortal.loadStatus == LoadStatus.LOADED){
                val portalItem = PortalItem(mPortal, MAP_ID)
                setupMap(portalItem)
            }else{
                Toast.makeText(this, mPortal.loadError.message, Toast.LENGTH_SHORT).show()
                Log.e(TAG,mPortal.loadError.message.toString())
            }
        }
        mPortal.loadAsync()
    }

    private fun setupMap(portalItem: PortalItem) {
        mapView.apply {
            map = ArcGISMap(portalItem)

            map.addDoneLoadingListener {
                if (map.loadStatus == LoadStatus.LOADED){
                    val featureTables = map.tables
                    if(featureTables.isEmpty()){
                        val message = "Map does not contain feature tables"
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                        Log.e(TAG,message)
                        return@addDoneLoadingListener;
                    }
                    loadTables(featureTables, object : ResultsCallback {
                        override fun onSuccess() {
                            setupIndoorsLocationDataSource(featureTables, setupResultCompletionHandler)
                        }

                        override fun onError(exception: Exception?) {
                            val message = "Failed to load feature tables"
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                            Log.e(TAG,message)
                        }
                    })
                }else{
                    val error = map.loadError
                    Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
                }

            }
            map.loadAsync()
        }
    }

    private fun loadTables(featureTables: MutableList<FeatureTable>, resultsCallback: ResultsCallback) {
        val iterator = featureTables.iterator()
        if(iterator.hasNext()){
            val table = iterator.next()
            table.addDoneLoadingListener {
                if (table.loadStatus == LoadStatus.LOADED) {
                    featureTables.removeAt(0)
                    loadTables(featureTables, resultsCallback)
                } else {
                    resultsCallback.onError(table.loadError)
                }
            }
            table.loadAsync()
        }else{
            resultsCallback.onSuccess()
        }
    }

    private fun setupIndoorsLocationDataSource(featureTables: List<FeatureTable>, resultsCallback: ResultsCallback) {
        // PositioningTable needs to be present
        var positioningTable: FeatureTable? = null
        for(featureTable in featureTables){
            if(featureTable.tableName.equals(IPS_POSITIONING_TABLE_NAME)){
                positioningTable = featureTable
                continue
            }
        }

        if (positioningTable != null){
            val serviceFeatureTable = positioningTable as ServiceFeatureTable
            // Standard way to initialize IndoorsLocationDataSource
            // When multiple entries are available, IndoorsLocationDataSource constructor function
            // looks up the entry with the most recent date and takes this positioning data.
            mIndoorsLocationDataSource = IndoorsLocationDataSource(this, serviceFeatureTable, getPathwaysTable())
            resultsCallback.onSuccess()

        }
    }

    private fun getPathwaysTable(): ArcGISFeatureTable? {
        var pathwaysLayer: FeatureLayer? = null
        for(layer in mapView.map.operationalLayers){
            if(layer.name.equals(PATHWAYS_TABLE_NAME)){
                pathwaysLayer = layer as FeatureLayer?
            }
        }

        return if (pathwaysLayer != null) {
            pathwaysLayer.featureTable as ArcGISFeatureTable
        }else
            null
    }

    private fun startLocationDisplay() {
        TODO("Not yet implemented")
    }


    private fun checkPermissions() {
        val requestCode = 1
        val requestPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (ContextCompat.checkSelfPermission(this, requestPermissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, requestPermissions, requestCode)
        } else {
            connectToPortal()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        mapView.dispose()
        super.onDestroy()
    }
}
