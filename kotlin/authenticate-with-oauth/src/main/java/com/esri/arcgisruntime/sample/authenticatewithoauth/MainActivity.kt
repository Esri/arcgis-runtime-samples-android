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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsClient
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsServiceConnection
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.security.AuthenticationChallenge
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.OAuthConfiguration
import com.esri.arcgisruntime.security.OAuthTokenCredentialRequest
import com.esri.arcgisruntime.security.UserCredential
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
      getString(R.string.portal_url),
      getString(R.string.oauth_client_id),
      getString(R.string.oauth_redirect_uri)
    )

    AuthenticationManager.setAuthenticationChallengeHandler(this)
    AuthenticationManager.addOAuthConfiguration(oAuthConfig)

    portal = Portal(getString(R.string.portal_url))
    portalItem = PortalItem(portal, getString(R.string.webmap_world_traffic_id))
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()

    handleIntent(intent)

    val map = ArcGISMap(portalItem)
    mapView.map = map
  }

  private fun handleIntent(intent: Intent?): Boolean {
    intent?.authCode?.let { code ->
      sharedPreferences.putAuthCode(code)
    }
    return false
  }

  override fun handleChallenge(authenticationChallenge: AuthenticationChallenge?): AuthenticationChallengeResponse {
    authenticationChallenge?.let { authChallenge ->

      Log.d(TAG, "New Auth Challenge: ${authChallenge.type}")
      try {
        sharedPreferences.accessToken?.let { accessToken ->
          if (authChallenge.type == AuthenticationChallenge.Type.USER_CREDENTIAL_CHALLENGE) {
            // check for expiration of token
            sharedPreferences.accessTokenExpiry.let {
              Log.d(TAG, "Access Token Expiry: $it")
              if (it in 1 until System.currentTimeMillis()) {
                sharedPreferences.clearAccessToken()
                beginOAuth()
                return AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, null)
              }
            }
          }

          if (authChallenge.type == AuthenticationChallenge.Type.OAUTH_CREDENTIAL_CHALLENGE) {
            return AuthenticationChallengeResponse(
              AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
              UserCredential.createFromToken(accessToken, authChallenge.remoteResource?.uri)
            )
          }
        }

        sharedPreferences.authCode?.let {
          Log.d(TAG, "Use existing auth code")
          // use the authorization code to get a token
          val request = OAuthTokenCredentialRequest(
            oAuthConfig.portalUrl,
            null,
            oAuthConfig.clientId,
            oAuthConfig.redirectUri,
            sharedPreferences.authCode
          )

          val credential = request.executeAsync().get()
          Log.d(TAG, "Credential expiry: ${credential.expiresIn}")
          with(sharedPreferences) {
            putAccessToken(credential.accessToken)
            putAccessTokenExpiry(System.currentTimeMillis() + (1 * 60))
            clearAuthCode()
          }
          return AuthenticationChallengeResponse(
            AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
            credential
          )
        }

        // we only want to begin OAuth here if the user has yet to authorize successfully
        if (authChallenge.failureCount < 2) {
          beginOAuth()
        }

        return AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, null)
      } catch (e: Exception) {
        getString(R.string.error_auth_exception, e.message).let {
          Log.d(TAG, it)
          runOnUiThread {
            logToUser(it)
          }
        }
      }
    }
    return AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, null)
  }

  private fun beginOAuth() {
    Log.d(TAG, "Get new auth code")
    // get the authorization code by sending user to the authorization screen
    val authorizationUrl = OAuthTokenCredentialRequest.getAuthorizationUrl(
      oAuthConfig.portalUrl, oAuthConfig.clientId, oAuthConfig.redirectUri, 1
    )

    if (CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(p0: ComponentName?, p1: CustomTabsClient?) {
          logToUser("Connected to Custom Tabs Service")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
          logToUser("Disconnected from Custom Tabs Service")
        }
      })) {
      launchChromeTab(authorizationUrl)
    }
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


/**
 * AppCompatActivity extensions
 */
val AppCompatActivity.sharedPreferences: SharedPreferences
  get() = this.getSharedPreferences("${this::class.java.simpleName}_shared_prefs", Context.MODE_PRIVATE)

fun AppCompatActivity.logToUser(logMessage: String) {
  Log.d(this::class.java.simpleName, logMessage)
  Toast.makeText(this, logMessage, Toast.LENGTH_LONG).show()
}

/**
 * SharedPreferences extensions
 */
fun SharedPreferences.putAuthCode(authCode: String) {
  this.edit().putString("auth_code", authCode)
    .apply()
}

val SharedPreferences.authCode: String?
  get() = this.getString("auth_code", null)

fun SharedPreferences.clearAuthCode() {
  this.edit().remove("auth_code")
    .apply()
}

val SharedPreferences.accessToken: String?
  get() = this.getString("access_token", null)

fun SharedPreferences.putAccessToken(accessToken: String) {
  this.edit().putString("access_token", accessToken)
    .apply()
}

fun SharedPreferences.clearAccessToken() {
  this.edit().remove("access_token")
    .apply()
  this.clearAccessTokenExpiry()
}

val SharedPreferences.accessTokenExpiry: Long
  get() = this.getLong("access_token_expiry", 0)

fun SharedPreferences.putAccessTokenExpiry(timeInMillis: Long) {
  this.edit().putLong("access_token_expiry", timeInMillis)
    .apply()
}

fun SharedPreferences.clearAccessTokenExpiry() {
  this.edit().remove("access_token_expiry")
    .apply()
}

/**
 * Intent extensions
 */
val Intent.authCode: String?
  get() = this.data?.getQueryParameter("code")