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

package com.esri.arcgisruntime.sample.honormobilemappackageexpirationdate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private Chronometer mChronometerView;
  private TextView mExpirationMessageTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // get a reference to the chronometer and expiration views
    mChronometerView = findViewById(R.id.chronometerView);
    mExpirationMessageTextView = findViewById(R.id.expirationMessageTextView);

    requestReadPermission();
  }

  /**
   * Load the mobile map package and set it's first map to the map view. If the mobile map package has expired, call a
   * method to pass expiration information on to the user.
   */
  private void loadMobileMapPackage() {
    MobileMapPackage mobileMapPackage = new MobileMapPackage(
        Environment.getExternalStorageDirectory() + getString(R.string.path_to_expired_mmpk));

    // wait for the map package to load
    mobileMapPackage.addDoneLoadingListener(() -> {
      // handle map package expiration, if expired
      if (mobileMapPackage.getExpiration() != null && mobileMapPackage.getExpiration().isExpired()) {
        handleMobileMapPackageExpiration(mobileMapPackage);
      }
      if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED) {
        // add the map to the map view
        mMapView.setMap(mobileMapPackage.getMaps().get(0));
      } else {
        String error = "Failed to load mobile map package: " + mobileMapPackage.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
    mobileMapPackage.loadAsync();
  }

  /**
   * Populate UI elements with relevant information regarding the expiration of the map.
   *
   * @param mobileMapPackage which has expired
   */
  private void handleMobileMapPackageExpiration(MobileMapPackage mobileMapPackage) {
    // show the views
    mExpirationMessageTextView.setVisibility(View.VISIBLE);
    mChronometerView.setVisibility(View.VISIBLE);
    // set the expiration message to the expiration text view
    mExpirationMessageTextView.setText(mobileMapPackage.getExpiration().getMessage());
    // define a format for the time passed
    SimpleDateFormat daysHoursFormat = new SimpleDateFormat("dd' days and 'HH:mm:ss' hours'", Locale.US);
    // set the base time to the mobile map package's expiration date
    mChronometerView.setBase(mobileMapPackage.getExpiration().getDateTime().getTimeInMillis());
    mChronometerView.setOnChronometerTickListener(chronometer -> {
      // the time passed since the mobile map package expired, in milliseconds
      long timePassedInMilliseconds = new Date().getTime() - chronometer.getBase();
      chronometer.setText(String.format(getResources().getString(R.string.chronometer_text),
          daysHoursFormat.format(new Date(timePassedInMilliseconds))));
    });
    mChronometerView.start();
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

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      loadMobileMapPackage();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      loadMobileMapPackage();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.read_local_storage_denied_mmpk), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }
}
