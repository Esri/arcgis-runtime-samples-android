package com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.fragment.app.FragmentManager
import com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers.databinding.ListItemBinding

internal class ListAdapter(
    context: Context,
    gardenSections: MutableList<GardenSection>,
    supportFragmentManager: FragmentManager
) :
    BaseAdapter() {

    private val mGardenSections = gardenSections
    private val mSupportFragmentManager = supportFragmentManager

    private val mLayoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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
            itemButton.setOnClickListener {
                GardenDescriptionFragment(mGardenSections[position]).show(
                    mSupportFragmentManager,
                    "GardenDescriptionFragment"
                )
            }
        }

        return listItemBinding.root
    }

}
