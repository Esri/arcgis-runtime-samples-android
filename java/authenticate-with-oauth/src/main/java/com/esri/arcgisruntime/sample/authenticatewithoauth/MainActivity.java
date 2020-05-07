/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.authenticatewithoauth;

import java.net.MalformedURLException;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.OAuthConfiguration;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    try {
      // set up an oauth config with url to portal, a client id and a re-direct url
      // a custom client id for your app can be set on the ArcGIS for Developers dashboard under
      // Authentication --> Redirect URIs
      OAuthConfiguration oAuthConfiguration = new OAuthConfiguration(getString(R.string.portal_url),
          getString(R.string.oauth_client_id),
          getString(R.string.oauth_redirect_uri) + "://" + getString(R.string.oauth_redirect_host));

      // setup AuthenticationManager to handle auth challenges
      DefaultAuthenticationChallengeHandler defaultAuthenticationChallengeHandler = new DefaultAuthenticationChallengeHandler(
          this);

      // use the DefaultChallengeHandler to handle authentication challenges
      AuthenticationManager.setAuthenticationChallengeHandler(defaultAuthenticationChallengeHandler);

      // add an OAuth configuration
      // NOTE: you must add the DefaultOAuthIntentReceiver Activity to the app's manifest to handle starting a browser
      AuthenticationManager.addOAuthConfiguration(oAuthConfiguration);

      // load the portal and add the portal item as a map to the map view
      Portal portal = new Portal(getString(R.string.portal_url));
      PortalItem portalItem = new PortalItem(portal, getString(R.string.webmap_world_traffic_id));
      ArcGISMap map = new ArcGISMap(portalItem);
      mMapView.setMap(map);

    } catch (MalformedURLException e) {
      String error = "Error in OAuthConfiguration URL: " + e.getMessage();
      Log.e(TAG, error);
      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
