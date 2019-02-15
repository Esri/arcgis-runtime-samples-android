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
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.portal.PortalQueryParameters
import com.esri.arcgisruntime.security.AuthenticationChallenge
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.AuthenticationManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.portal_info.*
import kotlinx.android.synthetic.main.portal_load_state.*
import java.net.URI
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity(), AuthenticationChallengeHandler, PortalItemAdapter.OnItemClickListener {

    private val logTag = MainActivity::class.java.simpleName

    private lateinit var portalItemAdapter: PortalItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create a streets base map and set the map to the map view
        mapView.map = ArcGISMap(Basemap.createStreets())

        // Set authentication challenge handler
        AuthenticationManager.setAuthenticationChallengeHandler(this)

        // Set up recycler view for listing portal items
        recyclerView.layoutManager = LinearLayoutManager(this)
        PortalItemAdapter(this).let {
            this.portalItemAdapter = it
            recyclerView.adapter = it
        }

        searchPublicButton.setOnClickListener {
            // Search the the public ArcGIS portal
            searchPortal(Portal(getString(R.string.arcgis_url)))
        }

        searchSecureButton.setOnClickListener {
            // Get the string entered for the secure portal URL.
            portalUrlEditText.text?.toString().let {
                // If the entered URL is a valid URL
                if (Patterns.WEB_URL.matcher(it).matches()) {
                    // Search an instance of the IWA-secured portal, the user may be challenged for access
                    searchPortal(Portal(it, true))
                } else {
                    getString(R.string.error_portal_url).let { errorString ->
                        Toast.makeText(this, errorString, Toast.LENGTH_LONG).show()
                        Log.e(logTag, errorString)
                    }
                }
            }
        }
    }

    private fun searchPortal(portal: Portal) {
        // Show portal load state during search
        portalLoadStateView.visibility = View.VISIBLE
        portalLoadStateTextView.text = getString(R.string.portal_load_state_searching, portal.uri)

        // Add Runnable to execute when Portal has finished loading
        portal.addDoneLoadingListener {
            if (portal.loadStatus == LoadStatus.LOADED) {
                // Update load state in UI with the portal URI
                portalLoadStateTextView.text = getString(R.string.portal_load_state_connected, URI(portal.uri).host)

                // Report the user name used for this connection.
                portal.user?.let {
                    portalLoadStateTextView.text = getString(
                            R.string.portal_user_connected,
                            if (it.username != null) it.username else getString(R.string.portal_user_anonymous)
                    )
                }

                // Search the portal for web maps
                portal.findItemsAsync(PortalQueryParameters("type:(\"web map\" NOT \"web mapping application\")"))?.let { portalItemResult ->
                    portalItemResult.addDoneListener {
                        try {
                            portalItemResult.get()?.results?.let { portalItemSetResults ->
                                portalItemAdapter.updatePortalItems(portalItemSetResults)
                            }
                        } catch (executionException: ExecutionException) {
                            getString(R.string.error_item_set, executionException.message).let {
                                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                                Log.e(logTag, it)
                            }
                        } catch (interruptedException: InterruptedException) {
                            getString(R.string.error_item_set, interruptedException.message).let {
                                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                                Log.e(logTag, it)
                            }
                        }
                    }
                }
            } else {
                // Report error
                portal.loadError?.let { loadError ->
                    (if (loadError.errorCode == 17) getString(R.string.error_portal_sign_in_cancelled) else
                        getString(R.string.error_portal_sign_in_failed, loadError.cause?.message)).let { errorString ->
                        Toast.makeText(this, errorString, Toast.LENGTH_LONG).show()
                        Log.e(logTag, errorString)
                    }
                }
            }

            // Hide portal load state
            portalLoadStateView.visibility = View.GONE
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
                Log.e(logTag, error)
            }
            return
        }
        // Create a map using the web map (portal item) and add it to the map view
        mapView.map = ArcGISMap(portalItem)
        // Show item ID in UI
        loadedWebMapTextView.text = getString(R.string.web_map_loaded_text, portalItem.itemId)
    }

    override fun handleChallenge(p0: AuthenticationChallenge?): AuthenticationChallengeResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPortalItemClick(portalItem: PortalItem) {
        addMap(portalItem);
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