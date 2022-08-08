/* Copyright 2022 Esri
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

package com.esri.arcgisruntime.sample.navigateroutewithrerouting

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.location.LocationDataSource
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.navigation.RouteTracker
import com.esri.arcgisruntime.sample.navigateroutewithrerouting.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.networkanalysis.DirectionManeuver
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val navigateButton: Button by lazy {
        activityMainBinding.navigateButton
    }

    private val resetButton: Button by lazy {
        activityMainBinding.resetButton
    }

    private val recenterButton: Button by lazy {
        activityMainBinding.recenterButton
    }

    val routePath = getExternalFilesDir(null)?.path + "navigate_a_route_detour.gpx"
    val tpkxPath = getExternalFilesDir(null)?.path + "sandiego.geodatabase"

    // the route task to solve the route between stops, using the online routing service
    val routeTask = RouteTask(this, tpkxPath, "Streets_ND")

    // the route result solved by the route task
    var routeResult: RouteResult? = null

    // the route tracker for navigation. Use delegate methods to update tracking status
    var routeTracker: RouteTracker? = null

    // the parameters of the route tracker
    var routeParameters: RouteParameters? = null

    // a list to keep track of directions solved by the route task
    val directionManeuvers = mutableListOf<DirectionManeuver>()

    // the graphic (with a dashed line symbol) to represent the route ahead
    val routeAheadGraphic =
        Graphic(null, SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.MAGENTA, 5F))

    // the graphic to represent the route that's been traveled (initially empty)
    val routeTraveledGraphic =
        Graphic(null, SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 3F))

    // the original view point that can be reset later on
    var defaultViewPoint: Viewpoint? = null

    // the starting location, the San Diego Convention Center
    val startLocation = Point(-117.160386727, 32.706608, SpatialReferences.getWgs84())

    // the destination location, the Fleet Science Center
    val destinationLocation = Point(-117.146679, 32.730351, SpatialReferences.getWgs84())

    // The location data source provided by a local GPX file
    val gpxDataSource: LocationDataSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION_NIGHT)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        mapView.setViewpoint(Viewpoint(34.056295, -117.195800, 10000.0))

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
