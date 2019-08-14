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

import android.content.Intent
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
import java.util.concurrent.CountDownLatch

/**
 * This sample demonstrates how to authenticate with ArcGIS Online (or your own portal) using OAuth2 to access secured
 * resources (such as private web maps or layers). Accessing secured items requires a login on the portal that hosts them
 * (an ArcGIS Online account, for example). This sample utilizes Android WebView to show the OAuth sign-in page in a dialog.
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
      "${getString(R.string.oauth_redirect_scheme)}://${getString(R.string.oauth_redirect_host)}"
    )
  }

  // instances of Portal and PortalItem to define what is displayed in the map
  private val portal: Portal by lazy { Portal(getString(R.string.portal_url)) }
  private val portalItem: PortalItem by lazy { PortalItem(portal, getString(R.string.webmap_world_traffic_id)) }

  // auth code stored as a property as auth code isn't obtainable from intent when called from unblocked thread
  private var authCode: String? = null
  // CountDownLatch used to block auth challenge when performing oauth authorization
  private var authCountDownLatch: CountDownLatch? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    mapView.map = ArcGISMap(portalItem)

    // setup AuthenticationManager to handle auth challenges
    AuthenticationManager.setAuthenticationChallengeHandler(this)
    AuthenticationManager.addOAuthConfiguration(oAuthConfig)
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)

    // check the intent for an auth code
    intent?.authCode?.let {
      authCode = it
    }
  }

  override fun onResume() {
    super.onResume()

    // hide WebView if it is visible
    if (webView.visibility == View.VISIBLE) {
      webView.visibility = View.GONE
    }

    // count down the latch to allow the auth challenge thread to continue
    authCountDownLatch?.countDown()

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

  /**
   * Function for handling authentication challenges.
   *
   * We launch the Intent to open the user's browser or as a last resort we use a WebView. Directly after
   * launching the Intent or opening the WebView we block the auth challenge thread to allow the user to enter their
   * credentials.
   *
   * We release the thread when the Activity is resumed. If we receive an auth code in the intent during onNewIntent(),
   * we perform a credential request using this auth code, otherwise we cancel the auth challenge.
   *
   * @param authenticationChallenge the authentication challenge to handle
   * @return the AuthenticationChallengeResponse indicating which action to take
   */
  override fun handleChallenge(authenticationChallenge: AuthenticationChallenge?): AuthenticationChallengeResponse {
    authenticationChallenge?.let { authChallenge ->

      if (authChallenge.type == AuthenticationChallenge.Type.OAUTH_CREDENTIAL_CHALLENGE) {

        beginOAuth()

        authCountDownLatch = CountDownLatch(1)
        authCountDownLatch?.await()

        // if we have an auth code, we've likely just been through the OAuth flow and now have an auth code
        // we can use it to request a new access token
        if (authCode != null) {
          try {
            // use the authorization code to get a new access token by executing an OAuthTokenCredentialRequest
            val request = OAuthTokenCredentialRequest(
              oAuthConfig.portalUrl,
              null,
              oAuthConfig.clientId,
              oAuthConfig.redirectUri,
              authCode
            )

            // clear stored auth code after usage
            authCode = null

            val credential = request.executeAsync().get()
            // continue with credentials generated using auth code
            return AuthenticationChallengeResponse(
              AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
              credential
            )
          } catch (e: Exception) {
            runOnUiThread {
              logToUser(getString(R.string.error_auth_exception, e.message))
            }
          }
        } else {
          return AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, null)
        }
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

  /**
   * Sets up the WebView.
   *
   * Please note that we have chosen to include use of the WebView as a fallback mechanism if the user does not have a
   * web browser installed, only as a matter of course to allow you to use this sample in all circumstances. We do not
   * recommend using WebView as part of your OAuth flow as per IETF's recommendations:
   * https://tools.ietf.org/html/rfc8252
   */
  private fun setupWebView() {
    // setup a WebViewClient to override handling of custom scheme and host for Intent
    webView.webViewClient = object : WebViewClient() {
      override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        Uri.parse(url)?.let {
          if (it.scheme == getString(R.string.oauth_redirect_scheme) && it.host == getString(R.string.oauth_redirect_host)) {
            startActivity(generateAuthIntent(it))
            return true
          }
        }
        return super.shouldOverrideUrlLoading(view, url)
      }

      @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
      override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.url?.let { url ->
          if (url.scheme == getString(R.string.oauth_redirect_scheme) && url.host == getString(R.string.oauth_redirect_host)) {
            startActivity(generateAuthIntent(url))
            return true
          }
        }
        return super.shouldOverrideUrlLoading(view, request)
      }
    }

    // enabled to allow javascript to run on auth webpage.
    webView.settings.javaScriptEnabled = true
    webView.visibility = View.VISIBLE
  }

  /**
   * Generate the Intent that launches the MainActivity when the OAuth portal redirects using the registered scheme and
   * host.
   *
   * @param uri instance of Uri generated from URL that OAuth webpage redirects to after successful login
   */
  private fun generateAuthIntent(uri: Uri): Intent {
    return Intent().also {
      // sets a flag to define the following behaviour: if the MainActivity activity being launched is already running
      // in the current task, then instead of launching a new instance of that activity, all of the other activities on
      // top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
      it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      // sets a flag to define the following behaviour: if a task is already running for the MainActivity you are now
      // starting, then a new activity will not be started; instead, the current task will simply be brought to the
      // front of the screen with the state it was last in.
      it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      it.data = uri
    }
  }

}

/**
 * AppCompatActivity extensions
 */
fun AppCompatActivity.logToUser(logMessage: String) {
  Log.d(this::class.java.simpleName, logMessage)
  Toast.makeText(this, logMessage, Toast.LENGTH_LONG).show()
}

/**
 * Intent extensions
 */
val Intent.authCode: String?
  get() = this.data?.getQueryParameter("code")
