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

package com.esri.arcgisruntime.samples.createterrainfromalocaltilepackage;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
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

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);

    // add the scene to the sceneview
    mSceneView.setScene(scene);

    // specify the initial camera position
    Camera camera = new Camera(36.525, -121.80, 300.0, 180, 80.0, 0.0);
    mSceneView.setViewpointCamera(camera);

    // add a ArcGISTiledElevationSource to the scene by passing the URI of the local tile package to the constructor
    ArcGISTiledElevationSource tiledElevationSource = new ArcGISTiledElevationSource(
        getExternalFilesDir(null) + getString(R.string.local_tile_package_location));

    // add a listener to perform operations when the load status of the ArcGISTiledElevationSource changes
    tiledElevationSource.addLoadStatusChangedListener(loadStatusChangedEvent -> {
      // when ArcGISTiledElevationSource loads
      if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.LOADED) {
        // add the ArcGISTiledElevationSource to the elevation sources of the scene
        mSceneView.getScene().getBaseSurface().getElevationSources().add(tiledElevationSource);
      } else if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        // notify user that the ArcGISTiledElevationSource has failed to load
        logErrorToUser(getString(R.string.error_tiled_elevation_source_load_failure_message));
      }
    });

    // load the ArcGISTiledElevationSource asynchronously
    tiledElevationSource.loadAsync();
  }

  private void logErrorToUser(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    Log.e(TAG, message);
  }
}
