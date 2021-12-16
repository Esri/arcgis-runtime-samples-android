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
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.floor.FloorManager
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.browsebuildingfloors.databinding.ActivityMainBinding

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

    // keep track of the current selected floor
    private var currentFloor = 0


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
                val floorManager = map.floorManager
                floorManager.loadAsync()

                // set initial floor level to currentFloor
                setFloor(floorManager)

                levelSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
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
                            setFloor(floorManager)
                        }
                    }
            } else {
                val error = "Error loading map or map is not floor-aware"
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                Log.e(TAG, error)
            }
        }

        // set the spinner adapter for the floor selection
        levelSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.floors,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

    }

    /**
     * update the [floorManager] to the currently selected floor
     * and disable the other floors.
     */
    private fun setFloor(floorManager: FloorManager) {
        floorManager.addDoneLoadingListener {
            if (floorManager.loadStatus == LoadStatus.LOADED) {
                // set all the floors to invisible to reset the floorManager
                floorManager.levels.forEach { floorLevel ->
                    floorLevel.isVisible = false
                }
                // set the currently selected floor to be visible
                floorManager.levels[currentFloor].isVisible = true
            } else {
                val error = "Error loading floor manager: " + floorManager.loadError.message
                Log.e(TAG, error)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
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