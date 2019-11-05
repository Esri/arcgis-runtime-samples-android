/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.displayannotation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.AnnotationLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a map with a topographic basemap
    ArcGISMap map = new ArcGISMap(Basemap.Type.LIGHT_GRAY_CANVAS, 55.882436, -2.725610, 13);

    // create a feature layer from a feature service
    FeatureLayer riverFeatureLayer = new FeatureLayer(new ServiceFeatureTable(getString(R.string.river_feature_layer_url)));
    // add the feature layer to the map
    map.getOperationalLayers().add(riverFeatureLayer);

    // create an annotation layer from a feature service
    AnnotationLayer annotationLayer = new AnnotationLayer(getString(R.string.annotation_feature_service_url));
    // add the annotation layer to the map
    map.getOperationalLayers().add(annotationLayer);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    // set the map to the map view
    mMapView.setMap(map);
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
