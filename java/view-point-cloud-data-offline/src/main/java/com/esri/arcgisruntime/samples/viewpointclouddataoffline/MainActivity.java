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

package com.esri.arcgisruntime.samples.viewpointclouddataoffline;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.esri.arcgisruntime.layers.PointCloudLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private static final int PERMISSIONS_REQUEST_CODE = 1;

  private static final String[] PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE };

  private SceneView mSceneView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSceneView = findViewById(R.id.sceneView);

    // Create a scene and add it to the scene view
    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());
    mSceneView.setScene(scene);

    // Create a camera and initial camera position
    Camera camera = new Camera(32.7321157, -117.150072, 452.282774, 25.481533, 78.0945859, 0.0);

    // Set viewpoint for SceneView using camera
    mSceneView.setViewpointCamera(camera);

    // Set the base surface with world elevation
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(getString(R.string.elevation_source_url)));
    scene.setBaseSurface(surface);

    requestReadPermission();
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    if (ContextCompat.checkSelfPermission(this, PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED) {
      createPointCloudLayer();
    } else {
      // Request permission
      ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_CODE);
    }
  }

  /**
   * Handle the permissions request response
   */
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      createPointCloudLayer();
    } else {
      // Report to user that permission was denied
      String error = getString(R.string.read_permission_denied_message);
      Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
      Log.e(TAG, error);
    }
  }

  private void createPointCloudLayer() {
    // Add a PointCloudLayer to the scene by passing the URI of the scene layer package to the constructor
    PointCloudLayer pointCloudLayer = new PointCloudLayer(
        Environment.getExternalStorageDirectory() + getString(R.string.scene_layer_package_location));

    // Add a listener to perform operations when the load status of the PointCloudLayer changes
    pointCloudLayer.addLoadStatusChangedListener(loadStatusChangedEvent -> {

      // When PointCloudLayer loads
      if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.LOADED) {
        // Add the PointCloudLayer to the operational layers of the scene
        mSceneView.getScene().getOperationalLayers().add(pointCloudLayer);
      } else if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        // Notify user that the PointCloudLayer has failed to load
        String error = getString(R.string.point_cloud_layer_load_failure_message);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });

    // Load the PointCloudLayer asynchronously
    pointCloudLayer.loadAsync();
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
