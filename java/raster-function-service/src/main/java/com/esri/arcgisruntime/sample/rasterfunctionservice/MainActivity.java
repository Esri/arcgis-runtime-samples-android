/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.rasterfunctionservice;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.ImageServiceRaster;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.raster.RasterFunction;
import com.esri.arcgisruntime.raster.RasterFunctionArguments;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();
  private MapView mMapView;
  private Button mRasterFunctionButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate views from layout
    mMapView = findViewById(R.id.mapView);
    mRasterFunctionButton = findViewById(R.id.rasterButton);
    mRasterFunctionButton.setEnabled(false);
    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_DARK_GRAY);
    final ImageServiceRaster imageServiceRaster = new ImageServiceRaster(getString(R.string.image_service_raster_url));
    final RasterLayer imageRasterLayer = new RasterLayer(imageServiceRaster);
    map.getOperationalLayers().add(imageRasterLayer);
    // zoom to the extent of the raster service
    imageRasterLayer.addDoneLoadingListener(() -> {
      if (imageRasterLayer.getLoadStatus() == LoadStatus.LOADED) {
        // get the center point
        Point centerPnt = imageServiceRaster.getServiceInfo().getFullExtent().getCenter();
        mMapView.setViewpointCenterAsync(centerPnt, 55000000);
        mRasterFunctionButton.setEnabled(true);
      } else {
        String error = "Error loading image raster layer: " + imageRasterLayer.getLoadError();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });

    // update the raster with simplified hillshade
    mRasterFunctionButton.setOnClickListener(v -> applyRasterFunction(imageServiceRaster));

    // set the map to be displayed in this view
    mMapView.setMap(map);
  }

  private void applyRasterFunction(Raster raster) {
    // create raster function from json string
    RasterFunction rasterFuntionFromJson = RasterFunction.fromJson(getString(R.string.hillshade_simplified));
    // get parameter name value pairs used by hillside
    RasterFunctionArguments rasterFunctionArguments = rasterFuntionFromJson.getArguments();
    // get a list of raster names associated with the raster function
    List<String> rasterNames = rasterFunctionArguments.getRasterNames();
    rasterFunctionArguments.setRaster(rasterNames.get(0), raster);
    // create raster as raster layer
    raster = new Raster(rasterFuntionFromJson);
    RasterLayer hillshadeLayer = new RasterLayer(raster);
    mMapView.getMap().getOperationalLayers().add(hillshadeLayer);
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
