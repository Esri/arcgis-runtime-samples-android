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
