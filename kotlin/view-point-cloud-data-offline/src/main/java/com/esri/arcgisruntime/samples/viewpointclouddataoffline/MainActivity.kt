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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.layers.PointCloudLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.Camera
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val logTag = MainActivity::class.java.simpleName
  private val permissionsRequestCode = 1
  private val _permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    sceneView.also { sceneView ->
      // Create a camera and initial camera position
      sceneView.setViewpointCamera(
        Camera(
          32.7321157,
          -117.150072,
          452.282774,
          25.481533,
          78.0945859,
          0.0
        )
      )

      // Create a scene and add it to the scene view
      with(ArcGISScene(Basemap.createImagery())) {
        sceneView.scene = this

        // Set the base surface with world elevation
        Surface().apply {
          elevationSources.add(ArcGISTiledElevationSource(getString(R.string.elevation_source_url)))
        }.let { surface ->
          // Set the base surface of the scene
          this.baseSurface = surface
        }
      }
    }

    requestReadPermission()
  }

  /**
   * Request read external storage for API level 23+.
   */
  private fun requestReadPermission() {
    if (ContextCompat.checkSelfPermission(
        this,
        _permissions[0]
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      createPointCloudLayer()
    } else {
      // Request permission
      ActivityCompat.requestPermissions(this, _permissions, permissionsRequestCode)
    }
  }

  /**
   * Handle the permissions request response
   */
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      createPointCloudLayer()
    } else {
      // Report to user that permission was denied
      getString(R.string.read_permission_denied_message).let { error ->
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        Log.e(logTag, error)
      }
    }
  }

  private fun createPointCloudLayer() {
    // Add a PointCloudLayer to the scene by passing the URI of the scene layer package to the constructor
    val pointCloudLayer = PointCloudLayer(
      Environment.getExternalStorageDirectory()
        .toString() + getString(R.string.scene_layer_package_location)
    )

    // Add a listener to perform operations when the load status of the PointCloudLayer changes
    pointCloudLayer.addLoadStatusChangedListener { loadStatusChangedEvent ->
      // When PointCloudLayer loads
      if (loadStatusChangedEvent.newLoadStatus == LoadStatus.LOADED) {
        // Add the PointCloudLayer to the operational layers of the scene
        sceneView.scene.operationalLayers.add(pointCloudLayer)
      } else if (loadStatusChangedEvent.newLoadStatus == LoadStatus.FAILED_TO_LOAD) {
        // Notify user that the PointCloudLayer has failed to load
        getString(R.string.point_cloud_layer_load_failure_message).let { error ->
          Toast.makeText(this, error, Toast.LENGTH_LONG).show()
          Log.e(logTag, error)
        }
      }
    }

    // Load the PointCloudLayer asynchronously
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
