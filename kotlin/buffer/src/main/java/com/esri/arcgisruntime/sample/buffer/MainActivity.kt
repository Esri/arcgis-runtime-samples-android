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
package com.esri.arcgisruntime.sample.buffer

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //UNSURE: I originally had this down below with the graphics overlay stuff, but moved it out
        // to prevent too much nesting.
        // the traceutilitynetwork sample has a triple nested apply{apply{apply}}}.

        // create a map with the Basemap
        mapView.map = ArcGISMap(SpatialReferences.getWebMercator()).apply {
            basemap = Basemap.createTopographic() //UNSURE: Is the apply block necessary here? Should I not just separate it into two lines?
        }

        // create a fill symbol for geodesic buffer polygons
        val geodesicOutlineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2F)
        val geodesicBufferFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.GREEN,
                geodesicOutlineSymbol)

        // create a fill symbol for planar buffer polygons
        val planarOutlineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2F)
        val planarBufferFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.RED,
                planarOutlineSymbol)

        // create a marker symbol for tap locations
        val tapSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.WHITE, 14F)

        // create a graphics overlay to display geodesic polygons and set its renderer.
        val geodesicGraphicsOverlay = GraphicsOverlay().apply {
            renderer = SimpleRenderer(geodesicBufferFillSymbol)
            opacity = 0.5f
        }

        // create a graphics overlay to display planar polygons and set its renderer.
        val planarGraphicsOverlay = GraphicsOverlay().apply {
            renderer = SimpleRenderer(planarBufferFillSymbol)
            opacity = 0.5f
        }

        // create a graphics overlay to display tap locations for buffers and set its renderer.
        val tapLocationsOverlay = GraphicsOverlay().apply {
            renderer = SimpleRenderer(tapSymbol)
        }

        // add overlays to the mapView.
        mapView.graphicsOverlays.apply {
            add(geodesicGraphicsOverlay)
            add(planarGraphicsOverlay)
            add(tapLocationsOverlay)
        }

        // create a buffer around the clicked location

        mapView.onTouchListener = object: DefaultMapViewOnTouchListener(applicationContext, mapView){
            override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {

                // get the point that was clicked and convert it to a point in the map
                val screenPoint = android.graphics.Point(motionEvent.x.roundToInt(),
                        motionEvent.y.roundToInt())
                val mapPoint = mapView.screenToLocation(screenPoint)

                // only draw a buffer if a value was entered
                if (bufferInput.text.toString().isNotEmpty()) {
                    // get the buffer distance (miles) entered in the text box.
//                    val bufferInMiles = Double.valueOf(bufferInput.getText().toString())
                    val bufferInMiles = bufferInput.text.toString().toDouble()

                    // convert the input distance to meters, 1609.34 meters in one mile
                    val bufferInMeters = bufferInMiles * 1609.34

                    // create a planar buffer graphic around the input location at the specified distance
                    val bufferGeometryPlanar = GeometryEngine.buffer(mapPoint, bufferInMeters)
                    val planarBufferGraphic = Graphic(bufferGeometryPlanar)

                    // create a geodesic buffer graphic using the same location and distance
                    val bufferGeometryGeodesic = GeometryEngine.bufferGeodetic(mapPoint, bufferInMeters,
                            LinearUnit(LinearUnitId.METERS), Double.NaN, GeodeticCurveType.GEODESIC)
                    val geodesicBufferGraphic = Graphic(bufferGeometryGeodesic)

                    // create a graphic for the user tap location
                    val locationGraphic = Graphic(mapPoint)

                    // add the buffer polygons and tap location graphics to the appropriate graphic overlays.
                    planarGraphicsOverlay.graphics.add(planarBufferGraphic)
                    geodesicGraphicsOverlay.graphics.add(geodesicBufferGraphic)
                    tapLocationsOverlay.graphics.add(locationGraphic)
                } else {
                    Toast.makeText(this@MainActivity, "Please enter a buffer distance first.", Toast.LENGTH_LONG).show()
                }
                return true
            }
        }

        clearButton.setOnClickListener {
            planarGraphicsOverlay.graphics.clear()
            geodesicGraphicsOverlay.graphics.clear()
            tapLocationsOverlay.graphics.clear()
        }
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
