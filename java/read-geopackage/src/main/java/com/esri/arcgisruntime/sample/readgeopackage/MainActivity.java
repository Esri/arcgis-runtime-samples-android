/*
 * Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.readgeopackage;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.data.GeoPackage;
import com.esri.arcgisruntime.data.GeoPackageFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.GeoPackageRaster;

public class MainActivity extends AppCompatActivity {

  private final static String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private GeoPackage mGeoPackage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a new map centered on Aurora Colorado and add it to the map view
    ArcGISMap map = new ArcGISMap(Basemap.Type.STREETS, 39.7294, -104.8319, 11);
    mMapView.setMap(map);

    // open and load the GeoPackage
    mGeoPackage = new GeoPackage(getExternalFilesDir(null) + getString(R.string.geopackage_path));
    mGeoPackage.loadAsync();
    mGeoPackage.addDoneLoadingListener(() -> {
      if (mGeoPackage.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        String error = "Geopackage failed to load: " + mGeoPackage.getLoadError();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        return;
      }

      // loop through each GeoPackageRaster
      for (GeoPackageRaster geoPackageRaster : mGeoPackage.getGeoPackageRasters()) {
        // create a RasterLayer from the GeoPackageRaster
        RasterLayer rasterLayer = new RasterLayer(geoPackageRaster);

        // set the opacity on the RasterLayer to partially visible
        rasterLayer.setOpacity(0.55f);

        // add the layer to the map
        mMapView.getMap().getOperationalLayers().add(rasterLayer);
      }

      // loop through each GeoPackageFeatureTable
      for (GeoPackageFeatureTable geoPackageFeatureTable : mGeoPackage.getGeoPackageFeatureTables()) {
        // create a FeatureLayer from the GeoPackageFeatureLayer
        FeatureLayer featureLayer = new FeatureLayer(geoPackageFeatureTable);

        // add the layer to the map
        mMapView.getMap().getOperationalLayers().add(featureLayer);
      }
    });
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
}
