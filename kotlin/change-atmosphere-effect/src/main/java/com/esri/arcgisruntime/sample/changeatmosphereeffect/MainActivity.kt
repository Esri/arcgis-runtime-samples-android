/* Copyright 2020 Esri
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

package com.esri.arcgisruntime.sample.changeatmosphereeffect

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.AtmosphereEffect
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.SceneView
import com.esri.arcgisruntime.sample.changeatmosphereeffect.databinding.ActivityMainBinding

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

    // create a scene and add a base map to it
    val scene = ArcGISScene(Basemap.createImagery())
    sceneView.scene = scene

    // add base surface for elevation data
    sceneView.scene.baseSurface = Surface().apply {
      elevationSources.add(
        ArcGISTiledElevationSource(getString(R.string.elevation_image_service))
      )
    }

    // create a camera and set it as the viewpoint for when the scene loads
    val camera = Camera(64.416919, -14.483728, 100.0, 318.0, 105.0, 0.0)
    sceneView.setViewpointCamera(camera)
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.change_atmosphere_effect_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // set different atmosphere effects on the scene view
    sceneView.atmosphereEffect = when (item.itemId) {
      R.id.action_no_atmosphere_effect -> AtmosphereEffect.NONE
      R.id.action_realistic_atmosphere_effect -> AtmosphereEffect.REALISTIC
      R.id.action_horizon_atmosphere_effect -> AtmosphereEffect.HORIZON_ONLY
      else -> return super.onOptionsItemSelected(item)
    }
    return true
  }

  override fun onResume() {
    super.onResume()
    sceneView.resume()
  }

  override fun onPause() {
    sceneView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    sceneView.dispose()
    super.onDestroy()
  }
}
