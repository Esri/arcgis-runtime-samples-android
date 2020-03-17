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

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.OrbitLocationCameraController
import com.esri.arcgisruntime.symbology.Renderer
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  // set flag for showing total population or population density
  private var showTotalPopulation = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // get us census data as a service feature table
    val statesServiceFeatureTable =
      ServiceFeatureTable(resources.getString(R.string.us_census_feature_service))

    // add the service feature table to a feature layer
    val statesFeatureLayer = FeatureLayer(statesServiceFeatureTable)
    // set the feature layer to render dynamically to allow extrusion
    statesFeatureLayer.renderingMode = FeatureLayer.RenderingMode.DYNAMIC

    // create a scene and add it to the scene view
    val scene = ArcGISScene(Basemap.createImagery())
    sceneView.scene = scene

    // add the feature layer to the scene
    scene.operationalLayers.add(statesFeatureLayer)

    // define line and fill symbols for a simple renderer
    val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 1.0f)
    val fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.BLUE, lineSymbol)
    val renderer = SimpleRenderer(fillSymbol)
    // set renderer extrusion mode to base height, which includes base height of each vertex in calculating z values
    renderer.sceneProperties.extrusionMode = Renderer.SceneProperties.ExtrusionMode.BASE_HEIGHT

    // set the simple renderer to the feature layer
    statesFeatureLayer.renderer = renderer

    // define a look at point for the camera at geographical center of the continental US
    val lookAtPoint = Point(-10974490.0, 4814376.0, 0.0, SpatialReferences.getWebMercator())
    // add a camera and set it to orbit the look at point
    val camera = Camera(lookAtPoint, 20000000.0, 0.0, 55.0, 0.0)
    val orbitCamera = OrbitLocationCameraController(lookAtPoint, 20000000.0)
    sceneView.cameraController = orbitCamera
    sceneView.setViewpointCamera(camera)

    // set button listener
    toggle_button.setOnClickListener {
      if (showTotalPopulation) {
        // divide total population by 10 to make data legible
        renderer.sceneProperties.extrusionExpression = "[POP2007] / 10"
        // change text of button to total pop
        toggle_button.text = resources.getString(R.string.total_pop)
        showTotalPopulation = false
      } else {
        // multiple population density by 5000 to make data legible
        renderer.sceneProperties.extrusionExpression = "[POP07_SQMI] * 5000"
        // change text of button to pop density
        toggle_button.text = resources.getString(R.string.density_pop)
        showTotalPopulation = true
      }
    }
    // click to set initial state
    toggle_button.performClick()
  }

  override fun onPause() {
    super.onPause()
    sceneView.pause()
  }

  override fun onResume() {
    super.onResume()
    sceneView.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    sceneView.dispose()
  }
}
