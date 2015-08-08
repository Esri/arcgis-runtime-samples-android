/* Copyright 2015 Esri
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

package com.esri.arcgis.android.samples.standardlicenseoffline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Environment;
import android.widget.FrameLayout;

import com.esri.android.map.MapOptions;
import com.esri.android.map.MapOptions.MapType;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.oauth.OAuthView;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.portal.LicenseInfo;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalInfo;
import com.esri.core.runtime.LicenseLevel;
import com.esri.core.runtime.LicenseResult;

/*
 * This sample shows how a Standard license can be set for an app by logging in 
 * to a Portal when a device has network connectivity, and how license 
 * information can be saved to license the app when network connectivity is 
 * unavailable. A Standard license removes the developer watermark from the map,
 * and also enables various offline functions. The license is set by using a 
 * LicenseInfo obtained from an authenticated Portal. An OAuthView is used to
 * enter Portal credentials. Once logged in, the LicenseInfo is saved to a file 
 * on the device, from where it can be used to license the app when network 
 * connectivity is unavailable. Alternatively you can explore how the set 
 * Standard license level via a license string obtained from Esri customer 
 * service (see the Standard License sample for more information). 
 * 
 * In order to set the license level to Standard you must first set a valid 
 * client ID by registering a new application with ArcGIS for Developers. 
 * When you release your app, you should ensure that the client id is 
 * encrypted and saved to the device in a secure manner; this sample uses 
 * a hardcoded string instead for simplicity of example code. 
 * 
 * This sample loads a Tile Map Package (TPK) from device storage if network is
 * not available when the app starts. A sample .tpk is available from ArcGIS
 * Online at:
 * http://www.arcgis.com/home/item.html?id=9a7e015149854c35b5eb94494f4aeb89 
 * 
 * To use this sample, you must set a valid client id and copy data locally.
 * Follow these steps to generate a client ID:
 * 1) Browse to https://developers.arcgis.com.
 * 2) Sign in with your ArcGIS developer account.
 * 3) Create an application. This will give you access to a client id string.
 * 4) Update the CLIENT_ID constant in the code below with the client id 
 *    string you generated. 
 *    NOTE: When you release your app, you should ensure that the client 
 *    id is encrypted and saved to the device in a secure manner; the
 *    code here uses a hardcoded string instead for simplicity. 
 * Follow these steps to copy the example .tpk locally:
 * 1) On your device, browse to:
 *    http://www.arcgis.com/home/item.html?id=9a7e015149854c35b5eb94494f4aeb89.
 * 2) Click Open and Download.
 * 3) Copy the file to the device at the expected path, for example to:
 *     <external-storage-folder>/ArcGIS/samples/SanFrancisco.tpk
 *   Alternatively, you can set the path to a .tpk file you already have on the 
 *   device by changing the DATA_RELATIVE_PATH to point to the file, as a 
 *   relative path under the external storage folder.
 */

public class MainActivity extends Activity {

  // TODO: Set CLIENT_ID to a valid client id from developers.arcgis.com.
  // NOTE: When you release your app, you should ensure that the client id is 
  // encrypted and saved to the device in a secure manner. 
  private static final String CLIENT_ID = ""; //null;

  private static final String PORTAL_URL = "https://www.arcgis.com";

  private static final String LICENSE_INFO_FILE_NAME = "licenseinfo.json";

  private static final String DATA_RELATIVE_PATH = "/ArcGIS/Samples/SanFrancisco.tpk";

  private static final int OAUTH_EXPIRATION_NEVER = -1;

  public FrameLayout mViewContainer;

  public OAuthView mOAuthView;

  /**
   * Called when the activity is starting. Set the client ID and initialize
   * license.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mViewContainer = (FrameLayout) findViewById(R.id.main_activity_view_container);

    // Setting the client id string on the ArcGISRuntime class will set the app
    // license level to Basic. ArcGISRuntime.setClientId() must be called before
    // any other calls to the ArcGIS API for Android are made.
    LicenseResult licenseResult = ArcGISRuntime.setClientId(CLIENT_ID);

    // Check the license result before proceeding.
    LicenseLevel licenseLevel = ArcGISRuntime.License.getLicenseLevel();
    if (licenseResult == LicenseResult.VALID
        && licenseLevel == LicenseLevel.BASIC) {
      // Next, try to initialize a STANDARD license.
      initializeStandardLicense();
    } else {
      // Indicate if an unexpected license result was received from the
      // setClientId
      // call, or the existing license level is not BASIC; in this case STANDARD
      // license may not be applied.
      MessageDialogFragment.showMessage(
          getString(R.string.valid_client_id_required), getFragmentManager());

      showMap();
    }
  }

  /**
   * This method chooses how to set the app license by checking network
   * connectivity. Results are reported to the user.
   */
  private void initializeStandardLicense() {
    // Check if network connectivity is available.
    if (isNetworkAvailable()) {
      // If network is available, sign in by showing the OAuthView.
      signInWithOAuth();
    } else {
      // If disconnected, check if we have saved license information.
      LicenseInfo licenseInfo = null;
      try {
        licenseInfo = readLicenseInfoFromFileSystem();
      } catch (IOException e) {
        e.printStackTrace();
      }

      // Use the deserialized LicenseInfo to set the app license. If the
      // LicenseInfo is null, the return value will be INVALID.
      LicenseResult licenseResult = ArcGISRuntime.License
          .setLicense(licenseInfo);

      // Check and report the result of the licensing.
      LicenseLevel licenseLevel = ArcGISRuntime.License.getLicenseLevel();
      if (licenseResult == LicenseResult.VALID
          && licenseLevel == LicenseLevel.STANDARD) {
        MessageDialogFragment.showMessage(
            getString(R.string.standard_license_succeeded),
            getFragmentManager());
      } else {
        MessageDialogFragment.showMessage(
            getString(R.string.standard_license_requires_connection),
            getFragmentManager());
      }

      // Now licensing is complete, create and show a MapView.
      showMap();
    }
  }

  /**
   * This method attempts to set the license level of the application to
   * LicenseLevel.Standard. Standard license level is set by retrieving a
   * LicenseInfo object from an authenticated Portal instance. The authenticated
   * Portal instance is created with the UserCredentials obtained from the
   * previous OAuth sign in step. The steps to set standard license level with a
   * LicenseInfo object are:
   * <ol>
   * <li>Sign in to a portal via OAuth to retrieve UserCredentials.</li>
   * <li>Create an authenticated Portal instance from the UserCredentials.</li>
   * <li>Fetch the PortalInfo from the Portal instance and retrieve the
   * LicenseInfo.</li>
   * <li>Initialize the license with the LicenseInfo to activate Standard
   * license level.</li>
   * </ol>
   */
  public void setStandardLicenseFromPortal(UserCredentials credentials) {

    // Create a Portal object with the credentials from the OAuthView.
    Portal portal = new Portal(PORTAL_URL, credentials);
    PortalInfo portalInfo = null;

    // Get the PortalInfo from the Portal.
    try {
      portalInfo = portal.fetchPortalInfo();
    } catch (Exception e) {
      MessageDialogFragment.showMessage(
          getString(R.string.standard_license_failed), getFragmentManager());

      return;
    }
    LicenseInfo licenseInfo = portalInfo.getLicenseInfo();

    // Set app license with the LicenseInfo from the PortalInfo.
    LicenseResult licenseResult = ArcGISRuntime.License.setLicense(licenseInfo);

    // Check and report the result of the licensing.
    LicenseLevel licenseLevel = ArcGISRuntime.License.getLicenseLevel();
    if (licenseResult == LicenseResult.VALID
        && licenseLevel == LicenseLevel.STANDARD) {

      // Store the LicenseInfo on the file system so it can be used another time
      // when there is no network connectivity.
      try {
        writeLicenseInfoToFileSystem(licenseInfo);
      } catch (IOException e) {
        e.printStackTrace();
      }
      MessageDialogFragment.showMessage(
          getString(R.string.standard_license_succeeded), getFragmentManager());
    } else {
      MessageDialogFragment.showMessage(
          getString(R.string.standard_license_failed), getFragmentManager());
    }

    showMap();
  }

  /**
   * Returns true if a data connection is currently available.
   * 
   * @return true if a data connection is available.
   */
  public boolean isNetworkAvailable() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    // Check cellular data connection.
    NetworkInfo mobileNi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    if (mobileNi != null && mobileNi.getState() == State.CONNECTED) {
      return true;
    }

    // Check wifi connection
    NetworkInfo wifiNi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    if (wifiNi != null && wifiNi.getState() == State.CONNECTED) {
      return true;
    }

    return false;
  }

  /**
   * Sets up a sign in UI that allows the user to sign in to a portal via OAuth.
   * OAuth is the recommended authentication method to sign in to a portal.
   * After successful sign in the obtained UserCredentials are used to set the
   * license level to standard by retrieving a LicenseInfo object from an
   * authenticated Portal instance (see setStandardLicenseWithLicenseInfo()
   * method).
   */
  private void signInWithOAuth() {

    // Create a new OAuthView to allow users to enter credentials.
    mOAuthView = new OAuthView(this, PORTAL_URL, CLIENT_ID,
        OAUTH_EXPIRATION_NEVER, new CallbackListener<UserCredentials>() {

          // Set a callback to inform the user if there is an error.
          @Override
          public void onError(Throwable e) {
            MessageDialogFragment.showMessage(
                getString(R.string.oauth_login_failed), getFragmentManager());
          }

          // Set a callback to continue with licensing the app once credentials
          // are entered.
          @Override
          public void onCallback(UserCredentials credentials) {

            // Use the entered credentials to get a license.
            if (credentials != null) {
              setStandardLicenseFromPortal(credentials);
            } else {
              MessageDialogFragment.showMessage(
                  getString(R.string.oauth_login_failed), getFragmentManager());
            }
          }
        });

    // Add the OAuthView to the root view.
    mViewContainer.addView(mOAuthView);
  }

  /**
   * Attempts to read a LicenseInfo persisted as a json string to a private
   * file.
   * 
   * @return a LicenseInfo read from the file system or null
   * @throws IOException
   */
  private LicenseInfo readLicenseInfoFromFileSystem() throws IOException {
    LicenseInfo licenseInfo = null;
    BufferedReader reader = null;

    try {
      // Open and read the saved license info.
      InputStream in = this.openFileInput(LICENSE_INFO_FILE_NAME);
      reader = new BufferedReader(new InputStreamReader(in));
      StringBuilder jsonBuilder = new StringBuilder();
      String line = null;

      while ((line = reader.readLine()) != null) {
        jsonBuilder.append(line);
      }

      // Deserialize the json string from the file into a LicenseInfo object
      licenseInfo = LicenseInfo.fromJson(jsonBuilder.toString());

    } catch (FileNotFoundException e) {
      // no persisted license info available
    } finally {
      // Clean up the license file reader.
      if (reader != null) {
        reader.close();
      }
    }

    return licenseInfo;
  }

  /**
   * Writes the specified LicenseInfo as a json string to the file system.
   * 
   * @param licenseInfo the LicenseInfo to store on the file system
   * @throws IOException
   */
  private void writeLicenseInfoToFileSystem(LicenseInfo licenseInfo)
      throws IOException {
    if (licenseInfo == null) {
      return;
    }

    // Write serialized LicenseInfo json to a private file.
    Writer writer = null;
    try {
      OutputStream out = this.openFileOutput(LICENSE_INFO_FILE_NAME,
          Context.MODE_PRIVATE);
      writer = new OutputStreamWriter(out);

      writer.write(licenseInfo.toJson());
    } finally {
      // Clean up the license file writer.
      if (writer != null) {
        writer.close();
      }
    }
  }

  /**
   * Shows a map in the MainActivity's layout, with online data if network
   * connectivity is available, or with local data if not.
   */
  private void showMap() {
    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        // If the OAuthView was used, remove it so map can be shown instead.
        if (mOAuthView != null) {
          mViewContainer.removeView(mOAuthView);
          mOAuthView = null;
        }

        MapView mapView;
        if (isNetworkAvailable()) {
          // If there is network connectivity, create a MapView with a basemap
          // based on a service.
          mapView = new MapView(MainActivity.this, new MapOptions(MapType.TOPO,
              34.056215, -117.195668, 16));
        } else {
          // If network is unavailable, create a local tiled layer based on a
          // .tpk file stored on the device.
          String dataPath = Environment.getExternalStorageDirectory().getPath()
              + DATA_RELATIVE_PATH;
          if (!new File(dataPath).exists()) {
            MessageDialogFragment.showMessage(
                getString(R.string.local_data_not_found) + dataPath,
                getFragmentManager());
          }
          ArcGISLocalTiledLayer local = new ArcGISLocalTiledLayer(dataPath);

          // Create a MapView, using extent and spatial reference of the layer.
          mapView = new MapView(MainActivity.this, local.getSpatialReference(),
              local.getFullExtent());
          mapView.addLayer(local);
        }

        // Add the MapView to the ViewContainer.
        mViewContainer.addView(mapView);
      }
    });
  }

}
