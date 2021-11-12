/*
 * Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package com.esri.arcgisruntime.sample.authenticatewithoauth

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.authenticatewithoauth.databinding.ActivityMainBinding
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.esri.arcgisruntime.security.OAuthConfiguration
import java.net.MalformedURLException


class MainActivity : AppCompatActivity() {

  companion object {
    private val TAG: String = MainActivity::class.java.simpleName
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

    try {
      // set up an oauth config with url to portal, a client id and a re-direct url
      // a custom client id for your app can be set on the ArcGIS for Developers dashboard under
      // Authentication --> Redirect URIs
      val oAuthConfiguration = OAuthConfiguration(
        getString(R.string.portal_url),
        getString(R.string.oauth_client_id),
        getString(R.string.oauth_redirect_uri) + "://" + getString(R.string.oauth_redirect_host)
      )

      // setup AuthenticationManager to handle auth challenges
      val defaultAuthenticationChallengeHandler = DefaultAuthenticationChallengeHandler(this)

      // use the DefaultChallengeHandler to handle authentication challenges
      AuthenticationManager.setAuthenticationChallengeHandler(
        defaultAuthenticationChallengeHandler
      )

      // add an OAuth configuration
      // NOTE: you must add the DefaultOAuthIntentReceiver Activity to the app's manifest to handle starting a browser
      AuthenticationManager.addOAuthConfiguration(oAuthConfiguration)

      // load the portal and add the portal item as a map to the map view
      val portal = Portal(getString(R.string.portal_url))
      val portalItem = PortalItem(portal, getString(R.string.webmap_world_traffic_id))
      mapView.apply {
        map = ArcGISMap(portalItem)
      }
    } catch (e: MalformedURLException) {
      logError("Error in OAuthConfiguration URL: " + e.message)
    }
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    // normally, you won't want to clear credentials once a device has been verified. These calls are made to keep this
    // sample from interfering with other authentication samples
    AuthenticationManager.CredentialCache.clear()
    AuthenticationManager.clearOAuthConfigurations()

    mapView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }

  /**
   * Log an error to logcat and to the screen via Toast.
   * @param message the text to log.
   */
  private fun logError(message: String?) {
    message?.let {
      Log.e(TAG, message)
      Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
  }
}
