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

package com.esri.arcgisruntime.sample.wmslayerurl;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.layers.WmsLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with a light gray basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_LIGHT_GRAY);
    // set the map to be displayed in this view
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint( 39.8, -98.6, 100000000));

    // hold a list of uniquely-identifying WMS layer names to display
    List<String> wmsLayerNames = new ArrayList<>();
    wmsLayerNames.add("1");

    // create a new WMS layer displaying the specified layers from the service
    WmsLayer wmsLayer = new WmsLayer(getString(R.string.wms_layer_url), wmsLayerNames);

    // add the layer to the map
    map.getOperationalLayers().add(wmsLayer);
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
