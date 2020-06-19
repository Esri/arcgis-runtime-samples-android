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

package com.esri.arcgisruntime.sample.wmtslayer;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.esri.arcgisruntime.layers.WmtsLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.wmts.WmtsLayerInfo;
import com.esri.arcgisruntime.ogc.wmts.WmtsService;
import com.esri.arcgisruntime.ogc.wmts.WmtsServiceInfo;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();
  private MapView mMapView;
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private WmtsService mWmtsService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create an ArcGIS map
    ArcGISMap map = new ArcGISMap();
    mMapView.setMap(map);
    // create wmts service from url string
    mWmtsService = new WmtsService(getString(R.string.wmts_url));
    mWmtsService.addDoneLoadingListener(() -> {
      if (mWmtsService.getLoadStatus() == LoadStatus.LOADED) {
        // get service info
        WmtsServiceInfo wmtsServiceInfo = mWmtsService.getServiceInfo();
        // get the first layer id
        List<WmtsLayerInfo> layerInfoList = wmtsServiceInfo.getLayerInfos();
        // create WMTS layer from layer info
        WmtsLayer wmtsLayer = new WmtsLayer(layerInfoList.get(0));
        // set the basemap of the map with WMTS layer
        map.setBasemap(new Basemap(wmtsLayer));
      } else {
        String error = "Error loading WMTS Service: " + mWmtsService.getLoadError();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });
    mWmtsService.loadAsync();
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
