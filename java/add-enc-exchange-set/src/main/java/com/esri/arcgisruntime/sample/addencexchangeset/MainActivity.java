/*
 * Copyright 2019 Esri
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

package com.esri.arcgisruntime.sample.addencexchangeset;

import java.util.Arrays;
import java.util.Collections;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.hydrography.EncCell;
import com.esri.arcgisruntime.hydrography.EncDataset;
import com.esri.arcgisruntime.hydrography.EncEnvironmentSettings;
import com.esri.arcgisruntime.hydrography.EncExchangeSet;
import com.esri.arcgisruntime.layers.EncLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private Envelope mCompleteExtent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(Basemap.createOceans());
    // set the map to be displayed in this view
    mMapView.setMap(map);

    // set paths using ENC environment settings
    // point to the folder containing hydrography resources
    EncEnvironmentSettings
        .setResourcePath(getExternalFilesDir(null) + getString(R.string.hydrography_directory));
    // use the app's cache to store processed System Electronic Navigational Chart (SENC) data
    EncEnvironmentSettings.setSencDataPath(getExternalCacheDir().getPath());

    // create the Exchange Set passing an array of paths. Update sets can be loaded alongside base data
    EncExchangeSet encExchangeSet = new EncExchangeSet(
        Collections.singleton(getExternalFilesDir(null) + getString(R.string.enc_path)));
    encExchangeSet.loadAsync();
    encExchangeSet.addDoneLoadingListener(() -> {
      if (encExchangeSet.getLoadStatus() == LoadStatus.LOADED) {
        // add each data set's Enc cell as an ENC layer
        for (EncDataset encDataset : encExchangeSet.getDatasets()) {
          // create an ENC layer with an ENC cell using the dataset
          EncLayer encLayer = new EncLayer(new EncCell(encDataset));
          // add the ENC layer to the map's operational layers
          mMapView.getMap().getOperationalLayers().add(encLayer);
          encLayer.addDoneLoadingListener(() -> {
            if (encLayer.getLoadStatus() == LoadStatus.LOADED) {
              Envelope extent = encLayer.getFullExtent();
              // combine extents of each layer
              if (mCompleteExtent == null) {
                mCompleteExtent = extent;
              } else {
                mCompleteExtent = GeometryEngine.combineExtents(Arrays.asList(mCompleteExtent, extent));
              }
              // set the view point to the extent of all enc layers
              mMapView.setViewpointAsync(new Viewpoint(mCompleteExtent));
            } else {
              String error = "Error loading ENC layer: " + encLayer.getLoadError().getMessage();
              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
              Log.e(TAG, error);
            }
          });
        }
      } else {
        String error = "Error loading ENC exchange set: " + encExchangeSet.getLoadError().getMessage();
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
