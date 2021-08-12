package com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers.databinding.ListItemBinding

internal class ListAdapter(context: Context, gardenSections: MutableList<GardenSection>) : BaseAdapter() {

    private val mGardenSections = gardenSections
    private val mContext = context
    private val mLayoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return mGardenSections.size
    }

    override fun getItem(position: Int): Any {
        return mGardenSections[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val listItemBinding = ListItemBinding.inflate(mLayoutInflater)

        listItemBinding.apply {
            itemButton.text = mGardenSections[position].title
            itemButton.setOnClickListener{
                val alertDialog: AlertDialog.Builder = AlertDialog.Builder(mContext)
                alertDialog.setTitle(mGardenSections[position].title)
                alertDialog.setMessage(mGardenSections[position].description)

                // Displays the where clause dialog
                val alert: AlertDialog = alertDialog.create()
                alert.show()
            }
        }

        return listItemBinding.root
    }

}
