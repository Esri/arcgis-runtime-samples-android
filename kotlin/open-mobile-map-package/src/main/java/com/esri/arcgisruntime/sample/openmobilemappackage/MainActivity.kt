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

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    try {
      val mobileMapFilePath = createMobileMapPackageFilePath()
      loadMobileMapPackage(mobileMapFilePath)

    } catch (exception: IllegalStateException) {
      "got error $exception".also {
        Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
        Log.e(TAG, it)
      }
    }
  }

  /**
   * Create the mobile map package file location and name structure.
   */
  private fun createMobileMapPackageFilePath(): String {

    // get the scoped storage location for this app
    val filesDir = getExternalFilesDir(null)?.path
    val fileName = getString(R.string.yellowstone_mmpk)

    // build the filename and return it
    filesDir?.let {
      val builder = StringBuilder(it)
        .append(File.pathSeparator)
        .append(fileName)
        .append(".")
        .append(mobileMapPackageFileExtension)

      return builder.toString()
    }

    throw IllegalStateException("could not open external files dir")
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
            Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
          }
          null
        }
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
