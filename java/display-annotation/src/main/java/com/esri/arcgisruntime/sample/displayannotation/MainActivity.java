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

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    AuthenticationManager.setTrustAllSigners(true);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    AnnotationLayer annotationLayer0 = new AnnotationLayer(
        "https://craigwilliams.esri.com/server/rest/services/Loudoun/FeatureServer/0");

    AnnotationLayer annotationLayer1 = new AnnotationLayer(
        "https://craigwilliams.esri.com/server/rest/services/Loudoun/FeatureServer/1");

    AnnotationLayer annotationLayer2 = new AnnotationLayer(
        "https://craigwilliams.esri.com/server/rest/services/Loudoun/FeatureServer/2");

    AnnotationLayer annotationLayer3 = new AnnotationLayer(
        "https://craigwilliams.esri.com/server/rest/services/Loudoun/FeatureServer/3");

    annotationLayer1.loadAsync();
    annotationLayer1.addDoneLoadingListener(() -> {
      if (annotationLayer1.getLoadStatus() == LoadStatus.LOADED) {

        // create a map and set it to the map view
        ArcGISMap map = new ArcGISMap(annotationLayer1.getSpatialReference());

        map.setBasemap(Basemap.createLightGrayCanvas());
        map.getOperationalLayers().add(annotationLayer0);
        map.getOperationalLayers().add(annotationLayer1);
        map.getOperationalLayers().add(annotationLayer2);
        map.getOperationalLayers().add(annotationLayer3);
        mMapView.setMap(map);

        mMapView.setViewpointAsync(
            new Viewpoint(new Point(annotationLayer1.getFullExtent().getCenter().getX(),
                annotationLayer1.getFullExtent().getCenter().getY(),
                annotationLayer1.getSpatialReference()), 2000));
      } else {
        Log.e("stuff", "Annotation layer failed to load: " + annotationLayer1.getLoadError());
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
