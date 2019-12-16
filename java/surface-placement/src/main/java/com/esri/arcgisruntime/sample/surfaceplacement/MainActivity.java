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

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;

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

    // create scene view from layout
    mSceneView = findViewById(R.id.sceneView);
    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());
    mSceneView.setScene(scene);

    // add base surface for elevation data
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getString(R.string.elevation_image_service));
    scene.getBaseSurface().getElevationSources().add(elevationSource);

    // add a camera and initial camera position
    Camera camera = new Camera(53.05, -4.01, 1115, 299, 88, 0);
    mSceneView.setViewpointCamera(camera);

    // create point for graphic location
    Point point = new Point(-4.04, 53.06, 1000, camera.getLocation().getSpatialReference());

    // create a red triangle symbol
    SimpleMarkerSymbol triangleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, Color.RED, 10);

    // create the draped flat overlay
    GraphicsOverlay drapedFlatOverlay = new GraphicsOverlay();
    drapedFlatOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.DRAPED_FLAT);
    drapedFlatOverlay.getGraphics().add(new Graphic(point, triangleSymbol));
    // create a text symbol for elevation mode
    TextSymbol drapedFlatText = new TextSymbol(10, "DRAPED FLAT", Color.WHITE, HorizontalAlignment.LEFT,
        VerticalAlignment.MIDDLE);
    drapedFlatText.setOffsetY(20);
    drapedFlatOverlay.getGraphics().add(new Graphic(point, drapedFlatText));
    mSceneView.getGraphicsOverlays().add(drapedFlatOverlay);

    // create the draped billboarded overlay
    GraphicsOverlay drapedBillboardedOverlay = new GraphicsOverlay();
    drapedBillboardedOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.DRAPED_BILLBOARDED);
    drapedBillboardedOverlay.getGraphics().add(new Graphic(point, triangleSymbol));
    // create a text symbol for elevation mode
    TextSymbol drapedBillboardedText = new TextSymbol(10, "DRAPED BILLBOARDED", Color.WHITE, HorizontalAlignment.LEFT,
        VerticalAlignment.MIDDLE);
    drapedBillboardedText.setOffsetY(20);
    drapedBillboardedOverlay.getGraphics().add(new Graphic(point, drapedBillboardedText));
    mSceneView.getGraphicsOverlays().add(drapedBillboardedOverlay);
    drapedBillboardedOverlay.setVisible(false);

    // create the relative overlay
    GraphicsOverlay relativeOverlay = new GraphicsOverlay();
    relativeOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.RELATIVE);
    relativeOverlay.getGraphics().add(new Graphic(point, triangleSymbol));
    // create a text symbol for elevation mode
    TextSymbol relativeText = new TextSymbol(10, "RELATIVE", Color.WHITE, HorizontalAlignment.LEFT,
        VerticalAlignment.MIDDLE);
    relativeText.setOffsetY(20);
    relativeOverlay.getGraphics().add(new Graphic(point, relativeText));
    mSceneView.getGraphicsOverlays().add(relativeOverlay);

    // create the absolute overlay
    GraphicsOverlay absoluteOverlay = new GraphicsOverlay();
    absoluteOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.ABSOLUTE);
    absoluteOverlay.getGraphics().add(new Graphic(point, triangleSymbol));
    // create a text symbol for elevation mode
    TextSymbol absoluteText = new TextSymbol(10, "ABSOLUTE", Color.WHITE, HorizontalAlignment.LEFT,
        VerticalAlignment.MIDDLE);
    absoluteText.setOffsetY(20);
    absoluteOverlay.getGraphics().add(new Graphic(point, absoluteText));
    mSceneView.getGraphicsOverlays().add(absoluteOverlay);

    // toggle visibility of the draped and billboarded graphics overlays
    ToggleButton drapedToggle = findViewById(R.id.drapedToggle);
    drapedToggle.setOnClickListener(v -> {
      drapedBillboardedOverlay.setVisible(drapedToggle.isChecked());
      drapedFlatOverlay.setVisible(!drapedToggle.isChecked());
    });
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
