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

package com.esri.arcgisruntime.sample.openmobilemappackage

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.IllegalStateException
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.MobileMapPackage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

  companion object {
    private val TAG: String = MainActivity::class.java.simpleName
  }

  private val mobileMapPackageFileExtension = "mmpk"
  private lateinit var mapPackage: MobileMapPackage
  val permsList = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    requestReadPermissionAndLoadPackage()

  }

  private fun requestReadPermissionAndLoadPackage() {
    val mobileMapFilePath = createMobileMapPackageFilePath()

    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
      == PackageManager.PERMISSION_GRANTED
    ) {
      // load the offline mobile map package
      loadMobileMapPackage(mobileMapFilePath);
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, permsList.toTypedArray(), 2);
    }
  }

  /**
   * Create the mobile map package file location and name structure.
   */
  private fun createMobileMapPackageFilePath(): String {

    val extStoreDir = Environment.getExternalStorageDirectory()
    val sdCarDir = getString(R.string.config_data_sdcard_offline_dir)
    val fileName = getString(R.string.yellowstone_mmpk)

    extStoreDir.absolutePath.also {
      val builder = StringBuilder(it)
        .append(File.separator)
        .append(sdCarDir)
        .append(File.separator)
        .append(fileName)
        .append(".")
        .append(mobileMapPackageFileExtension)

      return builder.toString()
    }
  }

  /**
   * Load a mobile map package into a MapView
   *
   * @param mmpkFile Full path to mmpk file
   */
  private fun loadMobileMapPackage(mmpkFile: String) {
    // create the mobile map package
    mapPackage = MobileMapPackage(mmpkFile).also {
      // load the mobile map package asynchronously
      it.loadAsync()
    }

    // add done listener which will invoke when mobile map package has loaded
    mapPackage.addDoneLoadingListener() {
      // check load status and that the mobile map package has maps
      mapView.map =
        if (mapPackage.getLoadStatus() === LoadStatus.LOADED && mapPackage.maps.isNotEmpty()) {
          // add the map from the mobile map package to the MapView
          mapPackage.maps[0]
        } else {
          // log an issue if the mobile map package fails to load
          mapPackage.loadError.message?.let {
            Log.e(TAG, it)
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
          }
          null
        }
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      // load the offline mobile map package
      loadMobileMapPackage(createMobileMapPackageFilePath());
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, permsList.toTypedArray(), 2);
    }
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
