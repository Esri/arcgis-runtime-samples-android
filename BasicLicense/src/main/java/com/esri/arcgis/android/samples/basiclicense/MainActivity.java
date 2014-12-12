/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the sample code usage restrictions document for further information.
 *
 */

package com.esri.arcgis.android.samples.basiclicense;

import android.app.Activity;
import android.os.Bundle;

import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.runtime.LicenseLevel;
import com.esri.core.runtime.LicenseResult;

/**
 * This sample shows how to set the license level of your ArcGIS application to Basic. Setting the license level to
 * Basic prevents the watermark from appearing on the map. In order to set the license level to Basic you need to edit
 * this code and assign a valid client id string to the CLIENT_ID constant.<p>
 * When you release your app, you should ensure that the client id is encrypted and saved to the device in a 
 * secure manner; this sample uses a hardcoded string instead for simplicity of example code.<p>
 * Follow these steps:
 * <ol>
 * <li>Browse to https://developers.arcgis.com.</li>
 * <li>Sign in with your ArcGIS developer account.</li>
 * <li>Create an application. This will give you access to a client id string.</li>
 * <li>Initialize the CLIENT_ID constant with the client id string and run the sample. If the license level has been
 * successfully set to Basic you won't see a watermark on the map.<p>
 * <b>NOTE:</b> When you release your app, you should ensure that the client id is encrypted and saved to the 
 * device in a secure manner; the code here uses a hardcoded string instead for simplicity.</li>
 * </ol>
 */
public class MainActivity extends Activity {

  // TODO: initialize CLIENT_ID with a valid client id string
  // NOTE: When you release your app, you should ensure that the client id is 
  // encrypted and saved to the device in a secure manner. 
  private static final String CLIENT_ID = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set the client id string on the ArcGISRuntime class. This will set the license level to Basic.
    // ArcGISRuntime.setClientId() needs to be called before any other calls to the ArcGIS SDK for Android are made.
    //
    LicenseResult licenseResult = ArcGISRuntime.setClientId(CLIENT_ID);

    LicenseLevel licenseLevel = ArcGISRuntime.License.getLicenseLevel();

    if (licenseResult == LicenseResult.VALID && licenseLevel == LicenseLevel.BASIC) {
      MessageDialogFragment.showMessage(getString(R.string.basic_license_succeeded), getFragmentManager());
    } else {
      MessageDialogFragment.showMessage(getString(R.string.valid_client_id_required), getFragmentManager());
    }

    setContentView(R.layout.activity_main);
  }
}
