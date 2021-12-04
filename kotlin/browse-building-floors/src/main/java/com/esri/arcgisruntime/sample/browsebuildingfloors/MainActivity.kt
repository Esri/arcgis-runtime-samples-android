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

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.floor.FloorManager
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.browsebuildingfloors.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    // manages the data displayed from the floor-aware map,
    // allowing filtering based on floor levels.
    private lateinit var floorManager: FloorManager

    // keep track of the current selected floor
    private var currentFloor = 0

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

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // load the portal and add the portal item as a map to the map view
        val portal = Portal("https://www.arcgis.com/", false)
        val portalItem = PortalItem(portal, "f133a698536f44c8884ad81f80b6cfc7")
        val map = ArcGISMap(portalItem)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        mapView.map.addDoneLoadingListener {
            if (map.loadStatus == LoadStatus.LOADED) {

                //set floor manager
                floorManager = mapView.map.floorManager

                //set initial floor level to 1
                setFloor()

                levelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // do nothing here
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        // update and set the map to the selected floor
                        currentFloor = position
                        setFloor()
                    }
                }
            }
        }

        // set the spinner adapter for the floor selection
        ArrayAdapter.createFromResource(
            this,
            R.array.floors,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // bind the spinner to the adapter
            levelSpinner.adapter = adapter
        }

    }

    /**
     * update the [floorManager] to the currently selected floor
     * and disable the other floors.
     */
    private fun setFloor() {
        floorManager.loadAsync()
        floorManager.addDoneLoadingListener {
            if (floorManager.loadStatus == LoadStatus.LOADED) {
                // set all the floors to invisible to reset the floorManager
                for (i in floorManager.levels.indices) {
                    floorManager.levels[i].isVisible = false
                }
                // set the currently selected floor to be visible.
                floorManager.levels[currentFloor].isVisible = true
            }
        }
    }

    override fun onPause() {
        mapView.pause()
        currentFloor
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        // update the spinner to the currently selected floor
        levelSpinner.setSelection(currentFloor)
        mapView.resume()
    }

    override fun onDestroy() {
        mapView.dispose()
        currentFloor
        super.onDestroy()
    }
}

