/*
 * Copyright 2019 Esri
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

package com.esri.arcgisruntime.sample.openmobilescenepackage;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.MobileScenePackage;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();
  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSceneView = findViewById(R.id.sceneView);
    // create a scene and add it to the scene view
    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());

    // add base surface for elevation data
    final Surface surface = new Surface();
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getString(R.string.elevation_image_service_url));
    surface.getElevationSources().add(elevationSource);
    scene.setBaseSurface(surface);

    requestReadPermission();
  }

  /**
   * Check if the mobile scene package supports direct read and, if not, unpack the mobile scene package to the cache.
   * Then call loadMobileScenePackage on with the mobile scene package.
   */
  private void checkReadSupport() {
    final String mspkPath = Environment.getExternalStorageDirectory() + getString(R.string.mspk_path);
    String mspkCachePath = getCacheDir().getPath() + getString(R.string.mspk_cache_path);

    // check if direct read is supported by the mobile scene package
    ListenableFuture<Boolean> isDirectReadSupportedFuture = MobileScenePackage.isDirectReadSupportedAsync(mspkPath);
    isDirectReadSupportedFuture.addDoneListener(() -> {
      try {
        if (isDirectReadSupportedFuture.get()) {
          // load the mobile scene package from the direct read path directory
          MobileScenePackage directReadMSPK = new MobileScenePackage(mspkPath);
          loadMobileScenePackage(directReadMSPK);
        } else {
          // unpack the mobile scene package and store it in the app's cache directory
          MobileScenePackage.unpackAsync(mspkPath, mspkCachePath).addDoneListener(() -> {
            MobileScenePackage unpackedMSPK = new MobileScenePackage(mspkCachePath);
            loadMobileScenePackage(unpackedMSPK);
          });
        }
      } catch (Exception e) {
        String error = "Mobile scene package direct read could not be determined: " + e.getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Loads the mobile scene package asynchronously, and once it has loaded, sets the first scene within the package to
   * the scene view.
   */
  private void loadMobileScenePackage(MobileScenePackage mobileScenePackage) {
    mobileScenePackage.addDoneLoadingListener(() -> {
      if (mobileScenePackage.getLoadStatus() == LoadStatus.LOADED && !mobileScenePackage.getScenes().isEmpty()) {
        mSceneView.setScene(mobileScenePackage.getScenes().get(0));
      } else {
        String error = "Failed to load mobile scene package: " + mobileScenePackage.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
    mobileScenePackage.loadAsync();
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

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      checkReadSupport();
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
      checkReadSupport();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }
}
