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

package com.esri.arcgisruntime.sample.togglebetweenfeaturerequestmodes

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.data.ServiceFeatureTable.FeatureRequestMode
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DrawStatus
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedEvent
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.togglebetweenfeaturerequestmodes.databinding.ActivityMainBinding
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger


class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val modeButton: Button by lazy {
        activityMainBinding.mode
    }

    private val populateButton: Button by lazy {
        activityMainBinding.populate
    }

    private val progressBar: ProgressBar by lazy {
        activityMainBinding.progressBar
    }

    private val labelTV: TextView by lazy {
        activityMainBinding.labelText
    }

    // instance of the FeatureLayer to be used by the map
    private var featureLayer: FeatureLayer? = null

    // currently selected feature mode: 0.Cache, 1.No cache, 2.Manual cache
    private var featureModeSelected: Int = 0

    // instance of the service feature table of street trees in Portland
    private var featureTable: ServiceFeatureTable? =
        ServiceFeatureTable("https://services2.arcgis.com/ZQgQTuoyBrtmoGdP/arcgis/rest/services/Trees_of_Portland/FeatureServer/0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is
        // required to access basemaps and other location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        mapView.apply {
            // set the map to be displayed in the layout's MapView
            map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)
            // set the starting viewpoint for the map view
            setViewpoint(Viewpoint(45.5266, -122.6219, 6000.0))
            // show a progress indicator when the map view is drawing (e.g. when fetching caches)
            addDrawStatusChangedListener { e: DrawStatusChangedEvent ->
                // true if DrawStatus is in progress
                //val drawStatusInProgress = e.drawStatus == DrawStatus.IN_PROGRESS
                // show ProgressBar if MapView is drawing and lock modeTV
                //progressBar.visibility = if (drawStatusInProgress) View.VISIBLE else View.GONE
                //modeButton.isEnabled = !drawStatusInProgress
            }
        }
        // create a feature layer from the service feature table
        featureLayer = FeatureLayer(featureTable)
        // set up the UI for switching between request modes
        setUpUi()
    }

    /**
     * Sets up the listeners for the UI when Mode or Populate views are clicked
     */
    private fun setUpUi() {
        // display feature mode options when the mode view is clicked
        modeButton.setOnClickListener {
            val featureModeChoices = arrayOf("Cache", "No cache", "Manual cache")
            // create an alert dialog and set up the options
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity).apply {
                setTitle("Choose a feature request mode")
                setSingleChoiceItems(
                    featureModeChoices, featureModeSelected
                ) { dialog, which ->
                    dialog.dismiss()
                    // update and set the current feature mode selected
                    featureModeSelected = which
                    setUpFeatureMode()
                }
            }
            // Displays the dialog
            val alert: AlertDialog = alertDialog.create()
            alert.setCancelable(false)
            alert.show()
        }
        // fetch cache manually when the populate button is clicked
        populateButton.setOnClickListener {
            fetchCacheManually()
        }
        // set label text on app launch
        labelTV.text = getString(R.string.labelDefaultText)
    }

    /**
     * Sets up the [featureLayer] to the [mapView] and updates the layer
     * to the selected feature request mode
     */
    private fun setUpFeatureMode() {
        // check if the feature layer has already been added to the map's operational layers, and if not, add it
        mapView.map.apply {
            if (operationalLayers.size == 0) {
                operationalLayers.add(featureLayer)
            }
        }
        // check the feature layer has loaded before setting the request mode of the feature table, selected from
        // the radio button's user data
        featureLayer?.addDoneLoadingListener {
            if (featureLayer?.loadStatus == LoadStatus.LOADED) {
                // set request mode of service feature table to selected toggle option
                featureTable?.featureRequestMode = getSelectedMode()
            } else {
                val error = "FeatureLayer failed to load" + featureLayer?.loadError?.message
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                Log.e(TAG, error)
            }
        }
    }

    /**
     * Fetches the cache from a Service Feature Table manually.
     */
    private fun fetchCacheManually() {
        // show loading ProgressBar when fetching manually
        progressBar.visibility = View.VISIBLE
        // create query to select all tree features
        val queryParams = QueryParameters().apply {
            // query for all tree conditions except "dead" with coded value '4' within the visible extent
            whereClause = "Condition < '4'"
            geometry = mapView.visibleArea.extent
        }

        // * means all features
        val outfields: List<String> = Collections.singletonList("*")

        // get queried features from service feature table and clear previous cache
        val tableResult = featureTable?.populateFromServiceAsync(queryParams, true, outfields)
        tableResult?.addDoneListener {
            try {
                // find the number of features returned from query
                val featuresReturned = AtomicInteger()
                tableResult.get().forEach { _ -> featuresReturned.getAndIncrement() }
                // display number of returned features to the user
                // note the service has a maximum record count of 2000
                labelTV.text = "Populated $featuresReturned features."
                // hide the loading ProgressBar
                progressBar.visibility = View.GONE
            } catch (e: Exception) {
                val error = "PopulateFromServiceAsync failed to load" + e.message
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                Log.e(TAG, error)
                // hide the loading ProgressBar
                progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * Updates the [labelTV] text and returns the selected
     * FeatureRequestMode using [featureModeSelected]
     */
    private fun getSelectedMode(): FeatureRequestMode {
        // enable populate view if request mode is manual cache
        populateButton.isEnabled = featureModeSelected == 2
        when (featureModeSelected) {
            0 -> {
                labelTV.text = getString(R.string.cacheEnabled)
                return FeatureRequestMode.ON_INTERACTION_CACHE
            }
            1 -> {
                labelTV.text = getString(R.string.noCacheEnabled)
                return FeatureRequestMode.ON_INTERACTION_NO_CACHE
            }
            2 -> {
                labelTV.text = getString(R.string.manualCacheEnabled)
                return FeatureRequestMode.MANUAL_CACHE
            }
        }
        return FeatureRequestMode.MANUAL_CACHE
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
