package com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.list_item.view.*

/**
 * Adapter to display the list of point of interests
 */
internal class ListAdapter(
    context: MainActivity,
    //List of garden sections as POIs
    private val gardenSections: MutableList<GardenSection>,
    // Fragment manager to display description dialog on click.
    private val supportFragmentManager: FragmentManager
) :
    BaseAdapter() {

    private val mLayoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return gardenSections.size
    }

    override fun getItem(position: Int): Any {
        return gardenSections[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Bind the view to the layout inflater
        val itemView = mLayoutInflater.inflate(R.layout.list_item,null,true)

        itemView.apply {
            itemButton.text = gardenSections[position].title
            //Display description dialog on button click
            itemButton.setOnClickListener {
                GardenDescriptionFragment(gardenSections[position], context as MainActivity).show(
                    supportFragmentManager,
                    "GardenDescriptionFragment"
                )
            }
        }

        return itemView
    }

}
