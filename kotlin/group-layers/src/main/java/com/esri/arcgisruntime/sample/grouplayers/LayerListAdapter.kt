/*
 * Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.grouplayers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.layers.GroupLayer
import com.esri.arcgisruntime.layers.GroupVisibilityMode
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.LayerList

/**
 * A custom RecyclerView.Adapter to display group layers and sublayers, accounting for group layers
 * with an exclusive visibility mode.
 *
 * @param dataSet the list of layers for the scene
 * @param onLayerCheckedChanged a callback function which is invoked by each layer and sublayer's onCheckedChangedListener
 */
class LayerListAdapter(
  private val dataSet: LayerList,
  private val onLayerCheckedChanged: (layer: Layer, isChecked: Boolean) -> Unit
) :
  RecyclerView.Adapter<LayerListAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    // There are two view holder types, for independent and exclusive visibility modes
    return when (viewType) {
      TYPE_EXCLUSIVE -> {
        val v = LayoutInflater.from(parent.context).inflate(
          R.layout.radio_group, parent, false
        )
        ExclusiveLayerViewHolder(v) as ViewHolder
      }
      else -> {
        val v = LayoutInflater.from(parent.context).inflate(
          R.layout.checkbox_group, parent, false
        )
        DefaultLayerViewHolder(v) as ViewHolder
      }
    }
  }


  override fun getItemViewType(position: Int): Int {
    val layer = dataSet[position]
    return if (layer is GroupLayer && layer.visibilityMode == GroupVisibilityMode.EXCLUSIVE) {
      TYPE_EXCLUSIVE
    } else TYPE_DEFAULT
  }

  override fun getItemCount(): Int = dataSet.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    when (holder.itemViewType) {
      TYPE_EXCLUSIVE -> (holder as ExclusiveLayerViewHolder).let { exclusiveLayerViewHolder ->
        val layer = dataSet[position]
        exclusiveLayerViewHolder.apply {
          this.layer = layer
          textView.text = layer.name
          onLayerChecked = onLayerCheckedChanged
          sublayers.apply {
            clear()
            addAll((layer as GroupLayer).layers)
          }
          populate()
        }
      }
      else -> (holder as DefaultLayerViewHolder).let { defaultLayerViewHolder ->
        val layer = dataSet[position]
        defaultLayerViewHolder.apply {
          this.layer = layer
          textView.text = layer.name
          onLayerChecked = onLayerCheckedChanged
          sublayers.apply {
            clear()
            addAll((layer as GroupLayer).layers)
          }
          populate()
        }
      }
    }
  }


  abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract val textView: TextView
  }

  class DefaultLayerViewHolder(itemView: View) : ViewHolder(itemView) {
    override val textView: TextView = itemView.findViewById(
      R.id.checkbox_grouplayer_name
    )
    private val sublayerLayout: LinearLayout = itemView.findViewById(
      R.id.sublayer_layout
    )
    val sublayers = mutableListOf<Layer>()
    var layer: Layer? = null

    var onLayerChecked: ((Layer, Boolean) -> Unit)? = null

    /**
     * Sets the OnCheckedChangeListener of the top-level layer
     * and creates checkboxes for each of the layer's sublayers with a label and OnCheckedChangeListener.
     */
    fun populate() {
      val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
      layer?.let { layer ->
        checkBox.setOnCheckedChangeListener { _, isChecked ->
          onLayerChecked?.invoke(layer, isChecked)
        }
        checkBox.isChecked = layer.isVisible
      }
      sublayers.forEach { sublayer ->
        CheckBox(itemView.context).apply {
          id = View.generateViewId()
          text = sublayer.name
          sublayerLayout.addView(this)
          setOnCheckedChangeListener { _, isChecked -> onLayerChecked?.invoke(sublayer, isChecked) }
          isChecked = sublayer.isVisible
        }
      }
    }
  }

  class ExclusiveLayerViewHolder(itemView: View) : ViewHolder(itemView) {
    override val textView: TextView = itemView.findViewById(
      R.id.radio_group_layer_name
    )
    private val radioGroup: RadioGroup = itemView.findViewById(
      R.id.radioGroup
    )
    val sublayers = mutableListOf<Layer>()
    var layer: Layer? = null

    var onLayerChecked: ((Layer, Boolean) -> Unit)? = null

    /**
     * Sets the OnCheckedChangeListener of the top-level layer
     * and creates radio buttons for each of the layer's sublayers with a label and OnCheckedChangeListener.
     */
    fun populate() {
      val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
      layer?.let { layer ->
        checkBox.setOnCheckedChangeListener { _, isChecked ->
          onLayerChecked?.invoke(layer, isChecked)
        }
        checkBox.isChecked = layer.isVisible
      }
      sublayers.forEach { sublayer ->
        RadioButton(itemView.context).apply {
          id = View.generateViewId()
          text = sublayer.name
          radioGroup.addView(this)
          setOnCheckedChangeListener { _, isChecked -> onLayerChecked?.invoke(sublayer, isChecked) }
          isChecked = sublayer.isVisible
        }
      }
    }
  }

  private val TYPE_DEFAULT = 0;
  private val TYPE_EXCLUSIVE = 1;
}