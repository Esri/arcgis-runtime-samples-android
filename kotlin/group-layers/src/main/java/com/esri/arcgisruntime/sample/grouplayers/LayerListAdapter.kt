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

class LayerListAdapter(
  private val dataSet: LayerList,
  private val onLayerCheckedChanged: (layer: Layer, isChecked: Boolean) -> Unit
) :
  RecyclerView.Adapter<LayerListAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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
      TYPE_EXCLUSIVE -> (holder as ExclusiveLayerViewHolder).let {
        val layer = dataSet[position]
        it.onLayerChecked = onLayerCheckedChanged
        it.textView.text = layer.name
        it.layer = layer
        it.sublayers.apply {
          clear()
          addAll((layer as GroupLayer).layers)
        }
        it.populate()
      }
      else -> (holder as DefaultLayerViewHolder).let {
        val layer = dataSet[position]
        it.layer = layer
        it.textView.text = layer.name
        it.onLayerChecked = onLayerCheckedChanged
        if (layer is GroupLayer && layer.visibilityMode == GroupVisibilityMode.INDEPENDENT) {
          it.sublayers.apply {
            clear()
            addAll(layer.layers)
          }
          it.populate()
        }
      }
    }

    holder.textView.text = dataSet[position].name
  }

  abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract val textView: TextView
  }

  class DefaultLayerViewHolder(itemView: View) : ViewHolder(itemView) {
    override val textView: TextView = itemView.findViewById(
      R.id.checkbox_grouplayer_name
    )
    val sublayerLayout: LinearLayout = itemView.findViewById(
      R.id.sublayer_layout
    )
    val sublayers = mutableListOf<Layer>()
    var layer: Layer? = null

    var onLayerChecked: ((Layer, Boolean) -> Unit)? = null

    fun populate() {
      val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
      layer?.let {
        checkBox.setOnCheckedChangeListener { _, isChecked ->  onLayerChecked?.invoke(it, isChecked) }
        checkBox.isChecked = it.isVisible
      }
      sublayers.forEach { layer ->
        CheckBox(itemView.context).apply {
          id = View.generateViewId()
          text = layer.name
          sublayerLayout.addView(this)
          setOnCheckedChangeListener { _, isChecked -> onLayerChecked?.invoke(layer, isChecked) }
          isChecked = layer.isVisible
        }
      }
    }
  }

  class ExclusiveLayerViewHolder(itemView: View) : ViewHolder(itemView) {
    override val textView: TextView = itemView.findViewById(
      R.id.radio_group_layer_name
    )
    val radioGroup: RadioGroup = itemView.findViewById(
      R.id.radioGroup
    )
    val sublayers = mutableListOf<Layer>()
    var layer: Layer? = null

    var onLayerChecked: ((Layer, Boolean) -> Unit)? = null

    fun populate() {
      val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
      layer?.let {
        checkBox.setOnCheckedChangeListener { _, isChecked ->  onLayerChecked?.invoke(it, isChecked) }
        checkBox.isChecked = it.isVisible
      }
      sublayers.forEach { layer ->
        RadioButton(itemView.context).apply {
          id = View.generateViewId()
          text = layer.name
          radioGroup.addView(this)
          setOnCheckedChangeListener { _, isChecked -> onLayerChecked?.invoke(layer, isChecked) }
          isChecked = layer.isVisible
        }
      }
    }
  }

  private val TYPE_DEFAULT = 0;
  private val TYPE_EXCLUSIVE = 1;
}