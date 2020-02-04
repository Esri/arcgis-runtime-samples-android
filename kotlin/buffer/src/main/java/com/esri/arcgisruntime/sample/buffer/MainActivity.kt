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
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.esri.arcgisruntime.geometry.GeodeticCurveType
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.LinearUnitId
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer

class MainActivity : AppCompatActivity() {

    private var mMapView: MapView? = null

    @Override
    protected fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // inflate views
        mMapView = findViewById(R.id.mapView)
        val bufferInput = findViewById(R.id.bufferInput)

        // create a map with the Basemap
        val map = ArcGISMap(SpatialReferences.getWebMercator())
        map.setBasemap(Basemap.createTopographic())
        // set the map to be displayed in this view
        mMapView!!.setMap(map)

        // create a graphics overlay to contain the buffered geometry graphics
        val graphicsOverlay = GraphicsOverlay()
        mMapView!!.getGraphicsOverlays().add(graphicsOverlay)

        // create a fill symbol for geodesic buffer polygons
        val geodesicOutlineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2)
        val geodesicBufferFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.GREEN,
                geodesicOutlineSymbol)

        // create a fill symbol for planar buffer polygons
        val planarOutlineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2)
        val planarBufferFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.RED,
                planarOutlineSymbol)

        // create a marker symbol for tap locations
        val tapSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.WHITE, 14)

        // create a graphics overlay to display geodesic polygons, set its renderer and add it to the map view.
        val geodesicGraphicsOverlay = GraphicsOverlay()
        geodesicGraphicsOverlay.setRenderer(SimpleRenderer(geodesicBufferFillSymbol))
        geodesicGraphicsOverlay.setOpacity(0.5f)
        mMapView!!.getGraphicsOverlays().add(geodesicGraphicsOverlay)

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.
        val planarGraphicsOverlay = GraphicsOverlay()
        planarGraphicsOverlay.setRenderer(SimpleRenderer(planarBufferFillSymbol))
        planarGraphicsOverlay.setOpacity(0.5f)
        mMapView!!.getGraphicsOverlays().add(planarGraphicsOverlay)

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.
        val tapLocationsOverlay = GraphicsOverlay()
        tapLocationsOverlay.setRenderer(SimpleRenderer(tapSymbol))
        mMapView!!.getGraphicsOverlays().add(tapLocationsOverlay)

        // create a buffer around the clicked location
        mMapView!!.setOnTouchListener(object : DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {

                // get the point that was clicked and convert it to a point in the map
                val screenPoint = android.graphics.Point(Math.round(motionEvent.getX()),
                        Math.round(motionEvent.getY()))
                val mapPoint = mMapView!!.screenToLocation(screenPoint)

                // only draw a buffer if a value was entered
                if (!bufferInput.getText().toString().isEmpty()) {
                    // get the buffer distance (miles) entered in the text box.
                    val bufferInMiles = Double.valueOf(bufferInput.getText().toString())

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
                    planarGraphicsOverlay.getGraphics().add(planarBufferGraphic)
                    geodesicGraphicsOverlay.getGraphics().add(geodesicBufferGraphic)
                    tapLocationsOverlay.getGraphics().add(locationGraphic)
                } else {
                    Toast.makeText(this@MainActivity, "Please enter a buffer distance first.", Toast.LENGTH_LONG).show()
                }
                return true
            }
        })

        // clear all graphics on button click
        val clearButton = findViewById(R.id.clearButton)
        clearButton.setOnClickListener({ v ->
            planarGraphicsOverlay.getGraphics().clear()
            geodesicGraphicsOverlay.getGraphics().clear()
            tapLocationsOverlay.getGraphics().clear()
        })
    }

    @Override
    protected fun onPause() {
        super.onPause()
        mMapView!!.pause()
    }

    @Override
    protected fun onResume() {
        super.onResume()
        mMapView!!.resume()
    }

    @Override
    protected fun onDestroy() {
        super.onDestroy()
        mMapView!!.dispose()
    }
}
