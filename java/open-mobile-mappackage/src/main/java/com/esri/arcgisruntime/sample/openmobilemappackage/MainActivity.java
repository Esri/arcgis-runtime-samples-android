/* Copyright 2016 Esri
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

package com.esri.arcgisruntime.sample.openmobilemappackage;

import java.io.File;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();
  private static final String FILE_EXTENSION = ".mmpk";
  private static File extStorDir;
  private static String extSDCardDirName;
  private static String filename;
  private static String mmpkFilePath;
  // define permission to request
  String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
  private MapView mMapView;
  private MobileMapPackage mapPackage;
  private int requestCode = 2;

  /**
   * Create the mobile map package file location and name structure
   */
  private static String createMobileMapPackageFilePath() {
    return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName + File.separator + filename
        + FILE_EXTENSION;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get sdcard resource name
    extStorDir = Environment.getExternalStorageDirectory();
    // get the directory
    extSDCardDirName = this.getResources().getString(R.string.config_data_sdcard_offline_dir);
    // get mobile map package filename
    filename = this.getResources().getString(R.string.yellowstone_mmpk);
    // create the full path to the mobile map package file
    mmpkFilePath = createMobileMapPackageFilePath();

    // retrieve the MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);

    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      loadMobileMapPackage(mmpkFilePath);
    } else {
      // request permission
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response
   */
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      loadMobileMapPackage(mmpkFilePath);
    } else {
      // report to user that permission was denied
      Toast.makeText(MainActivity.this, getResources().getString(R.string.location_permission_denied),
          Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Load a mobile map package into a MapView
   *
   * @param mmpkFile Full path to mmpk file
   */
  private void loadMobileMapPackage(String mmpkFile) {
    //[DocRef: Name=Open Mobile Map Package-android, Category=Work with maps, Topic=Create an offline map]
    // create the mobile map package
    mapPackage = new MobileMapPackage(mmpkFile);
    // load the mobile map package asynchronously
    mapPackage.loadAsync();

    // add done listener which will invoke when mobile map package has loaded
    mapPackage.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        // check load status and that the mobile map package has maps
        if (mapPackage.getLoadStatus() == LoadStatus.LOADED && !mapPackage.getMaps().isEmpty()) {
          // add the map from the mobile map package to the MapView
          mMapView.setMap(mapPackage.getMaps().get(0));
        } else {
          // log an issue if the mobile map package fails to load
          Log.e(TAG, mapPackage.getLoadError().getMessage());
        }
      }
    });
    //[DocRef: END]
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }
}
