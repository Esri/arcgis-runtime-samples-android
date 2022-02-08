/* Copyright 2019 Esri
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

package com.esri.arcgisruntime.sample.scenesymbols;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.SceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSceneSymbol;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get a reference to the scene view
    mSceneView = findViewById(R.id.sceneView);
    // create a scene and add it to the scene view
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);
    mSceneView.setScene(scene);

    // add base surface for elevation data
    final Surface surface = new Surface();
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getString(R.string.elevation_image_service_url));
    surface.getElevationSources().add(elevationSource);
    scene.setBaseSurface(surface);

    // add a camera and initial camera position
    Camera camera = new Camera(28.9, 45, 12000, 0, 45, 0);
    mSceneView.setViewpointCamera(camera);

    // add graphics overlay(s)
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    graphicsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.ABSOLUTE);
    mSceneView.getGraphicsOverlays().add(graphicsOverlay);

    int[] colors = { Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.WHITE };
    SimpleMarkerSceneSymbol.Style[] symbolStyles = SimpleMarkerSceneSymbol.Style.values();

    // for each symbol style (cube, cone, cylinder, diamond, sphere, tetrahedron)
    for (int i = 0; i < symbolStyles.length; i++) {
      SimpleMarkerSceneSymbol simpleMarkerSceneSymbol = new SimpleMarkerSceneSymbol(symbolStyles[i], colors[i], 200,
          200, 200, SceneSymbol.AnchorPosition.CENTER);
      Graphic graphic = new Graphic(new Point(44.975 + .01 * i, 29, 500, SpatialReferences.getWgs84()),
          simpleMarkerSceneSymbol);
      graphicsOverlay.getGraphics().add(graphic);
    }
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
