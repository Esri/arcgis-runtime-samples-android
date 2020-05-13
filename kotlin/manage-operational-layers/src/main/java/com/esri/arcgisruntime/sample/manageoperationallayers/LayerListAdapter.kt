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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.LayerList

class LayerListAdapter(private val dataSet: LayerList) :
  RecyclerView.Adapter<LayerListAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val v = LayoutInflater.from(parent.context).inflate(R.layout.layer_item, parent, false)
    return ViewHolder(v)
  }

  override fun getItemCount(): Int = dataSet.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.textView.text = dataSet[position].name
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.layerName)
  }
}

class RemovedListAdapter(
  private val dataSet: MutableList<Layer>,
  private val onItemClick: (pos: Int) -> Unit
) : RecyclerView.Adapter<RemovedListAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemovedListAdapter.ViewHolder {
    val v = LayoutInflater.from(parent.context).inflate(R.layout.removed_layer_item, parent, false)
    return ViewHolder(v)
  }

  override fun getItemCount() = dataSet.size

  override fun onBindViewHolder(holder: RemovedListAdapter.ViewHolder, position: Int) {
    holder.textView.text = dataSet[position].name
    holder.onClick = onItemClick
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.deletedLayerName)
    var onClick: ((Int) -> Unit)? = null

    init {
      itemView.setOnClickListener { onClick?.invoke(adapterPosition) }
    }
  }
}

class DragCallback(
  private val onItemMove: (oldPosition: Int, targetPosition: Int) -> Unit,
  private val onItemSwiped: (position: Int) -> Unit
) : ItemTouchHelper.Callback() {

  override fun isLongPressDragEnabled() = true
  override fun isItemViewSwipeEnabled() = true

  override fun getMovementFlags(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder
  ): Int = makeMovementFlags(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
  )

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder
  ): Boolean {
    onItemMove(viewHolder.adapterPosition, target.adapterPosition)
    return true
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    onItemSwiped(viewHolder.adapterPosition)
  }
}