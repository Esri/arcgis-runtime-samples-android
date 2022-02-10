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

package com.esri.arcgisruntime.sample.displayscene;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);

    //[DocRef: Name=Display Scene-android, Category=Work with 3D, Topic=Display a scene]
    // create SceneView from layout
    mSceneView = (SceneView) findViewById(R.id.sceneView);
    mSceneView.setScene(scene);
    //[DocRef: END]

    //[DocRef: Name=Add elevation to base surface-android, Category=Work with 3D, Topic=Display a scene,
    // RemoveChars=getResources().getString(R.string.elevation_image_service),
    // ReplaceChars=http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer]
    // create an elevation source, and add this to the base surface of the scene
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getResources().getString(R.string.elevation_image_service));
    scene.getBaseSurface().getElevationSources().add(elevationSource);
    //[DocRef: END]

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
