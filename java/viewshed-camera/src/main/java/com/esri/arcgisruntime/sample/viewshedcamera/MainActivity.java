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

package com.esri.arcgisruntime.sample.viewshedcamera;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

import com.esri.arcgisruntime.geoanalysis.LocationViewshed;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());

    // add the scene to a scene view
    mSceneView = findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    // set the camera to the scene view
    Camera camera = new Camera(48.3808, -4.49492, 48.2511, 344.488, 74.1212, 0.0);
    mSceneView.setViewpointCamera(camera);

    // add base surface for elevation data to the scene view
    Surface surface = new Surface();
    ArcGISTiledElevationSource brestElevationSource = new ArcGISTiledElevationSource(getString(R.string.brest_dtm));
    surface.getElevationSources().add(brestElevationSource);
    scene.setBaseSurface(surface);

    // add a scene layer to the scene
    ArcGISSceneLayer brestBuildingLayer = new ArcGISSceneLayer(getString(R.string.brest_building_layer));
    scene.getOperationalLayers().add(brestBuildingLayer);

    // create a viewshed from the camera
    LocationViewshed viewshed = new LocationViewshed(camera, 1.0, 500.0);

    // create an analysis overlay to add the viewshed to the scene view
    AnalysisOverlay analysisOverlay = new AnalysisOverlay();
    analysisOverlay.getAnalyses().add(viewshed);
    mSceneView.getAnalysisOverlays().add(analysisOverlay);

    // create a button to update the viewshed with the current camera
    Button cameraButton = findViewById(R.id.updateViewshedButton);
    cameraButton.setOnClickListener(view -> viewshed.updateFromCamera(mSceneView.getCurrentViewpointCamera()));
  }

  @Override
  protected void onPause() {
    super.onPause();
    // pause SceneView
    mSceneView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume SceneView
    mSceneView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // dispose SceneView
    mSceneView.dispose();
  }
}
