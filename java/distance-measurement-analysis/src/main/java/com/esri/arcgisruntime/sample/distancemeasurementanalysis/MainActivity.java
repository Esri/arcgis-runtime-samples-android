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

package com.esri.arcgisruntime.sample.distancemeasurementanalysis;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
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

  private LocationDistance

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());

    // create SceneView from layout
    mSceneView = findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    // add base surface for elevation data
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource("http://elevation3d.arcgis" +
            ".com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer"));
    surface.getElevationSources().add(new ArcGISTiledElevationSource("https://tiles.arcgis.com/tiles/d3voDfTFbHOCRwVR/arcgis/rest/services/MNT_IDF/ImageServer"));
    scene.setBaseSurface(surface);

    final String buildings =  "http://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/Buildings_Brest/SceneServer/layers/0";
    ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(buildings);
    scene.getOperationalLayers().add(sceneLayer);

    // create analysis overlay and add it to scene
    AnalysisOverlay analysisOverlay = new AnalysisOverlay();
    mSceneView.getAnalysisOverlays().addAll(analysisOverlay);

    //initialize a distance measurement and add it to the analyssis overlay
    Point start = new Point(-4.494677, 48.384472, 24.772694, SpatialReferences.getWgs84());
    Point end = new Point(-4.495646, 48.384377, 58.501115, SpatialReferences.getWgs84());
    distanceMeasurement = new LocationDistanceMeasurement(start, end);
    analysisOverlay.getAnalyses().add(distanceMeasurement);
    // add a camera and initial camera position
    Camera camera = new Camera(28.4, 83.9, 10010.0, 10.0, 80.0, 0.0);
    mSceneView.setViewpointCamera(camera);
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

  @Override protected void onDestroy() {
    super.onDestroy();
    // dispose SceneView
    mSceneView.dispose();
  }
}
