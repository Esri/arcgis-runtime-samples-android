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

package com.esri.arcgisruntime.sample.featurelayergeodatabase;

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

import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  // define permission to request
  private final String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create map and add to view
    ArcGISMap map = new ArcGISMap(Basemap.createStreets());
    mMapView = findViewById(R.id.mapView);
    mMapView.setMap(map);

    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      loadGeodatabase();
    } else {
      // request permission
      int requestCode = 2;
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }
  }

  /**
   * Load a local geodatabase file and add it to the map
   */
  private void loadGeodatabase() {

    String path =
        Environment.getExternalStorageDirectory() + getString(R.string.config_data_sdcard_offline_dir)
            + getString(R.string.config_geodb_name);
    Log.d("path", path);

    // create a new geodatabase from local path
    final Geodatabase geodatabase = new Geodatabase(path);
    // load the geodatabase
    geodatabase.loadAsync();
    // add feature layer from geodatabase to the ArcGISMap
    geodatabase.addDoneLoadingListener(() -> {
      if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
        // access the geodatabase's feature table Trailheads
        GeodatabaseFeatureTable geodatabaseFeatureTable = geodatabase.getGeodatabaseFeatureTable("Trailheads");
        geodatabaseFeatureTable.loadAsync();
        // create a layer from the geodatabase feature table above and add to map
        final FeatureLayer featureLayer = new FeatureLayer(geodatabaseFeatureTable);
        featureLayer.addDoneLoadingListener(() -> {
          if (featureLayer.getLoadStatus() == LoadStatus.LOADED) {
            // set viewpoint to the location of feature layer's features
            mMapView.setViewpointAsync(new Viewpoint(featureLayer.getFullExtent()));
          } else {
            Toast.makeText(MainActivity.this, "Feature Layer failed to load!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Feature Layer failed to load!");
          }
        });
        // display feature layer to the map view
        mMapView.getMap().getOperationalLayers().add(featureLayer);
      } else {
        Toast.makeText(MainActivity.this, "Geodatabase failed to load!", Toast.LENGTH_LONG).show();
        Log.e(TAG, "Geodatabase failed to load!");
      }
    });
  }

  /**
   * Handle the permissions request response
   */
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      loadGeodatabase();
    } else {
      // report to user that permission was denied
      Toast.makeText(MainActivity.this, getResources().getString(R.string.location_permission_denied),
          Toast.LENGTH_SHORT).show();
    }
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
}
