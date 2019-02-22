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

package com.esri.arcgisruntime.sample.authenticatewithoauth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.esri.arcgisruntime.io.JsonEmbeddedException
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.security.AuthenticationChallenge
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.OAuthConfiguration
import com.esri.arcgisruntime.security.OAuthTokenCredentialRequest
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), AuthenticationChallengeHandler {

  companion object {
    private val TAG = MainActivity::class.java.simpleName
  }

  private lateinit var oAuthConfig: OAuthConfiguration

  private lateinit var portal: Portal

  private lateinit var portalItem: PortalItem

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    oAuthConfig = OAuthConfiguration(
      getString(R.string.oauth_url),
      getString(R.string.oauth_client_id),
      getString(R.string.oauth_redirect_uri)
    )

    AuthenticationManager.setAuthenticationChallengeHandler(this)

    portal = Portal(getString(R.string.portal_url))
    portalItem = PortalItem(portal, getString(R.string.webmap_world_traffic_id))

    portalItem.addLoadStatusChangedListener {
      Log.d(TAG, "Portal Item Load Status: ${it.newLoadStatus}")
    }
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()

    handleIntent(intent)

    val map = ArcGISMap(portalItem)
    mapView.map = map
  }

  private fun handleIntent(intent: Intent?): Boolean {
    intent?.data?.getQueryParameter("code")?.let { code ->
      getSharedPreferences("shared_prefs", Context.MODE_PRIVATE)
        .edit()
        .putString("auth_code", code)
        .apply()
      return true
    }
    return false
  }

  override fun handleChallenge(authenticationChallenge: AuthenticationChallenge?): AuthenticationChallengeResponse {
    Log.d(TAG, "New Auth Challenge: ${authenticationChallenge?.type}")
    try {
      if (!getSharedPreferences("shared_prefs", Context.MODE_PRIVATE).contains("code")) {
        getNewAuthCode()
        return AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, null)
      } else {
        Log.d(TAG, "Use existing auth code")
        // use the authorization code to get a token
        val request = OAuthTokenCredentialRequest(
          oAuthConfig.portalUrl,
          null,
          oAuthConfig.clientId,
          oAuthConfig.redirectUri,
          getSharedPreferences("shared_prefs", Context.MODE_PRIVATE)
            .getString("code", "")
        )

        val credential = request.executeAsync().get()
        Log.d(TAG, "Credential expiry: ${credential.expiresIn}")
        return AuthenticationChallengeResponse(
          AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
          credential
        )
      }
    } catch (e: Exception) {
      getString(R.string.error_auth_exception, e.message).let {
        Log.d(TAG, it)
        runOnUiThread {
          Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
      }
      (e.cause as? JsonEmbeddedException)?.let {
        if (it.code == 400) {
          getNewAuthCode()
        }
      }
      return AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, null)
    }
  }

  private fun getNewAuthCode() {
    Log.d(TAG, "Get new auth code")
    // get the authorization code by sending user to the authorization screen
    val authorizationUrl = OAuthTokenCredentialRequest.getAuthorizationUrl(
      oAuthConfig.portalUrl, oAuthConfig.clientId, oAuthConfig.redirectUri, 20160
    )

    launchChromeTab(authorizationUrl)
  }

  private fun launchChromeTab(uri: String) {
    CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(uri))
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