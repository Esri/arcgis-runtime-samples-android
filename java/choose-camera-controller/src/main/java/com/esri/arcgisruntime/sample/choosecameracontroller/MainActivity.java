/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.choosecameracontroller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.ModelSceneSymbol;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;
  private Graphic mPlane3D;
  private GraphicsOverlay mSceneOverlay;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // load plane model from assets into cache directory
    copyFileFromAssetsToCache(getString(R.string.bristol_model));

    // create a graphics overlay for the scene
    mSceneOverlay = new GraphicsOverlay();
    mSceneOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.ABSOLUTE);
    mSceneView.getGraphicsOverlays().add(mSceneOverlay);

    // create a scene and add it to the scene view
    mSceneView = findViewById(R.id.sceneView);
    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());
    mSceneView.setScene(scene);

    loadModel().addDoneLoadingListener(() -> {
      // TODO
    });
  }

  /**
   * Load the plane model from the cache, use to construct a Model Scene Symbol and add it to the scene's graphic overlay.
   */
  private ModelSceneSymbol loadModel() {
    // create a graphic with a ModelSceneSymbol of a plane to add to the scene
    String pathToModel = getCacheDir() + File.separator + getString(R.string.bristol_model);
    ModelSceneSymbol plane3DSymbol = new ModelSceneSymbol(pathToModel, 1.0);
    plane3DSymbol.loadAsync();
    mPlane3D = new Graphic(new Point(0, 0, 0, SpatialReferences.getWgs84()), plane3DSymbol);
    mSceneOverlay.getGraphics().add(mPlane3D);
    return plane3DSymbol;
  }

  /**
   * Copy the given file from the app's assets folder to the app's cache directory.
   *
   * @param fileName as String
   */
  private void copyFileFromAssetsToCache(String fileName) {
    AssetManager assetManager = getApplicationContext().getAssets();
    File file = new File(getCacheDir() + File.separator + fileName);
    if (!file.exists()) {
      try {
        InputStream in = assetManager.open(fileName);
        OutputStream out = new FileOutputStream(getCacheDir() + File.separator + fileName);
        byte[] buffer = new byte[1024];
        int read = in.read(buffer);
        while (read != -1) {
          out.write(buffer, 0, read);
          read = in.read(buffer);
        }
        Log.i(TAG, fileName + " copied to cache.");
      } catch (Exception e) {
        Log.e(TAG, "Error writing " + fileName + " to cache. " + e.getMessage());
      }
    } else {
      Log.i(TAG, fileName + " already in cache.");
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSceneView.resume();
  }

  @Override
  protected void onPause() {
    mSceneView.pause();
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    mSceneView.dispose();
    super.onDestroy();
  }
}
