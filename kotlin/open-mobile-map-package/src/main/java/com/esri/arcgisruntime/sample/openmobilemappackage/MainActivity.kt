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

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.MobileMapPackage
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.openmobilemappackage.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

  private val TAG: String = MainActivity::class.java.simpleName

  private lateinit var mapPackage: MobileMapPackage

  private val activityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  private val mapView: MapView by lazy {
    activityMainBinding.mapView
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activityMainBinding.root)

    // access the mobile map package from the filesystem and load it into a MapView
    loadMobileMapPackage(getExternalFilesDir(null)?.path + getString(R.string.yellowstone_mmpk))
  }

  /**
   * Load a mobile map package into a MapView.
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
    mapPackage.addDoneLoadingListener {
      // check load status and that the mobile map package has maps
      if (mapPackage.loadStatus === LoadStatus.LOADED && mapPackage.maps.isNotEmpty()) {
        // add the map from the mobile map package to the MapView
        mapView.map = mapPackage.maps[0]
      } else {
        // log an issue if the mobile map package fails to load
        logError(mapPackage.loadError.message)
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

  /**
   * Log an error to logcat and to the screen via Toast.
   * @param message the text to log.
   */
  private fun logError(message: String?) {
    message?.let {
      Log.e(
        TAG,
        message
      )
      Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
  }
}

