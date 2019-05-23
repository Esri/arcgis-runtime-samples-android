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
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
import kotlinx.android.synthetic.main.activity_main.*

/**
 * This sample demonstrates how to authenticate with ArcGIS Online (or your own portal) using OAuth2 to access secured
 * resources (such as private web maps or layers). Accessing secured items requires a login on the portal that hosts them
 * (an ArcGIS Online account, for example). This sample utilizes Android WebView to show theOAuth sign-in page in a dialog.
 */
class MainActivity : AppCompatActivity(), AuthenticationChallengeHandler {

  companion object {
    private val TAG = MainActivity::class.java.simpleName
  }

  // define configuration for OAuth Portal using custom redirect URL to receive code after auth has been granted
  private val oAuthConfig: OAuthConfiguration by lazy {
    OAuthConfiguration(
      getString(R.string.portal_url),
      getString(R.string.oauth_client_id),
      "${BuildConfig.APPLICATION_ID}://${getString(R.string.oauth_redirect_host)}"
    )
  }

  // instances of Portal and PortalItem to define what is displayed in the map
  private val portal: Portal by lazy { Portal(getString(R.string.portal_url)) }

  private val portalItem: PortalItem by lazy { PortalItem(portal, getString(R.string.webmap_world_traffic_id)) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // setup AuthenticationManager to handle auth challenges
    AuthenticationManager.setAuthenticationChallengeHandler(this)
    AuthenticationManager.addOAuthConfiguration(oAuthConfig)
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()

    handleIntent(intent)

    mapView.map = ArcGISMap(portalItem)
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }

  /**
   * Attempt to handle the Intent received by the Activity
   * If the Intent contains an authorization code, store this in the SharedPreferences
   *
   * @param intent the Intent to handle
   */
  private fun handleIntent(intent: Intent?) {
    intent?.authCode?.let { code ->
      sharedPreferences.putAuthCode(code)
    }
  }

  /**
   * Function for handling authentication challenges.
   *
   * If an authorization code exists in the SharedPreferences, it's likely that the user is partially through the OAuth flow
   * and we need to try to obtain the token by performing a OAuthTokenCredentialRequest.
   *
   * If there is not an authorization code in the SharedPreferences, it's likely that this is the user's first attempt
   * at OAuth. So begin OAuth flow.
   *
   * @param authenticationChallenge the authentication challenge to handle
   * @return the AuthenticationChallengeResponse indicating which action to take
   */
  override fun handleChallenge(authenticationChallenge: AuthenticationChallenge?): AuthenticationChallengeResponse {
    authenticationChallenge?.let { authChallenge ->

      try {
        // if SharedPreferences has an auth code, we've likely just been through the OAuth flow and now have an auth code
        // we can use to request a new access token
        sharedPreferences.authCode?.let {
          // use the authorization code to get a new access token by executing an OAuthTokenCredentialRequest
          val request = OAuthTokenCredentialRequest(
            oAuthConfig.portalUrl,
            null,
            oAuthConfig.clientId,
            oAuthConfig.redirectUri,
            it
          )

          // we've used the auth code to obtain an access token, so clear it
          sharedPreferences.clearAuthCode()
          val credential = request.executeAsync().get()
          // continue with credentials generated using auth code
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
        // auth code has likely expired, clear existing auth code and begin OAuth flow
        getString(R.string.error_auth_exception, e.message).let {
          Log.d(TAG, it)
          runOnUiThread {
            logToUser(it)
          }
        }
        sharedPreferences.clearAuthCode()
        beginOAuth()
      }
    }
    return AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, null)
  }

  private fun beginOAuth() {
    // get the authorization code by sending user to the authorization screen
    val authorizationUrl = OAuthTokenCredentialRequest.getAuthorizationUrl(
      oAuthConfig.portalUrl,
      oAuthConfig.clientId,
      oAuthConfig.redirectUri,
      0
    )

    // create an Intent to attempt to handle the authorization URL
    with(Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))) {
      this.resolveActivity(packageManager)?.let {
        // this identifier ensures that the browser will attempt to reuse the same window each time the application
        // launches the browser with the same identifier, which in this case is static and will always only be our
        // application ID
        this.putExtra(Browser.EXTRA_APPLICATION_ID, BuildConfig.APPLICATION_ID)
        startActivity(this)
        return
      }
    }

    // user doesn't have a browser available to handle the Intent so we use a WebView to handle OAuth. WebView methods
    // must be called on UI thread
    runOnUiThread {
      setupWebView()
      webView.loadUrl(authorizationUrl)
    }
  }

  private fun setupWebView() {
    // setup a WebViewClient to override handling of custom scheme and host for Intent
    webView.webViewClient = object : WebViewClient() {
      override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        Uri.parse(url)?.let {
          if (it.scheme == BuildConfig.APPLICATION_ID && it.host == getString(R.string.oauth_redirect_host)) {
            startActivity(generateAuthIntent(it))
            return true
          }
        }
        return super.shouldOverrideUrlLoading(view, url)
      }

      @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
      override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request?.url?.scheme == BuildConfig.APPLICATION_ID && request.url?.host == getString(R.string.oauth_redirect_host)) {
          startActivity(generateAuthIntent(request.url))
          return true
        }
        return super.shouldOverrideUrlLoading(view, request)
      }
    }

    // enabled to allow javascript to run on auth webpage.
    webView.settings.javaScriptEnabled = true
    webView.visibility = View.VISIBLE
  }

  /**
   * Generate the Intent that launches the MainActivity with the new auth code
   *
   * @param uri instance of Uri generated from URL that OAuth webpage redirects to after successful login
   */
  private fun generateAuthIntent(uri: Uri): Intent {
    return Intent().also {
      it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      it.data = uri
    }
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

/**
 * Intent extensions
 */
val Intent.authCode: String?
  get() = this.data?.getQueryParameter("code")