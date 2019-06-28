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

package com.esri.arcgisruntime.sample.annotationsublayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.esri.arcgisruntime.layers.AnnotationLayer;
import com.esri.arcgisruntime.layers.AnnotationSublayer;
import com.esri.arcgisruntime.layers.LayerContent;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 16);
    // set the map to be displayed in this view
    mMapView.setMap(map);
    AnnotationLayer annotationLayer = new AnnotationLayer(
        "https://craigwilliams.esri.com/server/rest/services/Loudoun/FeatureServer/1");
    map.getOperationalLayers().add(annotationLayer);

    LayerContent sublayerContent = annotationLayer.getSubLayerContents().get(0);
    AnnotationSublayer sublayer = (AnnotationSublayer) sublayerContent;
    sublayer.getDefinitionExpression();
    TextView minScaleTextView = findViewById(R.id.currMinScale);
    minScaleTextView.setText(String.valueOf(sublayer.getMinScale()));
    TextView maxScaleTextView = findViewById(R.id.currMaxScale);
    maxScaleTextView.setText(String.valueOf(sublayer.getMaxScale()));
    TextView isVisibleTextView = findViewById(R.id.currIsVisible);
    mMapView.addMapScaleChangedListener(mapScaleChangedEvent -> isVisibleTextView
        .setText(String.valueOf(sublayer.isVisibleAtScale(mMapView.getMapScale()))));
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
