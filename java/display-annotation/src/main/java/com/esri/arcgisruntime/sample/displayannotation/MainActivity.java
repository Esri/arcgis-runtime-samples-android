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
import android.util.Log;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.AnnotationLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    AuthenticationManager.setTrustAllSigners(true);
    AuthenticationManager.setAuthenticationChallengeHandler(new DefaultAuthenticationChallengeHandler(this));

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    AnnotationLayer annotationLayer = new AnnotationLayer("https://ec2-35-172-138-30.compute-1.amazonaws.com:6443/arcgis/rest/services/RiversAnnotation/FeatureServer/1");

    // create a map and set it to the map view
    ArcGISMap map = new ArcGISMap(Basemap.createOpenStreetMap());
    map.getOperationalLayers().add(annotationLayer);
    mMapView.setMap(map);

    annotationLayer.loadAsync();
    annotationLayer.addDoneLoadingListener(() -> {
      if (annotationLayer.getLoadStatus() == LoadStatus.LOADED) {
        Log.d(TAG, "Annotation layer loaded");
        mMapView.setViewpointAsync(
            new Viewpoint(new Point(annotationLayer.getFullExtent().getCenter().getX(),
                annotationLayer.getFullExtent().getCenter().getY(),
                annotationLayer.getSpatialReference()), 2000));
      } else {
        Log.e(TAG, "Annotation layer failed to load: " + annotationLayer.getLoadError().getCause().getMessage());
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
