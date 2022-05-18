/* Copyright 2021 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.browsebuildingfloors

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.floor.FloorLevel
import com.esri.arcgisruntime.mapping.floor.FloorManager
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.browsebuildingfloors.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.browsebuildingfloors.databinding.BrowseFloorsSpinnerItemBinding


class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val levelSpinner: Spinner by lazy {
        activityMainBinding.levelSpinner
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // load the portal and create a map from the portal item
        val portal = Portal("https://www.arcgis.com/", false)
        val portalItem = PortalItem(portal, "f133a698536f44c8884ad81f80b6cfc7")
        val map = ArcGISMap(portalItem)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        map.addDoneLoadingListener {
            if (map.loadStatus == LoadStatus.LOADED && map.floorDefinition != null) {
                // get and load the floor manager
                val floorManager = map.floorManager.apply { loadAsync() }
                // set initial floor level to currentFloor
                setFloor(floorManager)

            } else {
                val error = "Error loading map or map is not floor-aware"
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                Log.e(TAG, error)
            }
        }
    }

    /**
     * update the [floorManager] to the currently selected floor
     * and disable the other floors.
     */
    private fun setFloor(floorManager: FloorManager) {
        //
        floorManager.addDoneLoadingListener {
            if (floorManager.loadStatus == LoadStatus.LOADED) {
                levelSpinner.apply {
                    // set the spinner adapter for the floor selection
                    adapter = FloorsAdapter(this@MainActivity, floorManager.levels)
                    // handle on spinner item selected
                    onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parentView: AdapterView<*>?,
                                selectedItemView: View?,
                                position: Int,
                                id: Long
                            ) {
                                // set all the floors to invisible to reset the floorManager
                                floorManager.levels.forEach { floorLevel ->
                                    floorLevel.isVisible = false
                                }
                                // set the currently selected floor to be visible
                                floorManager.levels[position].isVisible = true
                            }

                            // ignore if nothing is selected
                            override fun onNothingSelected(parentView: AdapterView<*>?) {}
                        }
                    // select the ground floor using `verticalOrder`.
                    // in the case of buildings with basements, they can have negative
                    // vertical orders; ground floor is expected to be 0.
                    // you can also use level ID, number or name to locate a floor.
                    setSelection(floorManager.levels.indexOf(
                        floorManager.levels.first { it.verticalOrder == 0 }
                    ))
                }

            } else {
                val error = "Error loading floor manager: " + floorManager.loadError.message
                Log.e(TAG, error)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Adapter to display a list of floor levels
     */
    private class FloorsAdapter(
        context: Context,
        private var floorLevels: MutableList<FloorLevel>
    ) :
        BaseAdapter() {

        private val mLayoutInflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return floorLevels.size
        }

        override fun getItem(position: Int): Any {
            return floorLevels[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            // bind the view to the layout inflater
            val listItemBinding =
                BrowseFloorsSpinnerItemBinding.inflate(this.mLayoutInflater).apply {
                    // bind the long name of the floor to it's respective text view
                    listItem.text = floorLevels[position].longName
                }
            return listItemBinding.root
        }
    }

    override fun onPause() {
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }
}
