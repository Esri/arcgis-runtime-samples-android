/*
 * Copyright 2020 Esri
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

package com.esri.arcgisruntime.sample.scenelayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.layers.ArcGISSceneLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Camera
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a scene layer from a url
    val sceneLayer = ArcGISSceneLayer(getString(R.string.brest_buildings))

    // create a camera with initial camera position
    val camera = Camera(48.378, -4.494, 200.0, 345.0, 65.0, 0.0)

    // create a scene
    val brestBuildingScene = ArcGISScene().apply {
      // add a base map
      basemap = Basemap.createTopographic()
      // add a scene service to the scene for viewing buildings
      operationalLayers.add(sceneLayer)
    }

    // create a scene view
    sceneView.apply {
      // add the scene to the scene view
      scene = brestBuildingScene
      // set initial camera position
      setViewpointCamera(camera)
    }
  }

  override fun onPause() {
    super.onPause()
    sceneView.pause()
  }

  override fun onResume() {
    sceneView.resume()
    super.onResume()
  }

  override fun onDestroy() {
    super.onDestroy()
    sceneView.dispose()
  }
}

