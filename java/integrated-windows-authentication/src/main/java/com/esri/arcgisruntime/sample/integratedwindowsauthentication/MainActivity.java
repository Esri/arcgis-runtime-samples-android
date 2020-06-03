/* Copyright 2019 Esri
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

package com.esri.arcgisruntime.sample.integratedwindowsauthentication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.portal.PortalQueryParameters;
import com.esri.arcgisruntime.portal.PortalQueryResultSet;
import com.esri.arcgisruntime.security.AuthenticationChallenge;
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.UserCredential;

public class MainActivity extends AppCompatActivity implements AuthenticationChallengeHandler {

  private static final String TAG = MainActivity.class.getSimpleName();

  private RecyclerView mRecyclerView;

  private TextView mLoadWebMapTextView;

  private View mPortalLoadStateView;

  private TextView mLoadStateTextView;

  private MapView mMapView;

  private UserCredential mUserCredential;

  private Portal mPortal;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a streets base map
    ArcGISMap map = new ArcGISMap(Basemap.createStreets());
    // set the map to the map view
    mMapView.setMap(map);

    // set authentication challenge handler
    AuthenticationManager.setAuthenticationChallengeHandler(this);

    // set up recycler view for listing portal items
    mRecyclerView = findViewById(R.id.recyclerView);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    mPortal = new Portal(getString(R.string.arcgis_url));

    // set up search public button
    Button searchPublicButton = findViewById(R.id.searchPublicButton);
    searchPublicButton.setOnClickListener(v -> {
      // search the the public ArcGIS portal
      searchPortal(mPortal);
    });

    // get reference to load state UI elements
    mPortalLoadStateView = findViewById(R.id.portalLoadState);
    mPortalLoadStateView.setVisibility(View.GONE);
    mLoadStateTextView = findViewById(R.id.loadStateTextView);
    mLoadWebMapTextView = findViewById(R.id.loadedWebMapTextView);

    Button searchSecureButton = findViewById(R.id.searchSecureButton);
    EditText portalUrlEditText = findViewById(R.id.portalUrlEditText);
    searchSecureButton.setOnClickListener(v -> {
      // get the string entered for the secure portal URL.
      String securedPortalUrl = portalUrlEditText.getText().toString();
      if (!securedPortalUrl.isEmpty()) {
        // search an instance of the IWA-secured portal, the user may be challenged for access
        searchPortal(new Portal(securedPortalUrl, true));
      } else {
        String error = "Portal URL is empty. Please enter a portal URL.";
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Search the given portal for its portal items and display them in a recycler view. On click, call AddMap().
   *
   * @param portal
   */
  private void searchPortal(Portal portal) {

    // check if the the portal is null
    if (portal == null) {
      Log.e(TAG, "Portal null");
      return;
    }

    // clear any existing data in the recycler view
    mRecyclerView.setAdapter(null);

    // show portal load state
    mPortalLoadStateView.setVisibility(View.VISIBLE);
    mLoadStateTextView.setText("Searching for web map items on the portal at " + portal.getUri());

    portal.loadAsync();
    portal.addDoneLoadingListener(() -> {
      if (portal.getLoadStatus() == LoadStatus.LOADED) {
        try {
          // update load state in UI with the portal URI
          mLoadStateTextView.setText("Connected to the portal on " + new URI(portal.getUri()).getHost());
        } catch (URISyntaxException e) {
          String error = "Error getting URI from portal: " + e.getMessage();
          Log.e(TAG, error);
        }
        // report the user name used for this connection.
        if (portal.getUser() != null) {
          mLoadStateTextView.setText("Connected as: " + portal.getUser().getUsername());
        } else {
          // for a secure portal, the user should never be anonymous
          mLoadStateTextView.setText("Connected as: Anonymous");
        }

        // search the portal for web maps
        ListenableFuture<PortalQueryResultSet<PortalItem>> portalItemResult = portal
            .findItemsAsync(new PortalQueryParameters("type:(\"web map\" NOT \"web mapping application\")"));
        portalItemResult.addDoneListener(() -> {
          try {
            PortalQueryResultSet<PortalItem> portalItemSet = portalItemResult.get();
            PortalItemAdapter portalItemAdapter = new PortalItemAdapter(portalItemSet.getResults(),
                portalItem -> addMap(portal, portalItem.getItemId()));
            mRecyclerView.setAdapter(portalItemAdapter);
            mPortalLoadStateView.setVisibility(View.GONE);
          } catch (ExecutionException | InterruptedException e) {
            // hide load state view
            mPortalLoadStateView.setVisibility(View.GONE);
            // report error
            String error = "Error getting portal item set from portal: " + e.getMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
      } else {
        // hide load state view
        mPortalLoadStateView.setVisibility(View.GONE);
        // report error
        ArcGISRuntimeException loadError = portal.getLoadError();
        String error = "Portal sign in failed: " + loadError.getCause() == null ?
            loadError.getMessage() :
            loadError.getCause().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Add the given portal item to a new map and set the map to the map view.
   *
   * @param portal
   * @param itemId
   */
  private void addMap(Portal portal, String itemId) {
    // report error and return if portal is null
    if (portal == null) {
      String error = "Portal not instantiated.";
      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      Log.e(TAG, error);
      return;
    }
    // use the item ID to create a portal item from the portal
    PortalItem portalItem = new PortalItem(portal, itemId);
    // create a map using the web map (portal item) and add it to the map view
    ArcGISMap webMap = new ArcGISMap(portalItem);
    mMapView.setMap(webMap);
    // show item ID in UI
    mLoadWebMapTextView.setText("Loaded web map from item " + itemId);
  }

  /**
   * When a user credential challenge is issued, a dialog will be presented to the user to take credential information.
   * The portal URL will be displayed as a message in the dialog. If a wrong credential has been passed in the previous
   * attempt, a different message will be displayed in the dialog. The dialog has two edit text boxes for username and
   * password respectively. Other SDKs' samples may have one more parameter for IWA domain. As indicated by the Javadoc
   * of UseCredential, the Android SDK is in favor of passing username as username@domain or domain\\username.
   */
  @Override
  public AuthenticationChallengeResponse handleChallenge(AuthenticationChallenge authenticationChallenge) {
    if (authenticationChallenge.getType() == AuthenticationChallenge.Type.USER_CREDENTIAL_CHALLENGE
        && authenticationChallenge.getRemoteResource() instanceof Portal) {

      // If challenge has been requested by a Portal and the Portal has been loaded, cancel the challenge
      // This is required as some layers have private portal items associated with them and we don't
      // want to auth against them
      if (((Portal) authenticationChallenge.getRemoteResource()).getLoadStatus() == LoadStatus.LOADED) {
        return new AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL,
            authenticationChallenge);
      }

      // inflate and create the credential dialog
      View dialogView = getLayoutInflater().inflate(R.layout.credential_dialog, null);
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      TextView hostname = dialogView.findViewById(R.id.credentialHostnameTextView);
      EditText username = dialogView.findViewById(R.id.credentialUsernameEditText);
      EditText password = dialogView.findViewById(R.id.credentialPasswordEditText);

      // create a countdown latch with a count of one to synchronize the dialog
      CountDownLatch signal = new CountDownLatch(1);
      runOnUiThread(() -> {
        // set click listeners
        builder.setPositiveButton("Sign In", (dialog, which) -> {
          if (username.getText().length() > 0 && password.getText().length() > 0) {
            // create user credential from edit text
            mUserCredential = new UserCredential(username.getText().toString(), password.getText().toString());
          } else {
            Toast.makeText(this, "Username and password must not be blank.", Toast.LENGTH_SHORT).show();
          }
          signal.countDown();
        }).setNegativeButton("Cancel", (dialog, which) -> {
          // user cancelled the sign in process. Reset credential to null
          mUserCredential = null;
          signal.countDown();
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            // act like it was a cancel. Reset credential to null
            mUserCredential = null;
            signal.countDown();  
          }
        }).setView(dialogView);
        AlertDialog credentialDialog = builder.create();
        credentialDialog.setCanceledOnTouchOutside(false);
        credentialDialog.show();
        // set message text
        if (authenticationChallenge.getFailureCount() > 0) {
          hostname.setText("Wrong credential was passed to " + authenticationChallenge.getRemoteResource().getUri());
        } else {
          hostname.setText("Credential is required to access " + authenticationChallenge.getRemoteResource().getUri());
        }
      });
      try {
        signal.await();
      } catch (InterruptedException e) {
        String error = "Interruption handling AuthenticationChallengeResponse: " + e.getMessage();
        runOnUiThread(() -> {
          Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
        Log.e(TAG, error);
      }

      // if credentials were set, return a new auth challenge response with them. otherwise, act like it was a cancel
      if (mUserCredential != null) {
        return new AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
            mUserCredential);
      }
    }
    // no credentials were set, return a new auth challenge response with a cancel
    return new AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, authenticationChallenge);
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}


