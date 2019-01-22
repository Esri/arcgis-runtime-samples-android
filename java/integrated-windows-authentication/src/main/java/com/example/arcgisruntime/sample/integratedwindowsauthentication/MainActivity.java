package com.example.arcgisruntime.sample.integratedwindowsauthentication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.io.RemoteResource;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.loadable.Loadable;
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

/**
 * This sample app shows how to use a custom authentication challenge handler to work with
 * IWA portals.
 * <p>
 * 1. Enter url of the portal you want to connect in the edit text box. The url of a default test portal has
 * been set to the box.
 * 2. Tap on the "Sign In" button to connect to the portal as a named user. A dialog will be popped up for
 * username/password. When a correct credential is passed, portal will be loaded. If you keep passing in a
 * wrong credential, no more prompt for a credential will occur after the default number of attempts.
 * Then portal will fail to load. You can click the "Cancel" button to cancel the sign-in process.
 * Portal will be failed to load with different error message.
 */
public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private boolean mIsPublicPortal = false;
  private Portal mPublicPortal;
  private RecyclerView mRecyclerView;

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
    AuthenticationManager.setAuthenticationChallengeHandler(new IWACustomChallengeHandler(this));

    View view = getLayoutInflater().inflate(R.layout.portal_info, null);

    mRecyclerView = findViewById(R.id.recyclerView);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerView.setHasFixedSize(false);

    Button searchPublicButton = findViewById(R.id.searchPublicButton);
    searchPublicButton.setOnClickListener(v -> {
      // flag to indicate public portal
      mIsPublicPortal = true;

      // create a portal from
      mPublicPortal = new Portal("http://www.arcgis.com");

      // search the portal
      searchPortal(mPublicPortal);
    });

    Button searchSecureButton = findViewById(R.id.searchSecureButton);
    EditText portalUrlEditText = findViewById(R.id.portalUrlEditText);
    searchSecureButton.setOnClickListener(v -> {
      // get the string entered for the secure portal URL.
      String securedPortalUrl = portalUrlEditText.getText().toString();
      if (!securedPortalUrl.isEmpty()) {
        // create an instance of the IWA-secured portal, the user may be challenged for access.
        Portal iwaSecuredPortal = new Portal(securedPortalUrl, true);

        // search the portal
        searchPortal(iwaSecuredPortal);

        // Set a variable that indicates this is the secure portal.
        // When a map is loaded from the results, will need to know which portal it came from.
        mIsPublicPortal = false;

      } else {
        String error = "Portal URL is empty. Please enter a portal URL.";
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });



  }

  private void searchPortal(Portal portal) {

    Toast.makeText(this, "Searching for web map items on the portal at " + portal.getUri(), Toast.LENGTH_LONG).show();

    portal.loadAsync();
    portal.addDoneLoadingListener(() -> {
      if (portal.getLoadStatus() == LoadStatus.LOADED) {
        try {
          Toast.makeText(this, "Connected to the portal on " + new URI(portal.getUri()).getHost(), Toast.LENGTH_LONG)
              .show();
        } catch (URISyntaxException e) {
          String error = "Error getting URI from portal: " + e.getMessage();
          Log.e(TAG, error);
        }
        // report the user name used for this connection.
        if (portal.getUser() != null) {
          Toast.makeText(this, "Connected as: " + portal.getUser().getUsername(), Toast.LENGTH_LONG).show();
        } else {
          // for a secure portal, the user should never be anonymous
          Toast.makeText(this, "Connected as: Anonymous", Toast.LENGTH_LONG).show();
        }

        List<String> portalItemNames = new ArrayList<>();

        // search the portal for web maps
        ListenableFuture<PortalQueryResultSet<PortalItem>> portalItemResult = portal.findItemsAsync(new PortalQueryParameters());
        portalItemResult.addDoneListener(() -> {
          try {
            PortalQueryResultSet<PortalItem> portalItemSet = portalItemResult.get();
            Log.d(TAG, "Portal item set size: " + portalItemSet.getResults().size());
            for (PortalItem portalItem : portalItemSet.getResults()) {
              portalItemNames.add(portalItem.getName());
              Log.d(TAG, portalItem.getName());
            }
            String[] portalItemNamesArray = new String[portalItemNames.size()];
            Log.d(TAG, "Portal item names array: " + portalItemNames.size());
            PortalItemAdapter portalItemAdapter = new PortalItemAdapter(portalItemNames.toArray(portalItemNamesArray));
                mRecyclerView.setAdapter(portalItemAdapter);
                portalItemAdapter.notifyDataSetChanged();
          } catch (ExecutionException | InterruptedException e) {
            String error = "Error getting portal item set from portal: " + e.getMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
      } else {
        ArcGISRuntimeException loadError = portal.getLoadError();
        String error = loadError.getErrorCode() == 17 ?
            "Portal sign in was cancelled by user." :
            "Portal sign in failed: " + portal.getLoadError().getCause().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
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

/**
 * A custom authentication challenge handler to handle user credential challenges.
 * <p>
 * When a user credential challenge is issued, a dialog will be presented to the user, to provide a credential.
 * The portal url will be displayed as a message in the dialog. If a wrong credential has been
 * passed in the previous attempt, a different message will be displayed in the dialog.
 * The dialog has two edit text boxes for username and password respectively. Other SDKs'
 * samples may have one more parameter for IWA domain. As indicated by the Javadoc of UseCredential
 * the SDK is in favor of passing username as username@domain. This sample doesn't provide
 * an edit text box for the domain for simplicity.
 *
 * @see <a href="https://developers.arcgis.com/android/latest/api-reference/reference/com/esri/arcgisruntime/security/UserCredential.html#UserCredential(java.lang.String,%20java.lang.String)">Javadoc of UserCredential</a>
 */

class IWACustomChallengeHandler implements AuthenticationChallengeHandler {

  private final Activity mActivity;

  IWACustomChallengeHandler(Activity activity) {
    mActivity = activity;
  }

  public AuthenticationChallengeResponse handleChallenge(AuthenticationChallenge authenticationChallenge) {

    if (authenticationChallenge.getType() == AuthenticationChallenge.Type.USER_CREDENTIAL_CHALLENGE) {
      int maxAttempts = 5;
      if (authenticationChallenge.getFailureCount() > maxAttempts) {
        // exceeded maximum amount of attempts. Act like it was a cancel
        Toast.makeText(mActivity, "Exceeded maximum amount of attempts. Please try again!", Toast.LENGTH_LONG).show();
        return new AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL,
            authenticationChallenge);
      }

      // create a countdown latch with a count of one to synchronize the dialog
      CountDownLatch countDownLatch = new CountDownLatch(1);

      // present the sign-in dialog
      AtomicReference<UserCredential> credential = null;
      mActivity.runOnUiThread(() -> {
        // inflate the layout
        View dialogView = mActivity.getLayoutInflater().inflate(R.layout.credential_dialog, null);
        // create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        TextView hostname = dialogView.findViewById(R.id.auth_hostname);
        EditText username = dialogView.findViewById(R.id.auth_username);
        EditText password = dialogView.findViewById(R.id.auth_password);
        // set click listeners
        builder.setPositiveButton("Sign In", (dialog, which) -> {
          // create user credential
          credential.set(new UserCredential(username.getText().toString(), password.getText().toString()));
          countDownLatch.countDown();
        })
            .setNegativeButton("Cancel", (dialog, which) -> {
              // user cancelled the sign in process
              RemoteResource remoteResource = authenticationChallenge.getRemoteResource();
              if (remoteResource instanceof Loadable) {
                ((Loadable) remoteResource).cancelLoad();
              }
              countDownLatch.countDown();
            })
            .setView(dialogView);
        // set message text
        if (authenticationChallenge.getFailureCount() > 0) {
          hostname.setText("Wrong credential was passed to ${challenge.remoteResource.uri}");
        } else {
          hostname.setText("Credential is required to access ${challenge.remoteResource.uri}");
        }

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        // apply the button texts and disable the positive button unless both username and password contain text
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        TextWatcher watcher = new TextWatcher() {
          @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          }

          @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
          }

          @Override public void afterTextChanged(Editable s) {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setEnabled(username.getText().length() > 0 && password.getText().length() > 0);
          }
        };
        username.addTextChangedListener(watcher);
        password.addTextChangedListener(watcher);
      });
      try {
        countDownLatch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      // if credentials were set, return a new auth challenge response with them. otherwise, act like it was a cancel
      if (credential.get() != null) {
        return new AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
            credential);
      }
    }
    // no credentials were set , return a new auth challenge response with a cancel
    return new AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, authenticationChallenge);
  }
}
