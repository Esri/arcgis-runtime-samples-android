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

package com.esri.arcgisruntime.sample.viewpointclouddataoffline;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.layers.PointCloudLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    mSceneView = findViewById(R.id.sceneView);

    // create a scene and add it to the scene view
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);
    mSceneView.setScene(scene);

    // create a camera and initial camera position
    Camera camera = new Camera(32.7321157, -117.150072, 452.282774, 25.481533, 78.0945859, 0.0);

    // set viewpoint for SceneView using camera
    mSceneView.setViewpointCamera(camera);

    // set the base surface with world elevation
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(getString(R.string.elevation_source_url)));
    scene.setBaseSurface(surface);

    // add a PointCloudLayer to the scene by passing the URI of the scene layer package to the constructor
    PointCloudLayer pointCloudLayer = new PointCloudLayer(
        getExternalFilesDir(null) + getString(R.string.scene_layer_package_location));

    // add the PointCloudLayer to the operational layers of the scene
    mSceneView.getScene().getOperationalLayers().add(pointCloudLayer);

    // add a listener to perform operations when the load status of the PointCloudLayer changes
    pointCloudLayer.addDoneLoadingListener(() -> {
      if (pointCloudLayer.getLoadStatus() != LoadStatus.LOADED) {
        // notify user that the PointCloudLayer has failed to load
        String error = "Point cloud layer failed to load: " + pointCloudLayer.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
    mSceneView.resume();
  }

  @Override protected void onPause() {
    mSceneView.pause();
    super.onPause();
  }

  @Override protected void onDestroy() {
    mSceneView.dispose();
    super.onDestroy();
  }
}
