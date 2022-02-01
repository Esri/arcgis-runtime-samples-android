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

package com.esri.arcgisruntime.sample.distancecompositesymbol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.OrbitGeoElementCameraController;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.DistanceCompositeSceneSymbol;
import com.esri.arcgisruntime.symbology.ModelSceneSymbol;
import com.esri.arcgisruntime.symbology.SceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get plane model from assets
    copyFileFromAssetsToCache(getString(R.string.bristol_dae));
    copyFileFromAssetsToCache(getString(R.string.bristol_png));
    copyFileFromAssetsToCache(getString(R.string.logo_jpg));

    mSceneView = findViewById(R.id.sceneView);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);
    mSceneView.setScene(scene);

    // add base surface for elevation data
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(getString(R.string.world_elevation_service_3D)));
    scene.setBaseSurface(surface);

    // add a graphics overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    graphicsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
    mSceneView.getGraphicsOverlays().add(graphicsOverlay);

    // set up the different symbols
    SimpleMarkerSymbol circleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10);
    SimpleMarkerSceneSymbol coneSymbol = SimpleMarkerSceneSymbol.createCone(Color.RED, 3, 10);
    coneSymbol.setPitch(-90);
    coneSymbol.setAnchorPosition(SceneSymbol.AnchorPosition.CENTER);
    String modelURI = getCacheDir() + File.separator + getString(R.string.bristol_dae);
    ModelSceneSymbol modelSymbol = new ModelSceneSymbol(modelURI, 1.0);
    modelSymbol.loadAsync();

    // set up the distance composite symbol
    DistanceCompositeSceneSymbol compositeSymbol = new DistanceCompositeSceneSymbol();
    compositeSymbol.getRangeCollection().add(new DistanceCompositeSceneSymbol.Range(modelSymbol, 0, 100));
    compositeSymbol.getRangeCollection().add(new DistanceCompositeSceneSymbol.Range(coneSymbol, 100, 500));
    compositeSymbol.getRangeCollection().add(new DistanceCompositeSceneSymbol.Range(circleSymbol, 500, 0));

    // create graphic
    Point aircraftPosition = new Point(-2.708471, 56.096575, 5000, SpatialReferences.getWgs84());
    Graphic aircraftGraphic = new Graphic(aircraftPosition, compositeSymbol);
    // add graphic to graphics overlay
    graphicsOverlay.getGraphics().add(aircraftGraphic);

    // add an orbit camera controller to lock the camera to the graphic
    OrbitGeoElementCameraController cameraController = new OrbitGeoElementCameraController(aircraftGraphic, 20);
    cameraController.setCameraPitchOffset(80);
    cameraController.setCameraHeadingOffset(-30);
    mSceneView.setCameraController(cameraController);
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
}
