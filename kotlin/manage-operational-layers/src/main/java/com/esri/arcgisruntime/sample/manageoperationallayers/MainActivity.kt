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

    val imageLayerElevation =
      ArcGISMapImageLayer("https://sampleserver5.arcgisonline.com/arcgis/rest/services/Elevation/WorldElevations/MapServer")
    val imagelayerCensus =
      ArcGISMapImageLayer("https://sampleserver5.arcgisonline.com/arcgis/rest/services/Census/MapServer")
    val imageLayerDamage =
      ArcGISMapImageLayer("https://sampleserver5.arcgisonline.com/arcgis/rest/services/DamageAssessment/MapServer")

    map.apply {
      // add operational layers to the map
      operationalLayers.addAll(listOf(imageLayerElevation, imagelayerCensus, imageLayerDamage))
      // set the initial viewpoint on the map
      initialViewpoint = Viewpoint(Point(-133e5, 45e5, SpatialReference.create(3857)), 2e7)
    }
    // set the map to the map view
    mapView.map = map

    // handle the floating action button and recycler view logic for this sample
    setupUI()
  }

  private fun moveLayerFromToPosition(oldPosition: Int, targetPosition: Int) {
    val operationalLayers = mapView.map.operationalLayers
    // remove the layer from the map
    val layer = operationalLayers.removeAt(oldPosition)
    // add the layer back to the map at the target position
    operationalLayers.add(targetPosition, layer)

    // tell the recycler view that the item has moved
    activeRecyclerView.adapter?.notifyItemMoved(oldPosition, targetPosition)
  }

  private fun removeLayerFromMap(position: Int) {
    val operationalLayers = mapView.map.operationalLayers
    removedLayers.add(operationalLayers[position])
    operationalLayers.removeAt(position)
    activeRecyclerView.adapter?.notifyDataSetChanged()
  }

  private fun addLayerToMap(position: Int) {
    // remove the layer from the removed layers list and add it to the map
    val layer = removedLayers[position]
    removedLayers.removeAt(position)
    mapView.map.operationalLayers.add(layer)

    // notify the recycler views of the change
    removedRecyclerView.adapter?.notifyDataSetChanged()
    activeRecyclerView.adapter?.notifyDataSetChanged()
  }

  private fun setupUI() {
    mapView.apply {
      // make sure the fab doesn't obscure the attribution bar
      addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
        val layoutParams = fab.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.bottomMargin += bottom - oldBottom
      }
      // close the layer lists when the map is tapped
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
      adapter = LayerListAdapter(mapView.map.operationalLayers)
      ItemTouchHelper(
        DragCallback(
          onItemMove = { oldPosition, newPosition -> moveLayerFromToPosition(oldPosition, newPosition) },
          onItemSwiped = { position -> removeLayerFromMap(position) })
      ).attachToRecyclerView(this)
      layoutManager = LinearLayoutManager(this@MainActivity)
    }

    removedRecyclerView.apply {
      adapter = RemovedListAdapter(removedLayers) { addLayerToMap(it) }
      layoutManager = LinearLayoutManager(this@MainActivity)
    }
  }
}
