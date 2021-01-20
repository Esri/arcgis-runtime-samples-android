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

package com.esri.arcgisruntime.sample.colormaprenderer;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.ColormapRenderer;
import com.esri.arcgisruntime.raster.Raster;

/**
 * A sample class which demonstrates the ColorMapRenderer.
 */
public class MainActivity extends AppCompatActivity {

  private final static String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // retrieve the MapView from layout
    mMapView = findViewById(R.id.mapView);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // create a raster from a local raster file
    Raster raster = new Raster(getExternalFilesDir(null) + getString(R.string.shasta_b_w));
    // create a raster layer
    final RasterLayer rasterLayer = new RasterLayer(raster);
    // create a Map with imagery basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);
    // add the map to a map view
    mMapView.setMap(map);
    // add the raster as an operational layer
    map.getOperationalLayers().add(rasterLayer);
    // create a color map where values 0-149 are red (Color.RED) and 150-250 are yellow (Color.Yellow)
    List<Integer> colors = new ArrayList<>();
    for (int i = 0; i <= 250; i++) {
      if (i < 150) {
        colors.add(i, Color.RED);
      } else {
        colors.add(i, Color.YELLOW);
      }
    }
    // create a colormap renderer
    ColormapRenderer colormapRenderer = new ColormapRenderer(colors);
    // set the ColormapRenderer on the RasterLayer
    rasterLayer.setRasterRenderer(colormapRenderer);
    // set Viewpoint on the Raster
    rasterLayer.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        if (rasterLayer.getLoadStatus() == LoadStatus.LOADED) {
          mMapView.setViewpointGeometryAsync(rasterLayer.getFullExtent(), 50);
        } else {
          String error = "RasterLayer failed to load: " + rasterLayer.getLoadError().getMessage();
          Log.e(TAG, error);
          Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
        }
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
