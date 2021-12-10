/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.integratedmeshlayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.layers.IntegratedMeshLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.SceneView
import com.esri.arcgisruntime.sample.integratedmeshlayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private val activityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  private val sceneView: SceneView by lazy {
    activityMainBinding.sceneView
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activityMainBinding.root)

    // create an integrated mesh layer of part of the city of girona
    val gironaIntegratedMeshLayer = IntegratedMeshLayer("https://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/Girona_Spain/SceneServer")

    // create a scene and add the integrated mesh layer to it
    val gironaScene = ArcGISScene().apply {
      operationalLayers.add(gironaIntegratedMeshLayer)
    }

    // create a camera focused on a part of the integrated mesh layer
    val gironaCamera = Camera(41.9906, 2.8259, 200.0, 190.0, 65.0, 0.0)

    sceneView.apply {
      // set the scene to the scene view
      scene = gironaScene
      // set the viewpoint for the scene view using a camera
      setViewpointCamera(gironaCamera)
    }
  }

  override fun onPause() {
    sceneView.pause()
    super.onPause()
  }

  override fun onResume() {
    super.onResume()
    sceneView.resume()
  }

  override fun onDestroy() {
    sceneView.dispose()
    super.onDestroy()
  }
}
