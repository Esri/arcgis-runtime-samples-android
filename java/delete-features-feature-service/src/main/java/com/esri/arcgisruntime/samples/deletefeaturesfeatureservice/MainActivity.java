/*
 * Copyright 2019 Esri
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

package com.esri.arcgisruntime.samples.deletefeaturesfeatureservice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  private ServiceFeatureTable mFeatureTable;

  private FeatureLayer mFeatureLayer;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = findViewById(R.id.mapView);

    // create a map with streets basemap
    ArcGISMap map = new ArcGISMap(Basemap.Type.STREETS, 40, -95, 4);

    // create service feature table from URL
    mFeatureTable = new ServiceFeatureTable(getString(R.string.feature_layer_url));

    // create a feature layer from table
    mFeatureLayer = new FeatureLayer(mFeatureTable);

    // add the layer to the ArcGISMap
    map.getOperationalLayers().add(mFeatureLayer);

    // set ArcGISMap to be displayed in map view
    mMapView.setMap(map);
  }
}