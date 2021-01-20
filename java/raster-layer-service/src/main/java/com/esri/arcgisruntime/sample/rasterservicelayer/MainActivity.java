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

package com.esri.arcgisruntime.sample.rasterservicelayer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.ImageServiceRaster;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // create a Dark Gray Canvas Vector basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_DARK_GRAY);

    // add the map to a map view
    mMapView.setMap(map);

    // zoom into San Francisco Bay
    mMapView.setViewpointCenterAsync(new Point(-13643095.660131, 4550009.846004, SpatialReferences.getWebMercator()),
        100000);

    // create image service raster as raster layer
    final ImageServiceRaster imageServiceRaster = new ImageServiceRaster(getString(R.string.image_service_url));
    final RasterLayer rasterLayer = new RasterLayer(imageServiceRaster);

    // check that the raster layer loaded
    rasterLayer.addDoneLoadingListener(() -> {
      if (rasterLayer.getLoadStatus() != LoadStatus.LOADED) {
        String error = "Raster layer failed to load.";
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });

    // add raster layer as map operational layer
    map.getOperationalLayers().add(rasterLayer);
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
