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

package com.esri.arcgisruntime.sample.manageoperationallayers

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  private val removedLayers = mutableListOf<Layer>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with the BasemapType topographic
    val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 14)

    val imageLayerElevation = ArcGISMapImageLayer("https://sampleserver5.arcgisonline.com/arcgis/rest/services/Elevation/WorldElevations/MapServer")
    val imagelayerCensus = ArcGISMapImageLayer("https://sampleserver5.arcgisonline.com/arcgis/rest/services/Census/MapServer")

    // get the LayerList from the Map
    val operationalLayers = map.operationalLayers
    // add operational layers to the Map
    operationalLayers.add(imageLayerElevation)
    operationalLayers.add(imagelayerCensus)

    // set the initial viewpoint on the map
    map.initialViewpoint = Viewpoint(Point(-133e5, 45e5, SpatialReference.create(3857)), 2e7)

    // set the map to be displayed in this view
    mapView.apply {
      this.map = map

      // make sure the fab doesn't obscure the attribution bar
      addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
        val layoutParams = fab.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.bottomMargin += bottom - oldBottom
      }
      // close the options sheet when the map is tapped
      onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
          if (fab.isExpanded) {
            fab.isExpanded = false
          }
          return super.onTouch(view, event)
        }
      }
    }

    fab.setOnClickListener {
      fab.isExpanded = !fab.isExpanded
    }

    activeRecyclerView.apply {
      adapter = LayerListAdapter(operationalLayers)
      ItemTouchHelper(DragCallback(mapView.map.operationalLayers, removedLayers, adapter as RecyclerView.Adapter<*>)).attachToRecyclerView(this)
      layoutManager = LinearLayoutManager(this@MainActivity)
    }

    removedRecyclerView.apply {
      adapter = RemovedListAdapter(removedLayers) {position ->
        val layer = removedLayers[position]
        removedLayers.removeAt(position)
        adapter?.notifyDataSetChanged()
        mapView.map.operationalLayers.add(layer)
        activeRecyclerView.adapter?.notifyDataSetChanged()
      }
      layoutManager = LinearLayoutManager(this@MainActivity)
    }
  }
}
