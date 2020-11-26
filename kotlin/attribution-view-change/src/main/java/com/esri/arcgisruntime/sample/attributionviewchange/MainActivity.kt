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
package com.esri.arcgisruntime.sample.attributionviewchange

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // authentication with an API key or named user is required to access basemaps and other 
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create a map with the BasemapType topographic
    val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC).apply {
      initialViewpoint = Viewpoint(47.495052, -121.786863, 100000.0)
    }
    // set the map to be displayed in this view
    mapView.map = map

    // create a FAB to respond to attribution bar
    fab.setOnClickListener {
      Toast.makeText(this@MainActivity, "Tap the attribution bar to expand it.", Toast.LENGTH_LONG).show()
    }

    // set attribution bar listener
    mapView.addAttributionViewLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
      val heightDelta = oldBottom - bottom
      fab.y += heightDelta
      Toast.makeText(
        this@MainActivity,
        "new bounds [$left,$top,$right,$bottom] old bounds [$oldLeft,$oldTop,$oldRight,$oldBottom]",
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  override fun onPause() {
    super.onPause()
    mapView.pause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.dispose()
  }
}
