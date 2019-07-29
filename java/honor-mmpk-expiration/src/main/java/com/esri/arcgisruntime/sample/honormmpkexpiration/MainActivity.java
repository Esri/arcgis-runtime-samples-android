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

package com.esri.arcgisruntime.sample.honormmpkexpiration;

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
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    requestReadPermission();
  }

  private void loadMobileMapPackage() {
    MobileMapPackage mobileMapPackage = new MobileMapPackage(
        Environment.getExternalStorageDirectory() + getString(R.string.path_to_expired_mmpk));

    // get a reference to the chronometer and expiration views
    Chronometer chronometerView = findViewById(R.id.chronometerView);
    TextView expirationMessageTextView = findViewById(R.id.expirationMessageTextView);

    // wait for the map package to load
    mobileMapPackage.addDoneLoadingListener(() -> {
      if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED && !mobileMapPackage.getMaps().isEmpty()) {
        // add the map to the map view
        mMapView.setMap(mobileMapPackage.getMaps().get(0));
        // if the map is expired
        if (mobileMapPackage.getExpiration().isExpired()) {
          // set the expiration message to the expiration text view
          expirationMessageTextView.setText(mobileMapPackage.getExpiration().getMessage());
          // define a format for the time passed
          SimpleDateFormat daysHoursFormat = new SimpleDateFormat("dd' days and 'HH:mm:ss' hours'", Locale.US);
          // set the base time to the date of expiration of the mmpk
          chronometerView.setBase(mobileMapPackage.getExpiration().getDateTime().getTimeInMillis());
          chronometerView.setOnChronometerTickListener(chronometer -> {
            // the time passed since the mmpk expired, in milliseconds
            long timePassedInMilliseconds = new Date().getTime() - chronometer.getBase();
            chronometer.setText("Expired " + daysHoursFormat.format(new Date(timePassedInMilliseconds)) + " ago.");
          });
          chronometerView.start();
        }
      } else {
        String error = "Failed to load mobile scene package: " + mobileMapPackage.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
    mobileMapPackage.loadAsync();
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
