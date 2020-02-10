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

package com.esri.arcgisruntime.sample.identifylayers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)


    val featureTable = ServiceFeatureTable(getString(R.string.damage_assessment))
    val featureLayer = FeatureLayer(featureTable)

    val mapImageLayer = ArcGISMapImageLayer(getString(R.string.world_cities))

    val map = ArcGISMap(Basemap.createTopographic()).apply {
      operationalLayers.add(mapImageLayer)
      operationalLayers.add(featureLayer)
      initialViewpoint = Viewpoint(
        Point(-10977012.785807, 4514257.550369, SpatialReference.create(3857)),
        68015210.0
      )
    }


    mapView.apply {
      this.map = map
      onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, this) {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
          return super.onSingleTapConfirmed(e)
        }
      }

    }
  }
}
