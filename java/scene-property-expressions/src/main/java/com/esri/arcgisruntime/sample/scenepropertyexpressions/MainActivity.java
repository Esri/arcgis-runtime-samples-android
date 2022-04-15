/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.scenepropertyexpressions;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.SeekBar;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.SceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;

  private static final int PITCH_OFFSET = 90;
  private static final String HEADING_EXPRESSION = "HEADING";
  private static final String PITCH_EXPRESSION = "PITCH";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the scene view
    mSceneView = findViewById(R.id.sceneView);

    // create a scene with an imagery basemap
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);

    // add the SceneView to the stack pane
    mSceneView.setScene(scene);

    // add a camera and initial camera position
    Point point = new Point(83.9, 28.4, 1000, SpatialReferences.getWgs84());
    Camera camera = new Camera(point, 1000, 0, 50, 0);
    mSceneView.setViewpointCamera(camera);

    // create a graphics overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    graphicsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
    mSceneView.getGraphicsOverlays().add(graphicsOverlay);

    // add renderer using rotation expressions
    SimpleRenderer renderer = new SimpleRenderer();
    renderer.getSceneProperties().setHeadingExpression('[' + HEADING_EXPRESSION + ']');
    renderer.getSceneProperties().setPitchExpression('[' + PITCH_EXPRESSION + ']');
    graphicsOverlay.setRenderer(renderer);

    // create a red cone graphic
    // in this sample we've set the anchor position to center. By default, the anchor position is BOTTOM
    SimpleMarkerSceneSymbol coneSymbol = SimpleMarkerSceneSymbol.createCone(Color.RED, 100, 100, SceneSymbol.AnchorPosition.CENTER);
    coneSymbol.setPitch(-PITCH_OFFSET);  // correct symbol's default pitch
    Graphic cone = new Graphic(new Point(83.9, 28.41, 200, SpatialReferences.getWgs84()), coneSymbol);
    graphicsOverlay.getGraphics().add(cone);

    // bind attribute based on values in seek bars
    SeekBar headingSeekBar = findViewById(R.id.headingSeekBar);
    cone.getAttributes().put(HEADING_EXPRESSION, headingSeekBar.getProgress());
    headingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        cone.getAttributes().put(HEADING_EXPRESSION, progress);
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
    SeekBar pitchSeekBar = findViewById(R.id.pitchSeekBar);
    cone.getAttributes().put(PITCH_EXPRESSION, pitchSeekBar.getProgress() - PITCH_OFFSET);
    pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        cone.getAttributes().put(PITCH_EXPRESSION, progress - PITCH_OFFSET);
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {

      }
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
