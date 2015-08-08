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

package com.esri.arcgis.android.samples.standardlicense;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.esri.android.map.MapOptions;
import com.esri.android.map.MapOptions.MapType;
import com.esri.android.map.MapView;
import com.esri.android.oauth.OAuthView;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.portal.LicenseInfo;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalInfo;
import com.esri.core.runtime.LicenseLevel;
import com.esri.core.runtime.LicenseResult;

/**
 * This sample shows how to set the license level of your ArcGIS application to Standard. Setting the license level to
 * Standard allows you to use features of the ArcGIS SDK for Android that require Standard license level. In order to
 * set the license level to Standard you need to edit this code and assign a valid client id string to the CLIENT_ID
 * constant.<p>
 * When you release your app, you should ensure that the client id, and license string (if used), are encrypted
 * and saved to the device in a secure manner; this sample uses a hardcoded string instead for simplicity of example 
 * code.<p>
 * Follow these steps:
 * <ol>
 * <li>Browse to https://developers.arcgis.com.</li>
 * <li>Sign in with your ArcGIS developer account.</li>
 * <li>Create an application. This will give you access to a client id string.</li>
 * <li>Initialize the CLIENT_ID constant with the client id string and run the sample. If the client id has been
 * successfully set a OAuth sign in UI is shown which allows the user to sign in to a portal. After successful sign in
 * the license level is set to standard based on the LicenseInfo retrieved from the authenticated portal.</li>
 * </ol>
 * Alternatively you can explore how to set Standard license level via a license string obtained from Esri customer
 * service. In this case edit the code to initialize the LICENSE_STRING constant with a license string and set
 * USE_LICENSE_INFO to false.<p> 
 * <b>NOTE:</b> When you release your app, you should ensure that the client id and license string (if used) are 
 * encrypted and saved to the device in a secure manner; the code here uses hardcoded strings instead for simplicity
 * of example code.
 */
public class MainActivity extends Activity {

  // TODO: initialize CLIENT_ID with a valid client id string
  // NOTE: When you release your app, you should ensure that the client id is encrypted and saved to the device 
  // in a secure manner. 
  private static final String CLIENT_ID = null;

  // TODO: initialize LICENSE_STRING with a valid license string and set USE_LICENSE_INFO to false if you want to unlock
  // Standard license level via a license string. A license string needs to be obtained from Esri customer service.
  // NOTE: When you release your app, you should ensure that the license string is encrypted and saved to the device
  // in a secure manner. 
  private static final String LICENSE_STRING = null;

  private static final boolean USE_LICENSE_INFO = true;

  private static final String PORTAL_URL = "https://www.arcgis.com";

  private static final int OAUTH_EXPIRATION_NEVER = -1;

  private FrameLayout mViewContainer;

  private OAuthView mOAuthView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mViewContainer = (FrameLayout) findViewById(R.id.main_activity_view_container);

    // Set the client id string on the ArcGISRuntime class. This will set the license level to Basic.
    // ArcGISRuntime.setClientId() needs to be called before any other calls to the ArcGIS SDK for Android are made.
    //
    LicenseResult licenseResult = ArcGISRuntime.setClientId(CLIENT_ID);

    LicenseLevel licenseLevel = ArcGISRuntime.License.getLicenseLevel();

    if (licenseResult == LicenseResult.VALID && licenseLevel == LicenseLevel.BASIC) {
      if (USE_LICENSE_INFO) {
        signInWithOAuth();
      } else {
        setStandardLicenseWithLicenseString();
      }
    } else {
      MessageDialogFragment.showMessage(getString(R.string.valid_client_id_required), getFragmentManager());

      showMap();
    }
  }

  /**
   * Sets up a sign in UI that allows the user to sign in to a portal via OAuth. OAuth is the recommended authentication
   * method to sign in to a portal. After successful sign in the obtained UserCredentials are used to set the license
   * level to standard by retrieving a LicenseInfo object from an authenticated Portal instance (see
   * setStandardLicenseWithLicenseInfo() method).
   */
  private void signInWithOAuth() {
    mOAuthView = new OAuthView(this, PORTAL_URL, CLIENT_ID, OAUTH_EXPIRATION_NEVER,
        new CallbackListener<UserCredentials>() {

          @Override
          public void onError(Throwable e) {
            MessageDialogFragment.showMessage(getString(R.string.oauth_login_failed), getFragmentManager());
          }

          @Override
          public void onCallback(UserCredentials credentials) {
            if (credentials != null) {
              setStandardLicenseWithLicenseInfo(credentials);
            } else {
              MessageDialogFragment.showMessage(getString(R.string.oauth_login_failed), getFragmentManager());
            }
          }
        });

    mViewContainer.addView(mOAuthView);
  }

  /**
   * This method attempts to set the license level of the application to LicenseLevel.Standard. Standard license level
   * is set by retrieving a LicenseInfo object from an authenticated Portal instance. The authenticated Portal instance
   * is created with the UserCredentials obtained from the previous OAuth sign in step. The steps to set standard
   * license level with a LicenseInfo object are:
   * <ol>
   * <li>Sign in to a portal via OAuth to retrieve UserCredentials.</li>
   * <li>Create an authenticated Portal instance from the UserCredentials.</li>
   * <li>Fetch the PortalInfo from the Portal instance and retrieve the LicenseInfo.</li>
   * <li>Initialize the license with the LicenseInfo to activate Standard license level.</li>
   * </ol>
   * This approach to set standard license level is suitable for applications that connect to an ArcGIS portal. For
   * applications that don't connect to an ArcGIS portal see {@link setStandardLicenseFromLicenseString} method.
   */
  private void setStandardLicenseWithLicenseInfo(UserCredentials credentials) {
    Portal portal = new Portal(PORTAL_URL, credentials);
    PortalInfo portalInfo = null;

    try {
      portalInfo = portal.fetchPortalInfo();
    } catch (Exception e) {
      MessageDialogFragment.showMessage(getString(R.string.standard_license_failed), getFragmentManager());

      return;
    }

    LicenseInfo licenseInfo = portalInfo.getLicenseInfo();

    LicenseResult licenseResult = ArcGISRuntime.License.setLicense(licenseInfo);
    LicenseLevel licenseLevel = ArcGISRuntime.License.getLicenseLevel();

    if (licenseResult == LicenseResult.VALID && licenseLevel == LicenseLevel.STANDARD) {
      MessageDialogFragment.showMessage(getString(R.string.standard_license_succeeded), getFragmentManager());
    } else {
      MessageDialogFragment.showMessage(getString(R.string.standard_license_failed), getFragmentManager());
    }

    showMap();
  }

  /**
   * This method attempts to set the license level of the application to LicenseLevel.Standard from a previously
   * obtained license string. The license string needs to be obtained from Esri customer service. This approach to set
   * standard license level is suitable for applications that never connect to an ArcGIS portal. For applications that
   * do connect to an ArcGIS portal see setStandardLicenseWithLicenseInfo() method.
   */
  private void setStandardLicenseWithLicenseString() {
    LicenseResult licenseResult = ArcGISRuntime.License.setLicense(LICENSE_STRING);
    LicenseLevel licenseLevel = ArcGISRuntime.License.getLicenseLevel();

    if (licenseResult == LicenseResult.VALID && licenseLevel == LicenseLevel.STANDARD) {
      MessageDialogFragment.showMessage(getString(R.string.standard_license_succeeded), getFragmentManager());
    } else {
      MessageDialogFragment.showMessage(getString(R.string.standard_license_failed), getFragmentManager());
    }

    showMap();
  }

  /**
   * Shows a map in the MainActivity's layout.
   */
  private void showMap() {
    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        if (mOAuthView != null) {
          // remove the OAuthView so we can show the map
          //
          mViewContainer.removeView(mOAuthView);
          mOAuthView = null;
        }

        MapView mapView = new MapView(MainActivity.this, new MapOptions(MapType.TOPO, 34.056215, -117.195668, 16));
        mViewContainer.addView(mapView);
      }
    });
  }
}
