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
 *
 */

package com.esri.arcgisruntime.samples.viewpointclouddataoffline

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.layers.PointCloudLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.Camera
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a surface with an elevation source
    val surface = Surface().apply {
      elevationSources.add(ArcGISTiledElevationSource(getString(R.string.elevation_source_url)))
    }

    // create a scene with the surface
    val sceneWithSurface = ArcGISScene(Basemap.createImagery()).apply {
      baseSurface = surface
    }

    // create the scene view with an initial viewpoint and scene
    sceneView.apply {
      // create a camera and initial camera position
      setViewpointCamera(Camera(32.7321157, -117.150072, 452.282774, 25.481533, 78.0945859, 0.0))
      // add the scene with the surface
      scene = sceneWithSurface
    }

    // add a PointCloudLayer to the scene by passing the URI of the scene layer package to the constructor
    val pointCloudLayer =
      PointCloudLayer(getExternalFilesDir(null)?.path + getString(R.string.scene_layer_package_location))

    // add a listener to perform operations when the load status of the PointCloudLayer changes
    pointCloudLayer.addDoneLoadingListener {
      // when PointCloudLayer loads
      if (pointCloudLayer.loadStatus == LoadStatus.LOADED) {
        // add the PointCloudLayer to the operational layers of the scene
        sceneView.scene.operationalLayers.add(pointCloudLayer)
      } else {
        val error = "Error loading point cloud layer: " + pointCloudLayer.loadError
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
      }
    }

    // load the PointCloudLayer asynchronously
    pointCloudLayer.loadAsync()
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
