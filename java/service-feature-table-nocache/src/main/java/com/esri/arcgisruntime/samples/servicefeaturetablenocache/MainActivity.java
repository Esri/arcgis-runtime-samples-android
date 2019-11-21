/* Copyright 2016 Esri
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
package com.esri.arcgisruntime.samples.servicefeaturetablenocache;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);

    // create a map with the topographic basemap
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    //set an initial viewpoint
    map.setInitialViewpoint(new Viewpoint(new Envelope(-1.30758164047166E7, 4014771.46954516, -1.30730056797177E7
        , 4016869.78617381, SpatialReferences.getWebMercator())));

    // create feature layer with its service feature table
    // create the service feature table
    ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.sample_service_url));

    //explicitly set the mode to on interaction no cache (every interaction (pan, query etc) new features will be
      // requested
    serviceFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_NO_CACHE);

    // create the feature layer using the service feature table
    FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);

    // add the layer to the map
    map.getOperationalLayers().add(featureLayer);

    // set the map to be displayed in the mapview
    mMapView.setMap(map);

  }

  @Override
  protected void onPause() {
    super.onPause();
    // pause MapView
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume MapView
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // dispose MapView
    mMapView.dispose();
  }
}
