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

package com.esri.arcgisruntime.sample.addpointscenelayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSceneView = findViewById(R.id.sceneView);

    // create a scene with a basemap and add it to the scene view
    ArcGISScene scene = new ArcGISScene(Basemap.Type.IMAGERY);
    mSceneView.setScene(scene);

    // set the base surface with world elevation
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(getString(R.string.elevation_image_service)));
    scene.setBaseSurface(surface);

    // add a point scene layer with points at world airport locations
    ArcGISSceneLayer pointSceneLayer = new ArcGISSceneLayer(getString(R.string.world_airports_scene_layer));
    scene.getOperationalLayers().add(pointSceneLayer);
  }

  @Override
  protected void onPause() {
    mSceneView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSceneView.resume();
  }

  @Override
  protected void onDestroy() {
    mSceneView.dispose();
    super.onDestroy();
  }
}
