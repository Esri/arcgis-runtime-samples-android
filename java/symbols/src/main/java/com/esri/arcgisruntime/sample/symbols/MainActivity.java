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

package com.esri.arcgisruntime.sample.symbols;

import java.util.ArrayList;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.SceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSceneSymbol;

public class MainActivity extends AppCompatActivity {
  GraphicsOverlay graphicsOverlay;
  double x = 44.975;
  double y = 29;
  double z = 500;
  private SceneView mSceneView;
  private Graphic scenegraphics;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the scene view
    mSceneView = findViewById(R.id.sceneView);
    // create a scene and add it to the scene view
    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());
    mSceneView.setScene(scene);

    // add base surface for elevation data
    final Surface surface = new Surface();
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getString(R.string.elevation_image_service_url));
    surface.getElevationSources().add(elevationSource);
    scene.setBaseSurface(surface);

    // add a camera and initial camera position
    Camera camera = new Camera(29, 45, 12000, 0, 0, 0);
    mSceneView.setViewpointCamera(camera);
    Point cameraLocation = camera.getLocation();

    // add graphics overlay(s)
    graphicsOverlay = new GraphicsOverlay();
    graphicsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.ABSOLUTE);
    // function to dynamically create the graphics and add them to the graphics overlay
    createSymbols();
    mSceneView.getGraphicsOverlays().add(graphicsOverlay);
  }

  public void createSymbols() {

    SimpleMarkerSceneSymbol cone = new SimpleMarkerSceneSymbol(SimpleMarkerSceneSymbol.Style.CONE, 0xFFFF0000, 200, 200,
        200, SceneSymbol.AnchorPosition.CENTER);
    SimpleMarkerSceneSymbol tetrahedron = new SimpleMarkerSceneSymbol(SimpleMarkerSceneSymbol.Style.TETRAHEDRON,
        0xFF00FF00, 200, 200, 200, SceneSymbol.AnchorPosition.CENTER);
    SimpleMarkerSceneSymbol spehere = new SimpleMarkerSceneSymbol(SimpleMarkerSceneSymbol.Style.SPHERE, 0xFF0000FF, 200,
        200, 200, SceneSymbol.AnchorPosition.CENTER);
    SimpleMarkerSceneSymbol cylinder = new SimpleMarkerSceneSymbol(SimpleMarkerSceneSymbol.Style.CYLINDER, 0xFFFF00FF,
        200, 200, 200, SceneSymbol.AnchorPosition.CENTER);
    SimpleMarkerSceneSymbol diamond = new SimpleMarkerSceneSymbol(SimpleMarkerSceneSymbol.Style.DIAMOND, 0xFF00FFFF,
        200, 200, 200, SceneSymbol.AnchorPosition.CENTER);
    SimpleMarkerSceneSymbol cube = new SimpleMarkerSceneSymbol(SimpleMarkerSceneSymbol.Style.CUBE, 0xFFFFFFFF, 200, 200,
        200, SceneSymbol.AnchorPosition.CENTER);

    ArrayList<SimpleMarkerSceneSymbol> symbols = new ArrayList<SimpleMarkerSceneSymbol>();
    symbols.add(cone);
    symbols.add(tetrahedron);
    symbols.add(spehere);
    symbols.add(cylinder);
    symbols.add(diamond);
    symbols.add(cube);

    for (int i = 0; i < symbols.size(); i++) {
      scenegraphics = new Graphic(new Point(x + 0.01 * i, y, z, SpatialReferences.getWgs84()), symbols.get(i));
      graphicsOverlay.getGraphics().add(scenegraphics);
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
