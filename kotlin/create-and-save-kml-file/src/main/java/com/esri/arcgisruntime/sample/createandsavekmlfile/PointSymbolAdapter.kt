package com.esri.arcgisruntime.sample.createandsavekmlfile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.esri.arcgisruntime.sample.createandsavekmlfile.databinding.PointSymbolBinding


class PointSymbolAdapter(context: Context, pointSymbolUrls: List<Int>) :
    ArrayAdapter<Int>(context, R.layout.point_symbol, pointSymbolUrls) {

    private val pointSymbolBinding by lazy {
        PointSymbolBinding.inflate(LayoutInflater.from(context))
    }

    override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }

    override fun getDropDownView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }

    private fun createView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val pointDrawable = getItem(position)!!
        val view = recycledView ?: pointSymbolBinding.root
        pointSymbolBinding.pointSymbol.setImageResource(pointDrawable)
        return view
    }
}
