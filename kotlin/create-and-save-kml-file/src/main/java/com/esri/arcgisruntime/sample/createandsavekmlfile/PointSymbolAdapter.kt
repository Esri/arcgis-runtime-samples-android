package com.esri.arcgisruntime.sample.createandsavekmlfile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.esri.arcgisruntime.sample.createandsavekmlfile.databinding.PointSymbolBinding


class PointSymbolAdapter(context: Context, pointSymbolUrls: List<Int>) :
    ArrayAdapter<Int>(context, R.layout.point_symbol, pointSymbolUrls) {

    override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val binding = PointSymbolBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        getItem(position)?.let { binding.pointSymbol.setImageResource(it) }
        return binding.root
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View = getView(position, convertView, parent)

}
