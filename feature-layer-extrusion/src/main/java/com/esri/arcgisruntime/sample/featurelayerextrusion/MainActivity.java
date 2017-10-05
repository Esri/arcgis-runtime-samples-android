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

package com.esri.arcgisruntime.sample.featurelayerextrusion;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.OrbitLocationCameraController;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;

  private boolean showTotalPopulation;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // set flag for showing total or density population
    showTotalPopulation = true;

    // inflate population type toggle button
    final Button togglePopButton = (Button) findViewById(R.id.toggle_button);

    // get us census data as a service feature table
    ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.us_census_feature_service));



    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());
    //scene.setBasemap(Basemap.createImagery());
    // create SceneView from layout
    mSceneView = (SceneView) findViewById(R.id.sceneView);
    scene.getLoadSettings().setPreferredPolygonFeatureRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);
    // set the scene to the scene view
    mSceneView.setScene(scene);
    final FeatureLayer states = new FeatureLayer(serviceFeatureTable);
    //states.setFeatureRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);
    scene.getOperationalLayers().add(states);

    // define line and fill renderers
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 1.0f);
    SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.BLUE, lineSymbol);
    SimpleRenderer renderer = new SimpleRenderer(fillSymbol);
    states.setRenderer(renderer);
    states.getRenderer().getSceneProperties().setExtrusionMode(Renderer.SceneProperties.ExtrusionMode.ABSOLUTE_HEIGHT);

    // define a look at point for the camera at geographical center of the continental US
    Point lookAtPoint = new Point(-10974490, 4814376, 0, SpatialReferences.getWebMercator());
    // add a camera
    Camera camera = new Camera(lookAtPoint, 20000000, 0, 55, 0);
    // set the camera to orbit the look at point
    OrbitLocationCameraController orbitCamera = new OrbitLocationCameraController(lookAtPoint, 20000000);
    mSceneView.setCameraController(orbitCamera);
    mSceneView.setViewpointCamera(camera);

    // set button listener
    togglePopButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        // set extrusion properties to either show total population or population density based on flag
        if (showTotalPopulation) {
          states.getRenderer().getSceneProperties().setExtrusionExpression("[POP2007]");
          // change text of button to reflect showing total population
          togglePopButton.setText(R.string.total_pop);
          showTotalPopulation = false;
        } else {
          states.getRenderer().getSceneProperties().setExtrusionExpression("[POP07_SQMI]");
          // change text of button to reflect showing population density
          togglePopButton.setText(R.string.density_pop);
          showTotalPopulation = true;
        }
      }
    });
    // click to set initial state
    togglePopButton.performClick();
  }
}
