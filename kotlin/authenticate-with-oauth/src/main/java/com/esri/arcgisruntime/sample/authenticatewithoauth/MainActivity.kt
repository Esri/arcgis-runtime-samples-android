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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.security.AuthenticationChallenge
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.OAuthConfiguration
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), AuthenticationChallengeHandler {

  companion object {
    private val TAG = MainActivity::class.java.simpleName
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    setupAuthenticationManager()

    // get the portal url for ArcGIS Online
    Portal(getString(R.string.portal_url)).run {
      // get the pre-defined portal id and portal url
      PortalItem(this, getString(R.string.webmap_world_traffic_id))
    }.let {
      // create a map from a PortalItem
      ArcGISMap(it)
    }.let {
      // set the map to be displayed in this view
      mapView.map = it
    }
  }

  private fun setupAuthenticationManager() {
    AuthenticationManager.setAuthenticationChallengeHandler(this)
    val configuration = OAuthConfiguration(
      getString(R.string.portal_url),
      "lgAdHkYZYlwwfAhC",
      "my-ags-app://auth"
    )

    AuthenticationManager.addOAuthConfiguration(configuration);
  }

  override fun handleChallenge(authenticationChallenge: AuthenticationChallenge?): AuthenticationChallengeResponse {
    try {
      // get config such as clientId from the authentication manager
      val config = AuthenticationManager.getOAuthConfiguration(authenticationChallenge?.remoteResource?.uri)

      // TODO
      /*// get the authorization code by sending user to the authorization screen
      val authorizationUrl = OAuthTokenCredentialRequest.getAuthorizationUrl(
        config.portalUrl, config.clientId, config.redirectUri, 0
      )
      val authorizationCode = OAuthChallenge.getAuthorizationCode(authorizationUrl)

      // use the authorization code to get a token
      val request = OAuthTokenCredentialRequest(
        config.portalUrl, null, config.clientId, null, authorizationCode
      )

      val credential = request.executeAsync().get()
      return AuthenticationChallengeResponse(
        AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
        credential
      )*/
      return AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, null)
    } catch (e: Exception) {
      return AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, null)
    }

  }
}