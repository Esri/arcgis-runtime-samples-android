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

import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
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

    //TODO Remove
    final GraphicsOverlay extrusionOverlay = new GraphicsOverlay(GraphicsOverlay.RenderingMode.DYNAMIC);

    FeatureLayer states = new FeatureLayer(serviceFeatureTable);





    // set a query parameter which will get all features
    QueryParameters queryParameters = new QueryParameters();
    queryParameters.setWhereClause("1=1");
    // execute the query being sure to load all fields
    final ListenableFuture<FeatureQueryResult> featureQueryResultFuture = serviceFeatureTable
        .queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
    featureQueryResultFuture.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          // get the feature query result
          FeatureQueryResult featureQueryResult = featureQueryResultFuture.get();
          for (Feature state : featureQueryResult) {
            //TODO Remove
            Graphic stateGraphic = new Graphic(state.getGeometry());
            int totalPopulation = Integer.parseInt(state.getAttributes().get("POP2007").toString());
            int densityPopulation = (int) Math
                .round(Double.parseDouble(state.getAttributes().get("POP07_SQMI").toString()));
            Log.d("density", String.valueOf(densityPopulation));
            // scale the total population down by a factor of 10 and set it as an attribute
            stateGraphic.getAttributes().put("TOTAL_POP", totalPopulation / 10);
            // scale the population density up by a factor of 1000 and set it as an attribute
            stateGraphic.getAttributes().put("DENSITY_POP", densityPopulation * 10000);
            extrusionOverlay.getGraphics().add(stateGraphic);
          }
        } catch (InterruptedException | ExecutionException e) {
          Log.e(TAG, "Error getting FeatureQueryResult: " + e.getMessage());
        }
      }
    });

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());
    // create SceneView from layout
    mSceneView = (SceneView) findViewById(R.id.sceneView);
    // set the scene to the scene view
    mSceneView.setScene(scene);
    mSceneView.getGraphicsOverlays().add(extrusionOverlay);

    // define a look at point for the camera at geographical center of the continental US
    Point lookAtPoint = new Point(-10974490, 4814376, 0, SpatialReferences.getWebMercator());
    // add a camera
    Camera camera = new Camera(lookAtPoint, 20000000, 0, 55, 0);
    // set the camera to orbit the look at point
    OrbitLocationCameraController orbitCamera = new OrbitLocationCameraController(lookAtPoint, 20000000);
    mSceneView.setCameraController(orbitCamera);
    mSceneView.setViewpointCamera(camera);

    // define line and fill renderers
    final SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 1.0f);
    final SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.BLUE, lineSymbol);

    // set button listener
    togglePopButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        // define a new renderer
        SimpleRenderer renderer = new SimpleRenderer(fillSymbol);
        Renderer.SceneProperties renderProperties = renderer.getSceneProperties();
        renderProperties.setExtrusionMode(Renderer.SceneProperties.ExtrusionMode.ABSOLUTE_HEIGHT);
        // set extrusion properties to either show total population or population density based on flag
        if (showTotalPopulation) {
          renderProperties.setExtrusionExpression("[POP2007]");
          // change text of button to reflect showing total population
          togglePopButton.setText(R.string.total_pop);
          showTotalPopulation = false;
        } else {
          renderProperties.setExtrusionExpression("[POP07_SQMI]");
          // change text of button to reflect showing population density
          togglePopButton.setText(R.string.density_pop);
          showTotalPopulation = true;
        }
        //TODO Remove
        extrusionOverlay.setRenderer(renderer);
      }
    });
    // click to set initial state
    togglePopButton.performClick();
  }


}
