package com.esri.arcgisruntime.sample.createandsavekmlfile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.point_symbol.view.*


class PointSymbolAdapter(context: Context, pointSymbolUrls: List<Int>) : ArrayAdapter<Int>(context, R.layout.point_symbol, pointSymbolUrls) {

  override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
    return this.createView(position, recycledView, parent)
  }

  override fun getDropDownView(position: Int, recycledView: View?, parent: ViewGroup): View {
    return this.createView(position, recycledView, parent)
  }

  private fun createView(position: Int, recycledView: View?, parent: ViewGroup): View {
    val pointDrawable = getItem(position)!!
    val view = recycledView ?: LayoutInflater.from(context).inflate(
      R.layout.point_symbol,
      parent,
      false
    )
    view.pointSymbol.setImageResource(pointDrawable)
    return view
  }
}
