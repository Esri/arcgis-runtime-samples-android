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

package com.esri.arcgisruntime.sample.syncmapandsceneviewpoints;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.GeoView;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get a reference to the map view and set a map to it
    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);
    mMapView.setMap(map);

    // get a reference to the scene view and set a scene to it
    mSceneView = findViewById(R.id.sceneView);
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);
    mSceneView.setScene(scene);

    // on viewpoint change synchronize viewpoints
    mMapView.addViewpointChangedListener(viewpointChangedEvent -> synchronizeViewpoints(mMapView, mSceneView));
    mSceneView.addViewpointChangedListener(viewpointChangedEvent -> synchronizeViewpoints(mSceneView, mMapView));
  }

  /**
   * Synchronizes the viewpoint across GeoViews when the user is navigating.
   */
  private static void synchronizeViewpoints(GeoView navigatingGeoView, GeoView geoViewToSync) {
    if (navigatingGeoView.isNavigating()) {
      Viewpoint navigatingViewpoint = navigatingGeoView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE);
      geoViewToSync.setViewpoint(navigatingViewpoint);
    }
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    mSceneView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
    mSceneView.resume();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    mSceneView.dispose();
    super.onDestroy();
  }
}
