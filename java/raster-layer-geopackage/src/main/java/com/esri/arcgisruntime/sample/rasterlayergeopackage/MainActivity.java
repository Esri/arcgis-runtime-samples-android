/*
 * Copyright 2018 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.rasterlayergeopackage;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.GeoPackage;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.Raster;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private GeoPackage mGeoPackage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a map with the Basemap Style light gray canvas
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_LIGHT_GRAY);
    // set the map to be displayed in this view
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint( 39.7294, -104.8319, 1000000));

    // open the GeoPackage
    mGeoPackage = new GeoPackage(getExternalFilesDir(null) + getString(R.string.geopackage_path));
    mGeoPackage.loadAsync();
    mGeoPackage.addDoneLoadingListener(() -> {
      if (mGeoPackage.getLoadStatus() == LoadStatus.LOADED) {
        if (!mGeoPackage.getGeoPackageRasters().isEmpty()) {
          // read raster images and get the first one
          Raster geoPackageRaster = mGeoPackage.getGeoPackageRasters().get(0);
          // create a layer to show the raster
          RasterLayer geoPackageRasterLayer = new RasterLayer(geoPackageRaster);
          // add the image as a raster layer to the map (with default symbology)
          mMapView.getMap().getOperationalLayers().add(geoPackageRasterLayer);
        } else {
          String emptyMessage = "No rasters found in this GeoPackage!";
          Toast.makeText(this, emptyMessage, Toast.LENGTH_LONG).show();
          Log.e(TAG, emptyMessage);
        }
      } else {
        String error = "GeoPackage failed to load!";
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
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
