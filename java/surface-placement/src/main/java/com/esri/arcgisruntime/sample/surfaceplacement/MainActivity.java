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
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
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

    // create a scene layer from the Brest, France scene server
    ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(getString(R.string.scene_layer_service));
    scene.getOperationalLayers().add(sceneLayer);

    // set an initial viewpoint
    Point initialViewPoint = new Point(-4.45968, 48.3889, 100.0);
    Camera camera = new Camera(initialViewPoint, 329.91, 80, 0);
    mSceneView.setViewpointCamera(camera);

    // create point for the scene related graphic with a z value of 0
    Point sceneRelatedPoint = new Point(-4.4610562, 48.3902727, 0, camera.getLocation().getSpatialReference());

    // create point for the surface related graphics with z value of 70
    Point surfaceRelatedPoint = new Point(-4.4609257, 48.3903965 , 70, camera.getLocation().getSpatialReference());

    // create a red triangle symbol
    SimpleMarkerSymbol triangleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, Color.RED, 10);

    // create the draped flat overlay
    GraphicsOverlay drapedFlatOverlay = new GraphicsOverlay();
    drapedFlatOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.DRAPED_FLAT);
    drapedFlatOverlay.getGraphics().add(new Graphic(surfaceRelatedPoint, triangleSymbol));
    // create a text symbol for elevation mode
    TextSymbol drapedFlatText = new TextSymbol(15, "DRAPED FLAT", Color.BLUE, HorizontalAlignment.LEFT,
        VerticalAlignment.TOP);
    drapedFlatText.setOffsetY(20);
    drapedFlatOverlay.getGraphics().add(new Graphic(surfaceRelatedPoint, drapedFlatText));
    mSceneView.getGraphicsOverlays().add(drapedFlatOverlay);

    // create the draped billboarded overlay
    GraphicsOverlay drapedBillboardedOverlay = new GraphicsOverlay();
    drapedBillboardedOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.DRAPED_BILLBOARDED);
    drapedBillboardedOverlay.getGraphics().add(new Graphic(surfaceRelatedPoint, triangleSymbol));
    // create a text symbol for elevation mode
    TextSymbol drapedBillboardedText = new TextSymbol(15, "DRAPED BILLBOARDED", Color.BLUE, HorizontalAlignment.LEFT,
        VerticalAlignment.TOP);
    drapedBillboardedText.setOffsetY(20);
    drapedBillboardedOverlay.getGraphics().add(new Graphic(surfaceRelatedPoint, drapedBillboardedText));
    mSceneView.getGraphicsOverlays().add(drapedBillboardedOverlay);
    drapedBillboardedOverlay.setVisible(false);

    // create the relative overlay
    GraphicsOverlay relativeOverlay = new GraphicsOverlay();
    relativeOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.RELATIVE);
    relativeOverlay.getGraphics().add(new Graphic(surfaceRelatedPoint, triangleSymbol));
    // create a text symbol for elevation mode
    TextSymbol relativeText = new TextSymbol(15, "RELATIVE", Color.BLUE, HorizontalAlignment.LEFT,
        VerticalAlignment.TOP);
    relativeText.setOffsetY(20);
    relativeOverlay.getGraphics().add(new Graphic(surfaceRelatedPoint, relativeText));
    mSceneView.getGraphicsOverlays().add(relativeOverlay);

    // create the absolute overlay
    GraphicsOverlay absoluteOverlay = new GraphicsOverlay();
    absoluteOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.ABSOLUTE);
    absoluteOverlay.getGraphics().add(new Graphic(surfaceRelatedPoint, triangleSymbol));
    // create a text symbol for elevation mode
    TextSymbol absoluteText = new TextSymbol(15, "ABSOLUTE", Color.BLUE, HorizontalAlignment.LEFT,
        VerticalAlignment.TOP);
    absoluteText.setOffsetY(20);
    absoluteOverlay.getGraphics().add(new Graphic(surfaceRelatedPoint, absoluteText));
    mSceneView.getGraphicsOverlays().add(absoluteOverlay);

    // create the relative to scene overlay
    GraphicsOverlay relativeToSceneOverlay = new GraphicsOverlay();
    relativeToSceneOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.RELATIVE_TO_SCENE);
    relativeToSceneOverlay.getGraphics().add(new Graphic(sceneRelatedPoint, triangleSymbol));
    // create a text symbol for elevation mode
    TextSymbol relativeToSceneText = new TextSymbol(15, "RELATIVE TO SCENE", Color.BLUE, HorizontalAlignment.RIGHT, VerticalAlignment.TOP);
    relativeToSceneText.setOffsetY(20);
    relativeToSceneOverlay.getGraphics().add(new Graphic(sceneRelatedPoint, relativeToSceneText));
    mSceneView.getGraphicsOverlays().add(relativeToSceneOverlay);

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
