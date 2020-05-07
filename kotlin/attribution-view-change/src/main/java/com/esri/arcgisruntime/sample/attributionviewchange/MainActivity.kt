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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with the BasemapType topographic
    val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 47.495052, -121.786863, 12)
    // set the map to be displayed in this view
    mapView.map = map

    // create a FAB to respond to attribution bar
    fab.setOnClickListener { view ->
      Snackbar.make(view, resources.getString(R.string.message), Snackbar.LENGTH_LONG)
        .setAction("Action", null).show()
    }

    // set attribution bar listener
    mapView.addAttributionViewLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
      val heightDelta = oldBottom - bottom
      fab.animate().translationYBy(heightDelta.toFloat())
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
