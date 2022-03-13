/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.samples.createterrainfromalocaltilepackage

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.SceneView
import com.esri.arcgisruntime.samples.createterrainfromalocaltilepackage.databinding.ActivityMainBinding

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

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create and add a scene with an imagery basemap
    sceneView.scene = ArcGISScene(BasemapStyle.ARCGIS_IMAGERY)

    // specify the initial camera position
    sceneView.setViewpointCamera(Camera(36.525, -121.80, 300.0, 180.0, 80.0, 0.0))

    // add a ArcGISTiledElevationSource to the scene by passing the URI of the local tile package to the constructor
    with(
      ArcGISTiledElevationSource(getExternalFilesDir(null)?.path + getString(R.string.local_tile_package_location)
      )
    ) {
      // add a listener to perform operations when the load status of the ArcGISTiledElevationSource changes
      this.addLoadStatusChangedListener { loadStatusChangedEvent ->
        // when ArcGISTiledElevationSource loads
        if (loadStatusChangedEvent.newLoadStatus == LoadStatus.LOADED) {
          // add the ArcGISTiledElevationSource to the elevation sources of the scene
          sceneView.scene.baseSurface.elevationSources.add(this)
        } else if (loadStatusChangedEvent.newLoadStatus == LoadStatus.FAILED_TO_LOAD) {
          // notify user that the ArcGISTiledElevationSource has failed to load
          logErrorToUser(getString(R.string.error_tiled_elevation_source_load_failure_message))
        }
      }

      // load the ArcGISTiledElevationSource asynchronously
      this.loadAsync()
    }
  }

  /**
   * AppCompatActivity Extensions
   **/
  private val AppCompatActivity.logTag get() = this::class.java.simpleName

  private fun AppCompatActivity.logErrorToUser(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    Log.e(logTag, message)
  }
}
