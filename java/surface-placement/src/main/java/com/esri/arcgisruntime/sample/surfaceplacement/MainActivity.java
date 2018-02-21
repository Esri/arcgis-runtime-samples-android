/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.surfaceplacement;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties.SurfacePlacement;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol.HorizontalAlignment;
import com.esri.arcgisruntime.symbology.TextSymbol.VerticalAlignment;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // create SceneView from layout
    mSceneView = (SceneView) findViewById(R.id.sceneView);
    // create a scene and add a basemap to it
    ArcGISScene agsScene = new ArcGISScene();
    agsScene.setBasemap(Basemap.createImagery());
    mSceneView.setScene(agsScene);

    // add base surface for elevation data
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getResources().getString(R.string.elevation_image_service));
    agsScene.getBaseSurface().getElevationSources().add(elevationSource);

    // add a camera and initial camera position
    Camera camera = new Camera(53.04, -4.04, 1300, 0, 90.0, 0);
    mSceneView.setViewpointCamera(camera);

    // create overlays with elevation modes
    GraphicsOverlay drapedOverlay = new GraphicsOverlay();
    drapedOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.DRAPED);
    mSceneView.getGraphicsOverlays().add(drapedOverlay);
    GraphicsOverlay relativeOverlay = new GraphicsOverlay();
    relativeOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.RELATIVE);
    mSceneView.getGraphicsOverlays().add(relativeOverlay);
    GraphicsOverlay absoluteOverlay = new GraphicsOverlay();
    absoluteOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.ABSOLUTE);
    mSceneView.getGraphicsOverlays().add(absoluteOverlay);

    // create point for graphic location
    Point point = new Point(-4.04, 53.06, 1000, camera.getLocation().getSpatialReference());

    // create a red (0xFFFF0000) circle symbol
    SimpleMarkerSymbol circleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 10);

    // create a text symbol for each elevation mode
    TextSymbol drapedText = new TextSymbol(10, "DRAPED", 0xFFFFFFFF, HorizontalAlignment.LEFT,
        VerticalAlignment.MIDDLE);
    TextSymbol relativeText = new TextSymbol(10, "RELATIVE", 0xFFFFFFFF, HorizontalAlignment.LEFT,
        VerticalAlignment.MIDDLE);
    TextSymbol absoluteText = new TextSymbol(10, "ABSOLUTE", 0xFFFFFFFF, HorizontalAlignment.LEFT,
        VerticalAlignment.MIDDLE);

    // add the point graphic and text graphic to the corresponding graphics
    // overlay
    drapedOverlay.getGraphics().add(new Graphic(point, circleSymbol));
    drapedOverlay.getGraphics().add(new Graphic(point, drapedText));

    relativeOverlay.getGraphics().add(new Graphic(point, circleSymbol));
    relativeOverlay.getGraphics().add(new Graphic(point, relativeText));

    absoluteOverlay.getGraphics().add(new Graphic(point, circleSymbol));
    absoluteOverlay.getGraphics().add(new Graphic(point, absoluteText));
  }

  @Override
  protected void onPause() {
    super.onPause();
    // pause SceneView
    mSceneView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume SceneView
    mSceneView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mSceneView.dispose();
  }
}
