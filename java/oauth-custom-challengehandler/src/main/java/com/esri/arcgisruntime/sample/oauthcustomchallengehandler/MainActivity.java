/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.oauthcustomchallengehandler;

import java.net.MalformedURLException;
import java.util.concurrent.CountDownLatch;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.AuthenticationChallenge;
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.OAuthConfiguration;
import com.esri.arcgisruntime.security.OAuthTokenCredential;
import com.esri.arcgisruntime.security.OAuthTokenCredentialRequest;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  private static OAuthChallengeCountDownLatch sOAuthChallengeCountDownLatch;

  private OAuthConfiguration mOAuthConfiguration = null;

  private Dialog mOAuthLoginDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // set custom challenge handler on the AuthenticationManager
    AuthenticationManager.setAuthenticationChallengeHandler(customAuthenticationChallengeHandler);

    // initialize the dialog to display the OAuth login page
    mOAuthLoginDialog = new Dialog(this);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);

    // add the OAuth configuration to the AuthenticationManager
    try {
      OAuthConfiguration config = new OAuthConfiguration(getString(R.string.portal_url), getString(R.string.client_id), getString(R.string.redirect_url));
      AuthenticationManager.addOAuthConfiguration(config);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e.getMessage(), e.getCause());
    }

    // create a portal instance for the portal to load
    Portal portal = new Portal(getString(R.string.portal_url));

    // create a portal item with the itemId of the web map
    PortalItem webMapItem = new PortalItem(portal, getString(R.string.webmap_id));

    // create a map with the portal item
    ArcGISMap map = new ArcGISMap(webMapItem);

    // set the map to the map view
    mMapView.setMap(map);
  }

  /**
   * Anonymous class to define our customAuthenticationChallengeHandler.
   */
  AuthenticationChallengeHandler customAuthenticationChallengeHandler = new AuthenticationChallengeHandler() {
    /**
     * Handles the incoming AuthenticationChallenge, returning a response that contains an action and
     * potentially a parameter with which to carry out the action.
     *
     * @param challenge the authentication challenge to handle
     * @return the response that should be taken
     */
    @Override
    public AuthenticationChallengeResponse handleChallenge(AuthenticationChallenge challenge) {

      AuthenticationChallengeResponse ret = null;

      if (challenge.getType() == AuthenticationChallenge.Type.OAUTH_CREDENTIAL_CHALLENGE) {
        sOAuthChallengeCountDownLatch = new OAuthChallengeCountDownLatch(1);

        final String portalUrl = challenge.getRemoteResource().getUri();

        // get the OAuth config for the portalUrl associated with the challenge
        try {
          mOAuthConfiguration = AuthenticationManager.getOAuthConfiguration(portalUrl);
        } catch (MalformedURLException e) {
          throw new RuntimeException(e.getMessage(), e.getCause());
        }

        // get the authorization url which loads the OAuth login page to display in the dialog
        final String url = OAuthTokenCredentialRequest
            .getAuthorizationUrl(portalUrl, mOAuthConfiguration.getClientId(), mOAuthConfiguration.getRedirectUri(), 0);

        runOnUiThread(new Runnable() {
          @Override public void run() {

            // setup webview
            WebView webView = new WebView(MainActivity.this);
            webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
            WebSettings webSettings = webView.getSettings();
            webSettings.setBuiltInZoomControls(true);
            webSettings.setJavaScriptEnabled(true);

            // setup webviewClient
            CustomWebViewClient customWebViewClient = new CustomWebViewClient(portalUrl,
                mOAuthConfiguration.getClientId(), mOAuthConfiguration.getRedirectUri());
            webView.setWebViewClient(customWebViewClient);

            webView.loadUrl(url);
            // set the webview on the dialog
            mOAuthLoginDialog.setContentView(webView);
            // display the dialog
            mOAuthLoginDialog.show();
          }
        });

        try {
          // Wait for the OAuth browser page to be shown and user to login and for the CustomWebViewClient
          // to get the auth code and request an OAuthTokenCredential with it
          sOAuthChallengeCountDownLatch.await();
        } catch (InterruptedException ie) {
        }
        // Get the credential from the latch once it has been counted down
        OAuthTokenCredential credential = sOAuthChallengeCountDownLatch.getOAuthTokenCredential();
        if (credential != null) {
          ret = new AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
              credential);
        }
        // Reset the latch for the next challenge
        sOAuthChallengeCountDownLatch = null;
        // dismiss the dialog
        mOAuthLoginDialog.dismiss();
      }
      return ret;
    }
  };

  /**
   * CustomWebViewClient overrides the shouldOverrideUrlLoading method to handle errors on the login page and to request
   * OAuthTokenCredential after having received the auth code when the user provides the login credential/
   */
  private class CustomWebViewClient extends WebViewClient {

    private final String mPortal;

    private final String mClientId;

    private final String mRedirectUri;

    CustomWebViewClient (String portal, String clientId, String redirectUri) {
      mPortal = portal;
      mClientId = clientId;
      mRedirectUri = redirectUri;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

      Uri responseUri = Uri.parse(url);
      String schema = responseUri.getScheme();
      if (schema == null) {
        throw new RuntimeException("Invalid schema could not be parsed: " + responseUri.toString());
      }

      String error = responseUri.getQueryParameter("error");
      String errorDescription = responseUri.getQueryParameter("error_description");
      final String authCode = responseUri.getQueryParameter("code");

      // an error is returned from the portal OAuth login page when the user presses cancel button
      // that case is handled here
      if (error != null && !error.isEmpty()) {
        if (error.equalsIgnoreCase("access_denied") && errorDescription.equalsIgnoreCase("The user denied your request.")) {
          Toast.makeText(getApplicationContext(), "No credentials provided, failed to load secured resource", Toast.LENGTH_LONG).show();
          mOAuthLoginDialog.dismiss();
          return true;
        }
      }

      // if the user successfully provides the credential, the portal responds with the auth code
      // we send another request for OAuthTokenCredential with that code here
      if (authCode != null && !authCode.isEmpty()) {

        OAuthTokenCredentialRequest request = new OAuthTokenCredentialRequest(mPortal, null, mClientId, mRedirectUri,
            authCode);

        final ListenableFuture<OAuthTokenCredential> future = request.executeAsync();
        future.addDoneListener(new Runnable() {
          @Override
          public void run() {
            OAuthTokenCredential cred = null;
            try {
              cred = future.get();
            } catch (Exception e) {
              // If there is an exception, cred will remain null and will be passed back to
              // finish the challenge with a null credential
            } finally {
              finishOAuthChallenge(cred);
            }
          }
        });
        return true;
      }
      return (false);
    }
  }

  private static final class OAuthChallengeCountDownLatch extends CountDownLatch {

    private OAuthTokenCredential mCredential;

    /**
     * Constructs a {@code CountDownLatch} initialized with the given count.
     *
     * @param count the number of times {@link #countDown} must be invoked
     *              before threads can pass through {@link #await}
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public OAuthChallengeCountDownLatch(int count) {
      super(count);
    }

    /**
     * Sets an OAuthTokenCredential on the CountDownLatch so that it can be
     * received and returned to the calling request.
     *
     * @param credential the OAuthTokenCredential
     */
    public void setOAuthTokenCredential(OAuthTokenCredential credential) {
      mCredential = credential;
    }

    /**
     * Gets the OAuthTokenCredential that was obtained from the challenge so
     * that it may be returned to the calling request.
     *
     * @return the OAuthTokenCredential
     */
    public OAuthTokenCredential getOAuthTokenCredential() {
      return mCredential;
    }
  }

  /**
   * Helper method to finish the OAuthCredential challenge by setting the credential on the OAuthChallengeCountDownLatch
   * and counting down the latch.
   *
   * @param credential
   */
  public static void finishOAuthChallenge(OAuthTokenCredential credential) {
    if(sOAuthChallengeCountDownLatch != null) {
      sOAuthChallengeCountDownLatch.setOAuthTokenCredential(credential);
      sOAuthChallengeCountDownLatch.countDown();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }

}
