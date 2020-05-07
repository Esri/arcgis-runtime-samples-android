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

package com.esri.arcgisruntime.sample.rasterlayerfile;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.Raster;

/**
 * A sample class which demonstrates loading a Raster from the local device.
 */
public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // retrieve the MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a raster from a local raster file
    Raster raster = new Raster(getExternalFilesDir(null) + getString(R.string.shasta_tif));
    // create a raster layer
    final RasterLayer rasterLayer = new RasterLayer(raster);
    // create a Map with imagery basemap
    ArcGISMap map = new ArcGISMap(Basemap.createImagery());
    // add the map to a map view
    mMapView.setMap(map);
    // add the raster as an operational layer
    map.getOperationalLayers().add(rasterLayer);
    // set viewpoint on the raster
    rasterLayer.addDoneLoadingListener(() -> mMapView.setViewpointGeometryAsync(rasterLayer.getFullExtent(), 50));
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
