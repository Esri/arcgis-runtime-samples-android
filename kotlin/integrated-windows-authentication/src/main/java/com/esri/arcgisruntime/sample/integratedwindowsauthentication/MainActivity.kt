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

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.portal.PortalQueryParameters
import com.esri.arcgisruntime.security.AuthenticationChallenge
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.UserCredential
import com.esri.arcgisruntime.sample.integratedwindowsauthentication.databinding.ActivityMainBinding
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity(), AuthenticationChallengeHandler,
  PortalItemAdapter.OnItemClickListener,
  CredentialDialogFragment.OnCredentialDialogButtonClickListener,
  DialogInterface.OnDismissListener {

  private lateinit var portalItemAdapter: PortalItemAdapter

  private var userCredentials: MutableMap<String, UserCredential> = HashMap()

  // Instance of CountDownLatch used to block the thread that handles authentication
  private var authLatch: CountDownLatch? = null
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private lateinit var portal: Portal

  companion object {
    private val TAG: String = MainActivity::class.java.simpleName

    private val MAX_AUTH_ATTEMPTS = 5
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

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // Create a streets base map and set the map to the map view
    mapView.map = ArcGISMap(BasemapStyle.ARCGIS_STREETS)

    // Set authentication challenge handler
    AuthenticationManager.setAuthenticationChallengeHandler(this)

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
    portalLoadStateView.visibility = View.VISIBLE
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
              } catch (executionException: ExecutionException) {
                getString(R.string.error_item_set, executionException.message).let {
                  Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                  Log.e(TAG, it)
                }
              } catch (interruptedException: InterruptedException) {
                getString(R.string.error_item_set, interruptedException.message).let {
                  Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                  Log.e(TAG, it)
                }
              }
              // Hide portal load state
              portalLoadStateView.visibility = View.GONE
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
        portalLoadStateView.visibility = View.GONE
      }
    }

    // Load portal asynchronously
    portal.loadAsync()
  }

  /**
   * Handle sign in button click on CredentialDialogFragment
   *
   * @param uri the URI requiring credentials
   * @param username the username entered in the dialog
   * @param password the password entered in the dialog
   */
  override fun onSignInClicked(uri: URI, username: String, password: String) {
    uri.host?.let {
      userCredentials[it] = UserCredential(username, password)
    }
  }

  /**
   * Handle cancel button click on CredentialDialogFragment
   *
   * @param uri the URI requiring credentials
   */
  override fun onCancelClicked(uri: URI) {
    uri.host?.let {
      userCredentials.remove(it)
    }
  }

  override fun onDismiss(dialog: DialogInterface?) {
    // Countdown auth latch to unblock thread
    authLatch?.countDown()
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

  /**
   * When a user credential challenge is issued, pop up a dialog for user credential. When
   * the user credential has been set, respond with a AuthenticationChallengeResponse with
   * a continue with credential action and with the credentials as a parameter.
   *
   * @param authenticationChallenge
   */
  override fun handleChallenge(authenticationChallenge: AuthenticationChallenge?): AuthenticationChallengeResponse {
    if (authenticationChallenge?.type == AuthenticationChallenge.Type.USER_CREDENTIAL_CHALLENGE
      && authenticationChallenge.remoteResource is Portal
    ) {
      URI(authenticationChallenge.remoteResource.uri).host?.let { remoteResourceHost ->

        // If challenge has been requested by a Portal and the Portal has been loaded, cancel the challenge
        // This is required as some layers have private portal items associated with them and we don't
        // want to auth against them
        if ((authenticationChallenge.remoteResource as Portal).loadStatus == LoadStatus.LOADED) {
          return AuthenticationChallengeResponse(
            AuthenticationChallengeResponse.Action.CANCEL,
            authenticationChallenge
          )
        }

        // If we have not stored credentials against this host or an invalid credential was passed,
        // request them from the user
        authLatch = CountDownLatch(1)
        // Show the dialog fragment to request the user to enter their credentials
        CredentialDialogFragment.newInstance(URI(authenticationChallenge.remoteResource.uri)).show(
          supportFragmentManager,
          CredentialDialogFragment::class.java.simpleName
        )
        try {
          // Wait for dialog to dismiss to capture credentials
          authLatch?.await()
        } catch (e: InterruptedException) {
          getString(R.string.error_interruption, e.message).let {
            runOnUiThread {
              Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
            Log.e(TAG, it)
          }
        }
        // If the user has entered credentials, continue with those credentials
        return if (userCredentials[remoteResourceHost] != null) {
          AuthenticationChallengeResponse(
            AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
            userCredentials[remoteResourceHost]
          )
        } else {
          // No credentials were set, return a new auth challenge response with a cancel
          AuthenticationChallengeResponse(
            AuthenticationChallengeResponse.Action.CANCEL,
            authenticationChallenge
          )
        }
      }
    }

    // Return a new auth challenge response with a cancel for other challenge types or other remote resources.
    return AuthenticationChallengeResponse(
      AuthenticationChallengeResponse.Action.CANCEL,
      authenticationChallenge
    )
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
