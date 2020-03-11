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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Camera
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val permissionsRequestCode = 1
  private val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a scene and add a basemap to it
    with(ArcGISScene()) {
      this.basemap = Basemap.createImagery()
      // add the scene to the sceneview
      sceneView.scene = this
    }

    // specify the initial camera position
    sceneView.setViewpointCamera(Camera(36.525, -121.80, 300.0, 180.0, 80.0, 0.0))

    requestReadPermission()
  }

  private fun createTiledElevationSource() {
    // add a ArcGISTiledElevationSource to the scene by passing the URI of the local tile package to the constructor
    with(
      ArcGISTiledElevationSource(
        Environment.getExternalStorageDirectory()
          .toString() + getString(R.string.local_tile_package_location)
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
   * Request read external storage for API level 23+.
   */
  private fun requestReadPermission() {
    if (ContextCompat.checkSelfPermission(
        this,
        permissions[0]
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      createTiledElevationSource()
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, permissions, permissionsRequestCode)
    }
  }

  /**
   * Handle the permissions request response
   */
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      createTiledElevationSource()
    } else {
      // report to user that permission was denied
      logErrorToUser(getString(R.string.error_read_permission_denied_message))
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
