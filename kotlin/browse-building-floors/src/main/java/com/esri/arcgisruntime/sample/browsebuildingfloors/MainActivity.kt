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
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.browsebuildingfloors.databinding.ActivityMainBinding
import kotlin.math.floor


class MainActivity : AppCompatActivity() {

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

        val portal = Portal("https://www.arcgis.com/", false)
        val portalItem = PortalItem(portal,"f133a698536f44c8884ad81f80b6cfc7")

        val map = ArcGISMap(portalItem)
        // set the map to be displayed in the layout's MapView
        mapView.map = map

        mapView.map.addDoneLoadingListener {
            setFloor(0)

            levelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    setFloor(position)
                }
            }
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.floors,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            levelSpinner.adapter = adapter
        }

    }

    private fun setFloor(position: Int) {
        val floorManager = mapView.map.floorManager
        floorManager.loadAsync()
        floorManager.addDoneLoadingListener{
            if(floorManager.loadStatus == LoadStatus.LOADED){

                floorManager.levels[0].isVisible = false
                floorManager.levels[1].isVisible = false
                floorManager.levels[2].isVisible = false

                when (position) {
                    0 -> {
                        floorManager.levels[0].isVisible = true;
                    }
                    1 -> {
                        floorManager.levels[1].isVisible = true
                    }
                    2 -> {
                        floorManager.levels[2].isVisible = true
                    }
                }
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

