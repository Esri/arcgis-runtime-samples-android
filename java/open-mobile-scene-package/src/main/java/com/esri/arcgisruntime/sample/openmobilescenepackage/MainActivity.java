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

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.MobileScenePackage;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();
  private SceneView mSceneView;
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private MobileScenePackage mMobileScenePackage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSceneView = findViewById(R.id.sceneView);

    // create a mobile scene package from a path to the mspk
    mMobileScenePackage = new MobileScenePackage(getExternalFilesDir(null) + getString(R.string.philadelphia_mspk));
    mMobileScenePackage.addDoneLoadingListener(() -> {
      // check if the mobile scene package loaded and has a scene
      if (mMobileScenePackage.getLoadStatus() == LoadStatus.LOADED && !mMobileScenePackage.getScenes().isEmpty()) {
        // set the scene view's scene to the first in the mobile scene package
        mSceneView.setScene(mMobileScenePackage.getScenes().get(0));
        // set the scene view's camera
        mSceneView.setViewpointCamera(new Camera(39.9625,-75.1771,310,139,75,0.0));
      } else {
        String error = "Failed to load mobile scene package: " + mMobileScenePackage.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
    mMobileScenePackage.loadAsync();
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
