/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.samples.scalebar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.toolkit.scalebar.Scalebar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create a map with the BasemapType topographic
        val map = ArcGISMap(Basemap.Type.STREETS_VECTOR, 45.524070, -122.679653, 14)
        // set the map to be displayed in this view
        mapView.map = map
        // add a dual unit line scalebar to MapView
        val scaleBar = Scalebar(this)
        with (scaleBar) {
            alignment = Scalebar.Alignment.LEFT
            style = Scalebar.Style.DUAL_UNIT_LINE
            addToMapView(mapView)
        }
    }
}
