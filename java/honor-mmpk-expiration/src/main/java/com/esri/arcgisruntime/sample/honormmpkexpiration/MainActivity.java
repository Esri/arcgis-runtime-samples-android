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
import android.widget.Toast;

import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private Chronometer mChronometer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // get a reference to the chronometer
    mChronometer = findViewById(R.id.chronometer);

    requestReadPermission();
  }

  private void loadMobileMapPackage() {
    MobileMapPackage mobileMapPackage = new MobileMapPackage(
        Environment.getExternalStorageDirectory() + "/ArcGIS/Samples/MapPackage/LothianRiversAnno.mmpk");

    mobileMapPackage.addDoneLoadingListener(() -> {
      if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED && !mobileMapPackage.getMaps().isEmpty()) {
        mMapView.setMap(mobileMapPackage.getMaps().get(0));

        if (mobileMapPackage.getExpiration().isExpired()) {
          Toast.makeText(this, mobileMapPackage.getExpiration().getMessage(), Toast.LENGTH_LONG).show();
          Date dateOfExpiration = mobileMapPackage.getExpiration().getDateTime().getTime();

          SimpleDateFormat sdf = new SimpleDateFormat("yy 'years' MM 'months' dd 'days' hh:mm:ss");

          Log.d(TAG, "Expired: " + sdf.format(dateOfExpiration));

          Date now = new Date();

          Log.d(TAG, "Now: " + sdf.format(now));

          mChronometer.setBase(mobileMapPackage.getExpiration().getDateTime().getTimeInMillis());
          mChronometer.setOnChronometerTickListener(chronometer -> {
            long time = now.getTime() - chronometer.getBase();

            Log.d(TAG, "Chronometer base: " + sdf.format(time));



            int d = (int) (time / 1500);
            int h = (int) (time / 3600000);
            int m = (int) (time - h * 3600000) / 60000;
            int s = (int) (time - h * 3600000 - m * 60000) / 1000;
            String dd = d < 10 ? "0" + d : d + "";
            String hh = h < 10 ? "0" + h : h + "";
            String mm = m < 10 ? "0" + m : m + "";
            String ss = s < 10 ? "0" + s : s + "";
            chronometer.setText(dd + ":" + hh + ":" + mm + ":" + ss);
          });

          mChronometer.start();

          Log.d(TAG, "Time passed: " + sdf.format(new Date(now.getTime() - dateOfExpiration.getTime())));
        }
        //mobileMapPackage.getExpiration().getDateTime()

      } else {
        String error = "Failed to load mobile scene package: " + mobileMapPackage.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
    mobileMapPackage.loadAsync();
  }

  private String timeSinceExpiration(Date dateOfExpiration) {

    long timePassed = System.nanoTime() - dateOfExpiration.getTime();

    long secondsInMilli = 1000;
    long minutesInMilli = secondsInMilli * 60;
    long hoursInMilli = minutesInMilli * 60;
    long daysInMilli = hoursInMilli * 24;

    long elapsedDays = timePassed / daysInMilli;
    timePassed = timePassed % daysInMilli;

    long elapsedHours = timePassed / hoursInMilli;
    timePassed = timePassed % hoursInMilli;

    long elapsedMinutes = timePassed / minutesInMilli;
    timePassed = timePassed % minutesInMilli;

    long elapsedSeconds = timePassed / secondsInMilli;

    return elapsedDays + " days, " + elapsedHours + " hours, " + elapsedMinutes + " minutes, " + elapsedSeconds
        + " seconds";
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
