/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.featurelayergeopackage;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.GeoPackage;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private ArcGISMap mMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    mMap = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 39.7294, -104.8319, 12);
    // set the map to be displayed in this view
    mMapView.setMap(mMap);

    // Get the full path to the local GeoPackage
    String geoPackagePath = getExternalFilesDir(null) + getString(R.string.aurora_co_gpkg);

    // Open the GeoPackage
    GeoPackage geoPackage = new GeoPackage(geoPackagePath);
    geoPackage.loadAsync();
    geoPackage.addDoneLoadingListener(() -> {
      if (geoPackage.getLoadStatus() == LoadStatus.LOADED) {
        // Read the feature tables and get the first one
        FeatureTable geoPackageTable = geoPackage.getGeoPackageFeatureTables().get(0);

        // Make sure a feature table was found in the package
        if (geoPackageTable == null) {
          Toast.makeText(MainActivity.this, "No feature table found in the package!", Toast.LENGTH_LONG).show();
          Log.e(TAG, "No feature table found in this package!");
          return;
        }

        // Create a layer to show the feature table
        FeatureLayer featureLayer = new FeatureLayer(geoPackageTable);

        // Add the feature table as a layer to the map (with default symbology)
        mMap.getOperationalLayers().add(featureLayer);
      } else {
        Toast.makeText(MainActivity.this, "GeoPackage failed to load! " + geoPackage.getLoadError(), Toast.LENGTH_LONG).show();
        Log.e(TAG, "GeoPackage failed to load!" + geoPackage.getLoadError());
      }
    });
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
