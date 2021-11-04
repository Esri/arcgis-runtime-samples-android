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

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private MobileMapPackage mMapPackage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    //[DocRef: Name=Open Mobile Map Package-android, Category=Work with maps, Topic=Create an offline map]
    // create the mobile map package
    mMapPackage = new MobileMapPackage(getExternalFilesDir(null) + getString(R.string.yellowstone_mmpk));
    // load the mobile map package asynchronously
    mMapPackage.loadAsync();

    // add done listener which will invoke when mobile map package has loaded
    mMapPackage.addDoneLoadingListener(() -> {
      // check load status and that the mobile map package has maps
      if (mMapPackage.getLoadStatus() == LoadStatus.LOADED && !mMapPackage.getMaps().isEmpty()) {
        // add the map from the mobile map package to the MapView
        mMapView.setMap(mMapPackage.getMaps().get(0));
        mMapView.setViewpointScaleAsync(1000000);
      } else {
        String error = "Error loading mobile map package: " + mMapPackage.getLoadError().getMessage();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
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
