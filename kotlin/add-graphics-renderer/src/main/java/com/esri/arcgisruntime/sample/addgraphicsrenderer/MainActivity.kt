/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.addgraphicsrenderer

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PolygonBuilder
import com.esri.arcgisruntime.geometry.PolylineBuilder
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create a map with the Basemap Type topographic
        val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 15.169193, 16.333479, 2)

        // add graphics overlays to the map view
        mapView.graphicsOverlays.apply {
            // add a point graphic overlay to the map view
            add(GraphicsOverlay().apply {
                // create red diamond simple renderer
                renderer = SimpleRenderer(SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.RED, 10f))
                // define point geometry
                graphics.add(Graphic(Point(40e5, 40e5, SpatialReferences.getWebMercator())))
            })

            // add a line graphic overlay to the map view
            add(GraphicsOverlay().apply {
                // create blue line simple renderer
                renderer = SimpleRenderer(SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f))
                // define line geometry
                graphics.add(Graphic(PolylineBuilder(SpatialReferences.getWebMercator()).apply {
                    addPoint(-10e5, 40e5)
                    addPoint(20e5, 50e5)
                }.toGeometry()))
            })

            // add a polygon graphic overlay to the map view
            add(GraphicsOverlay().apply {
                // create yellow fill simple renderer
                renderer = SimpleRenderer(SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.YELLOW, null))
                // define polygon geometry
                graphics.add(Graphic(PolygonBuilder(SpatialReferences.getWebMercator()).apply {
                    addPoint(-20e5, 20e5)
                    addPoint(20e5, 20e5)
                    addPoint(20e5, -20e5)
                    addPoint(-20e5, -20e5)
                }.toGeometry()))
            })
        }

        // set the map to the map view
        mapView.map = map
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.dispose()
    }
}
