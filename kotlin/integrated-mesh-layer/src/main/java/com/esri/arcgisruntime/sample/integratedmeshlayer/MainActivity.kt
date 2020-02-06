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
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.IntegratedMeshLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.Camera
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a scene to add the IntegratedMeshLayer to add subsequently add it to the SceneView
    ArcGISScene(Basemap.createImagery()).let { scene ->
      // create IntegratedMeshLayer and add to the scene's operational layers
      with(IntegratedMeshLayer(getString(R.string.mesh_layer_url))) {
        scene.operationalLayers.add(this)
      }
      sceneView.scene = scene

      // set the base surface with world elevation
      with(Surface()) {
        this.elevationSources.add(ArcGISTiledElevationSource(getString(R.string.elevation_source_url)))
        scene.baseSurface = this
      }
    }

    // create a camera and initial camera position
    with(
      Camera(
        Point(-119.622075, 37.720650, 2104.901239), 315.50368761552056, 78.09465920130114,
        0.0
      )
    ) {
      // set Viewpoint for SceneView using camera
      sceneView.setViewpointCamera(this)
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
