/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.integratedwindowsauthentication

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.portal.PortalQueryParameters
import com.esri.arcgisruntime.sample.integratedwindowsauthentication.databinding.ActivityMainBinding
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import java.net.URI

class MainActivity : AppCompatActivity(),
    PortalItemAdapter.OnItemClickListener {

    private lateinit var portalItemAdapter: PortalItemAdapter

    // objects that implement Loadable must be class fields to prevent being garbage collected before loading
    private lateinit var portal: Portal


    private val TAG: String = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val searchPublicButton: Button by lazy {
        activityMainBinding.portalInfo.searchPublicButton
    }

    private val searchSecureButton: Button by lazy {
        activityMainBinding.portalInfo.searchSecureButton
    }

    private val portalUrlEditText: EditText by lazy {
        activityMainBinding.portalInfo.portalUrlEditText
    }

    private val recyclerView: RecyclerView by lazy {
        activityMainBinding.portalInfo.recyclerView
    }

    private val loadedWebMapTextView: TextView by lazy {
        activityMainBinding.portalInfo.loadedWebMapTextView
    }

    private val portalLoadStateTextView: TextView by lazy {
        activityMainBinding.portalInfo.portalLoadStateView.portalLoadStateTextView
    }

    private val portalLoadStateLayout: ConstraintLayout by lazy {
        activityMainBinding.portalInfo.portalLoadStateView.portalLoadStateLayout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // Create a streets base map and set the map to the map view
        mapView.map = ArcGISMap(BasemapStyle.ARCGIS_STREETS)

        // Set authentication challenge handler
        AuthenticationManager.setAuthenticationChallengeHandler(
            DefaultAuthenticationChallengeHandler(this)
        )

        // Set up recycler view for listing portal items
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        PortalItemAdapter(this).let {
            this.portalItemAdapter = it
            recyclerView.adapter = it
        }

        searchPublicButton.setOnClickListener {
            portal = Portal(getString(R.string.arcgis_url))
            // Search the the public ArcGIS portal
            searchPortal(portal)
        }

        searchSecureButton.setOnClickListener {
            // Get the string entered for the secure portal URL.
            portalUrlEditText.text?.toString()?.let {
                // If the entered URL is a valid URL
                if (Patterns.WEB_URL.matcher(it).matches()) {
                    portal = Portal(portalUrlEditText.text.toString(), true)
                    searchPortal(portal)
                } else {
                    getString(R.string.error_portal_url).let { errorString ->
                        Toast.makeText(this, errorString, Toast.LENGTH_LONG).show()
                        Log.e(TAG, errorString)
                    }
                }
            }
        }
    }

    private fun searchPortal(portal: Portal) {
        // Hide portal list during search
        recyclerView.visibility = View.INVISIBLE

        // Show portal load state during search
        portalLoadStateLayout.visibility = View.VISIBLE
        portalLoadStateTextView.text = getString(R.string.portal_load_state_searching, portal.uri)

        // Add Runnable to execute when Portal has finished loading
        portal.addDoneLoadingListener {
            if (portal.loadStatus == LoadStatus.LOADED) {
                // Update load state in UI with the portal URI
                portalLoadStateTextView.text =
                    getString(R.string.portal_load_state_connected, URI(portal.uri).host)

                // Report the user name used for this connection.
                portal.user?.let {
                    portalLoadStateTextView.text = getString(
                        R.string.portal_user_connected,
                        if (it.username != null) it.username else getString(R.string.portal_user_anonymous)
                    )
                }

                // Search the portal for web maps
                portal.findItemsAsync(PortalQueryParameters("type:(\"web map\" NOT \"web mapping application\")"))
                    ?.let { portalItemResult ->
                        portalItemResult.addDoneListener {
                            try {
                                portalItemResult.get()?.results?.let { portalItemSetResults ->
                                    portalItemAdapter.updatePortalItems(portalItemSetResults)
                                }
                            } catch (exception: Exception) {
                                getString(
                                    R.string.error_item_set, exception.message
                                ).let {
                                    Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                                    Log.e(TAG, it)
                                }
                            }
                            // Hide portal load state
                            portalLoadStateLayout.visibility = View.GONE
                            // Show portal list
                            recyclerView.visibility = View.VISIBLE
                        }
                    }
            } else {
                // Report error
                portal.loadError?.let { loadError ->
                    (getString(
                        R.string.error_portal_sign_in_failed,
                        loadError.cause?.message
                    )).let { errorString ->
                        Toast.makeText(this, errorString, Toast.LENGTH_LONG).show()
                        Log.e(TAG, errorString)
                    }
                }
                // Hide portal load state
                portalLoadStateLayout.visibility = View.GONE
            }
        }

        // Load portal asynchronously
        portal.loadAsync()
    }

    /**

     * Add the given portal item to a new map and set the map to the map view.
     *
     * @param portalItem
     */
    private fun addMap(portalItem: PortalItem) {
        // Report error and return if portal is null
        if (portalItem.portal == null) {
            getString(R.string.error_portal_not_instantiated).let { error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                Log.e(TAG, error)
            }
            return
        }
        // Create a map using the web map (portal item) and add it to the map view
        mapView.map = ArcGISMap(portalItem)
        // Show item ID in UI
        loadedWebMapTextView.text = getString(R.string.web_map_loaded_text, portalItem.itemId)
    }


    override fun onPortalItemClick(portalItem: PortalItem) {
        addMap(portalItem)
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
