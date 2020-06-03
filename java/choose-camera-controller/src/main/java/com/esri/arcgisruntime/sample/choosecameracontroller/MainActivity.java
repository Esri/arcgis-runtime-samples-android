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

package com.esri.arcgisruntime.sample.choosecameracontroller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.content.res.AssetManager;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.GlobeCameraController;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.OrbitGeoElementCameraController;
import com.esri.arcgisruntime.mapping.view.OrbitLocationCameraController;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.ModelSceneSymbol;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;
  private Graphic mPlane3D;
  private GraphicsOverlay mSceneOverlay;
  private OrbitGeoElementCameraController mOrbitPlaneCameraController;
  private OrbitLocationCameraController mOrbitLocationCameraController;
  private ModelSceneSymbol mModelSceneSymbol;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // load plane model from assets into cache directory
    copyFileFromAssetsToCache(getString(R.string.bristol_model));
    copyFileFromAssetsToCache(getString(R.string.bristol_skin));

    setupToolbar();

    // create a scene and add it to the scene view
    mSceneView = findViewById(R.id.sceneView);
    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());
    mSceneView.setScene(scene);

    // add base surface for elevation data
    Surface surface = new Surface();
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getString(R.string.world_elevation_service_url));
    surface.getElevationSources().add(elevationSource);
    scene.setBaseSurface(surface);

    // create a graphics overlay for the scene
    mSceneOverlay = new GraphicsOverlay();
    mSceneOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.ABSOLUTE);
    mSceneView.getGraphicsOverlays().add(mSceneOverlay);

    // create a camera and set it as the viewpoint for when the scene loads
    Camera camera = new Camera(38.459291, -109.937576, 5500, 150.0, 20.0, 0.0);
    mSceneView.setViewpointCamera(camera);

    // instantiate a new camera controller which orbits a target location
    Point locationPoint = new Point(-109.929589, 38.437304, 1700, SpatialReferences.getWgs84());
    mOrbitLocationCameraController = new OrbitLocationCameraController(locationPoint, 5000);
    mOrbitLocationCameraController.setCameraPitchOffset(3);
    mOrbitLocationCameraController.setCameraHeadingOffset(150);

    mModelSceneSymbol = loadModel();
    mModelSceneSymbol.addDoneLoadingListener(() -> {
      // instantiate a new camera controller which orbits the plane at a set distance
      mOrbitPlaneCameraController = new OrbitGeoElementCameraController(mPlane3D, 100.0);
      mOrbitPlaneCameraController.setCameraPitchOffset(30);
      mOrbitPlaneCameraController.setCameraHeadingOffset(150);
    });
  }

  private void setupToolbar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_photo_camera));
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.camera_controller_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int itemid = item.getItemId();
    if (itemid == R.id.action_camera_controller_plane) {
      mSceneView.setCameraController(mOrbitPlaneCameraController);
      return true;
    } else if (itemid == R.id.action_camera_controller_crater) {
      mSceneView.setCameraController(mOrbitLocationCameraController);
      return true;
    } else if (itemid == R.id.action_camera_controller_globe) {
      mSceneView.setCameraController(new GlobeCameraController());
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Load the plane model from the cache, use to construct a Model Scene Symbol and add it to the scene's graphic overlay.
   */
  private ModelSceneSymbol loadModel() {
    // create a graphic with a ModelSceneSymbol of a plane to add to the scene
    String pathToModel = getCacheDir() + File.separator + getString(R.string.bristol_model);
    ModelSceneSymbol plane3DSymbol = new ModelSceneSymbol(pathToModel, 1.0);
    plane3DSymbol.loadAsync();
    plane3DSymbol.setHeading(45);
    mPlane3D = new Graphic(new Point(-109.937516, 38.456714, 5000, SpatialReferences.getWgs84()), plane3DSymbol);
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
        BufferedInputStream bis = new BufferedInputStream(assetManager.open(fileName));
        BufferedOutputStream bos = new BufferedOutputStream(
            new FileOutputStream(getCacheDir() + File.separator + fileName));
        byte[] buffer = new byte[bis.available()];
        int read = bis.read(buffer);
        while (read != -1) {
          bos.write(buffer, 0, read);
          read = bis.read(buffer);
        }
        bos.close();
        bis.close();
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
