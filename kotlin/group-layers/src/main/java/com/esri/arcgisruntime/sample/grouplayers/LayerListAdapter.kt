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
        exclusiveLayerViewHolder.layer = layer
        exclusiveLayerViewHolder.textView.text = layer.name
        exclusiveLayerViewHolder.onLayerChecked = onLayerCheckedChanged
        exclusiveLayerViewHolder.sublayers.apply {
          clear()
          addAll((layer as GroupLayer).layers)
        }
        exclusiveLayerViewHolder.populate()
      }
      else -> (holder as DefaultLayerViewHolder).let { defaultLayerViewHolder ->
        val layer = dataSet[position]
        defaultLayerViewHolder.layer = layer
        defaultLayerViewHolder.textView.text = layer.name
        defaultLayerViewHolder.onLayerChecked = onLayerCheckedChanged
        defaultLayerViewHolder.sublayers.apply {
          clear()
          addAll((layer as GroupLayer).layers)
        }
        defaultLayerViewHolder.populate()
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