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

package com.esri.tokenauthentication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.samples.tokenauthentication.R;
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = findViewById(R.id.mapView);

    // set up an authentication handler to take credentials for access to the protected layer
    AuthenticationChallengeHandler handler = new DefaultAuthenticationChallengeHandler(this);
    AuthenticationManager.setAuthenticationChallengeHandler(handler);

    // create a ArcGISMap with a topographic basemap
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());

    // center for initial viewpoint of map
    Point center = new Point(-12649954, 7123527, SpatialReference.create(3857));

    // set initial viewpoint of map
    map.setInitialViewpoint(new Viewpoint(center, 167233023));

    // create a layer with dynamically generated map images
    ArcGISMapImageLayer mapImageLayer = new ArcGISMapImageLayer(getString(R.string.map_image_layer_url));

    // add the layer to the operational layers of the map
    map.getOperationalLayers().add(mapImageLayer);

    // set the map to be displayed in the map view
    mMapView.setMap(map);
  }

  @Override protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
