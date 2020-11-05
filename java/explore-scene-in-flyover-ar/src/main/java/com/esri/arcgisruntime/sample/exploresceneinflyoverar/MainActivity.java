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

package com.esri.arcgisruntime.sample.exploresceneinflyoverar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.IntegratedMeshLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.NavigationConstraint;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.toolkit.ar.ArcGISArView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private ArcGISArView mArView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    requestCameraPermission();
  }

  private void displaySceneInAr() {
    mArView = findViewById(R.id.arView);
    mArView.registerLifecycle(getLifecycle());
    // disable touch interactions with the scene view
    mArView.getSceneView().setOnTouchListener((view, motionEvent) -> true);

    // create scene with imagery basemap
    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());

    // create an integrated mesh layer
    IntegratedMeshLayer integratedMeshLayer = new IntegratedMeshLayer(
        getString(R.string.girona_integrated_mesh_layer_url));
    scene.getOperationalLayers().add(integratedMeshLayer);

    // create an elevation source and add it to the scene
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getString(R.string.world_terrain_service_url));
    scene.getBaseSurface().getElevationSources().add(elevationSource);
    scene.getBaseSurface().setNavigationConstraint(NavigationConstraint.NONE);

    // add the scene to the scene view
    mArView.getSceneView().setScene(scene);

    // wait for the layer to load, then set the AR camera
    integratedMeshLayer.addDoneLoadingListener(() -> {
      if (integratedMeshLayer.getLoadStatus() == LoadStatus.LOADED) {
        Envelope envelope = integratedMeshLayer.getFullExtent();
        Camera camera = new Camera(envelope.getCenter().getY(), envelope.getCenter().getX(), 250, 0, 90, 0);
        mArView.setOriginCamera(camera);
      } else {
        String error =
            getString(R.string.error_loading_integrated_mesh_layer) + integratedMeshLayer.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });

    // set the translation factor to enable rapid movement through the scene
    mArView.setTranslationFactor(1000);
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestCameraPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.CAMERA };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      displaySceneInAr();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      displaySceneInAr();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.camera_permission_required_for_ar), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onPause() {
    if (mArView != null) {
      mArView.stopTracking();
    }
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mArView != null) {
      mArView.startTracking(ArcGISArView.ARLocationTrackingMode.IGNORE);
    }
  }
}
