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
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  private val removedLayers = mutableListOf<Layer>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with a topographic basemap
    val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 4)

    val imageLayerElevation =
      ArcGISMapImageLayer("https://sampleserver5.arcgisonline.com/arcgis/rest/services/Elevation/WorldElevations/MapServer")
    val imageLayerCensus =
      ArcGISMapImageLayer("https://sampleserver5.arcgisonline.com/arcgis/rest/services/Census/MapServer")
    val imageLayerDamage =
      ArcGISMapImageLayer("https://sampleserver5.arcgisonline.com/arcgis/rest/services/DamageAssessment/MapServer")

    // add the map image layers to the map's operational layers
    map.operationalLayers.addAll(listOf(imageLayerElevation, imageLayerCensus, imageLayerDamage))

    // set the map to the map view
    mapView.map = map

    // handle the floating action button and recycler view logic for this sample
    setupUI()
  }

  /**
   * Changes a layer's ordered position within the map's operational layers.
   *
   * @param oldPosition the layer's position before the move
   * @param targetPosition the layer's position after the move
   */
  private fun moveLayerFromToPosition(oldPosition: Int, targetPosition: Int) {
    val operationalLayers = mapView.map.operationalLayers
    // remove the layer from the map
    val layer = operationalLayers.removeAt(oldPosition)
    // add the layer back to the map at the target position
    operationalLayers.add(targetPosition, layer)

    // tell the recycler view that the item has moved
    activeRecyclerView.adapter?.notifyItemMoved(oldPosition, targetPosition)
  }

  /**
   * Removes a layer from the map's operational layers and stores it the removed layers list.
   *
   * @param position the index of the layer in the map's operational layers LayerList
   */
  private fun removeLayerFromMap(position: Int) {
    val operationalLayers = mapView.map.operationalLayers
    // store the layer in a list to keep track of which layers have been removed
    removedLayers.add(operationalLayers[position])
    // remove the layer from the map
    operationalLayers.removeAt(position)

    // tell the recycler view that the item has been removed
    activeRecyclerView.adapter?.notifyDataSetChanged()
  }

  /**
   * Adds a layer from the removed layer list to the map's operational layers.
   *
   * @param position the index of the layer within the list of removed layers
   */
  private fun addLayerToMap(position: Int) {
    // remove the layer from the removed layers
    val layer = removedLayers.removeAt(position)
    // add the layer to the map
    mapView.map.operationalLayers.add(layer)

    // notify the recycler views of the change
    removedRecyclerView.adapter?.notifyDataSetChanged()
    activeRecyclerView.adapter?.notifyDataSetChanged()
  }

  /**
   * Initializes behavior for the UI, including floating action button position and behavior
   * and recycler view creation and setup.
   */
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

    // open and close the layer list with the fab
    fab.setOnClickListener {
      fab.isExpanded = !fab.isExpanded
    }

    // set up the recycler view for the active layer list
    activeRecyclerView.apply {
      adapter = LayerListAdapter(mapView.map.operationalLayers)
      // create the drag and drop behavior with callbacks for moving items and removing them
      // and attach it to the recycler view
      ItemTouchHelper(
        DragCallback(
          onItemMove = { oldPosition, newPosition ->
            moveLayerFromToPosition(oldPosition, newPosition)
          },
          onItemSwiped = { position -> removeLayerFromMap(position) })
      ).attachToRecyclerView(this)
      layoutManager = LinearLayoutManager(this@MainActivity)
    }

    // set up the recycler view for the inactive layer list
    removedRecyclerView.apply {
      // create the adapter with a callback for adding the layer back to the map
      adapter = RemovedListAdapter(removedLayers) { addLayerToMap(it) }
      layoutManager = LinearLayoutManager(this@MainActivity)
    }
  }
}
