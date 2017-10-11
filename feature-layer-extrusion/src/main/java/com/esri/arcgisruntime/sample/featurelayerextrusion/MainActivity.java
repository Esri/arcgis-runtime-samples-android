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

import com.esri.arcgisruntime.data.QueryParameters;
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

  private boolean showTotalPopulation;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // set flag for showing total population or population density
    showTotalPopulation = true;

    // inflate population type toggle button
    final Button togglePopButton = (Button) findViewById(R.id.toggle_button);

    // get us census data as a service feature table
    ServiceFeatureTable statesServiceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.us_census_feature_service));

    // load all fields in the service feature table
    QueryParameters queryParams = new QueryParameters();
    queryParams.setWhereClause("1=1");
    statesServiceFeatureTable.queryFeaturesAsync(queryParams, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);

    // add the service feature table to a feature layer
    final FeatureLayer statesFeatureLayer = new FeatureLayer(statesServiceFeatureTable);
    // set the feature layer to render dynamically to allow extrusion
    statesFeatureLayer.setFeatureRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);

    // create a scene and add it to the scene view
    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());
    SceneView sceneView = (SceneView) findViewById(R.id.sceneView);
    sceneView.setScene(scene);

    // add the feature layer to the scene
    scene.getOperationalLayers().add(statesFeatureLayer);

    // define line and fill symbols for a simple renderer
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 1.0f);
    SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.BLUE, lineSymbol);
    final SimpleRenderer renderer = new SimpleRenderer(fillSymbol);
    // set renderer extrusion mode to base height, which includes base height of each vertex in calculating z values
    renderer.getSceneProperties().setExtrusionMode(Renderer.SceneProperties.ExtrusionMode.BASE_HEIGHT);
    // set the simple renderer to the feature layer
    statesFeatureLayer.setRenderer(renderer);

    // define a look at point for the camera at geographical center of the continental US
    Point lookAtPoint = new Point(-10974490, 4814376, 0, SpatialReferences.getWebMercator());
    // add a camera and set it to orbit the look at point
    Camera camera = new Camera(lookAtPoint, 20000000, 0, 55, 0);
    OrbitLocationCameraController orbitCamera = new OrbitLocationCameraController(lookAtPoint, 20000000);
    sceneView.setCameraController(orbitCamera);
    sceneView.setViewpointCamera(camera);

    // set button listener
    togglePopButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        // set extrusion properties to either show total population or population density based on flag
        if (showTotalPopulation) {
          // divide total population by 10 to make data legible
          renderer.getSceneProperties().setExtrusionExpression("[POP2007] / 10");
          // change text of button to total pop
          togglePopButton.setText(R.string.total_pop);
          showTotalPopulation = false;
        } else {
          // multiple population density by 5000 to make data legible
          renderer.getSceneProperties().setExtrusionExpression("[POP07_SQMI] * 5000");
          // change text of button to pop density
          togglePopButton.setText(R.string.density_pop);
          showTotalPopulation = true;
        }
      }
    });
    // click to set initial state
    togglePopButton.performClick();
  }
}
