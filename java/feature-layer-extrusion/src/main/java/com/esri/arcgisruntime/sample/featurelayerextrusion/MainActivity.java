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
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Switch;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get us census data as a service feature table
    ServiceFeatureTable statesServiceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.us_census_feature_service));

    // add the service feature table to a feature layer
    final FeatureLayer statesFeatureLayer = new FeatureLayer(statesServiceFeatureTable);
    // set the feature layer to render dynamically to allow extrusion
    statesFeatureLayer.setRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);

    // create a scene and add it to the scene view
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);
    mSceneView = findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    // add the feature layer to the scene
    scene.getOperationalLayers().add(statesFeatureLayer);

    // define line and fill symbols for a simple renderer
    final SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 1.0f);
    final SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.BLUE, lineSymbol);
    final SimpleRenderer renderer = new SimpleRenderer(fillSymbol);
    // set renderer extrusion mode to absolute height, which extrudes the feature to the specified z-value as flat top
    renderer.getSceneProperties().setExtrusionMode(Renderer.SceneProperties.ExtrusionMode.ABSOLUTE_HEIGHT);
    // set the simple renderer to the feature layer
    statesFeatureLayer.setRenderer(renderer);

    // define a look at point for the camera at geographical center of the continental US
    final Point lookAtPoint = new Point(-10974490, 4814376, 0, SpatialReferences.getWebMercator());
    // add a camera and set it to orbit the look at point
    final Camera camera = new Camera(lookAtPoint, 20000000, 0, 55, 0);
    mSceneView.setViewpointCamera(camera);

    // set switch listener
    Switch popSwitch = findViewById(R.id.populationSwitch);
    popSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
      // set extrusion properties to either show total population or population density based on flag
      if (isChecked) {
        // multiple population density by 5000 to make data legible
        renderer.getSceneProperties().setExtrusionExpression("[POP07_SQMI] * 5000 + 100000");
      } else {
        // divide total population by 10 to make data legible
        renderer.getSceneProperties().setExtrusionExpression("[POP2007] / 10");
      }
    });

    // set initial switch state
    popSwitch.setChecked(true);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mSceneView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSceneView.resume();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mSceneView.dispose();
  }
}
