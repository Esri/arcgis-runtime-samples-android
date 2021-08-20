package com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.fragment.app.FragmentManager
import com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers.databinding.ListItemBinding

/**
 * Adapter to display the list of point of interests
 */
internal class ListAdapter(
    private val context: MainActivity,
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
        val listItemBinding = ListItemBinding.inflate(mLayoutInflater)

        listItemBinding.apply {
            itemButton.text = gardenSections[position].title
            //Display description dialog on button click
            itemButton.setOnClickListener {
                GardenDescriptionFragment(gardenSections[position], context).show(
                    supportFragmentManager,
                    "GardenDescriptionFragment"
                )
            }
        }

        for (gardenSection in gardenSections){
            Log.d("ITEMS: ", gardenSection.title)
        }

        return listItemBinding.root
    }

}
