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
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.location.LocationDataSource
import com.esri.arcgisruntime.location.RouteTrackerLocationDataSource
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.navigation.ReroutingParameters
import com.esri.arcgisruntime.navigation.RouteTracker
import com.esri.arcgisruntime.navigation.TrackingStatus
import com.esri.arcgisruntime.sample.navigateroutewithrerouting.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.tasks.networkanalysis.*


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

    // the route tracker for navigation. Use delegate methods to update tracking status
    var routeTracker: RouteTracker? = null

    // a list to keep track of directions solved by the route task
    val directionManeuvers = mutableListOf<DirectionManeuver>()

    // the original view point that can be reset later on
    var defaultViewPoint: Viewpoint? = null

    private var routeResult: RouteResult? = null
    private var route: Route? = null
    private val directionsList = mutableListOf<DirectionManeuver>()
    private var routeParameters: RouteParameters? = null

    // the route task to solve the route between stops, using the online routing service
    private lateinit var routeTask: RouteTask


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // set the map to be displayed in the layout's MapView
        mapView.apply {
            map = ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION)
            graphicsOverlays.add(GraphicsOverlay())
        }

        navigateButton.setOnClickListener {
            startNavigation()
        }
        val tpkxPath = getExternalFilesDir(null)?.path + "/sandiego.geodatabase"
        routeTask = RouteTask(this, tpkxPath, "Streets_ND")
        routeTask.loadAsync()
        routeTask.addDoneLoadingListener {
            // the parameters of the route tracker
            val routeParametersFuture = routeTask.createDefaultParametersAsync()
            routeParametersFuture.addDoneListener {
                routeParameters = routeParametersFuture.get()
                // explicitly set values for parameters
                routeParameters?.isReturnDirections = true
                routeParameters?.isReturnStops = true
                routeParameters?.isReturnRoutes = true
                routeParameters?.outputSpatialReference = SpatialReferences.getWgs84()
                // the starting location, the San Diego Convention Center
                val conventionCenter =
                    Point(-117.160386727, 32.706608, SpatialReferences.getWgs84())
                // the destination location, the Fleet Science Center
                val aerospaceMuseum = Point(-117.146679, 32.730351, SpatialReferences.getWgs84())
                // create starting and destination stops
                val startingStop = Stop(conventionCenter).apply {
                    name = "San Diego Convention Center"
                }
                val destinationStop = Stop(aerospaceMuseum).apply {
                    name = "RH Fleet Aerospace Museum"
                }
                // assign the stops to the route parameters
                routeParameters?.setStops(mutableListOf(startingStop, destinationStop))
                // Get the route results
                val routeResultFuture = routeTask.solveRouteAsync(routeParameters)
                routeResultFuture.addDoneListener {
                    routeResult = routeResultFuture.get()
                    route = routeResult?.routes?.get(0)
                    val stopSymbol =
                        SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.RED, 20F)
                    // the graphic (with a dashed line symbol) to represent the route ahead
                    val routeAheadGraphic =
                        Graphic(
                            route?.routeGeometry,
                            SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.MAGENTA, 5F)
                        )
                    // the graphic to represent the route that's been traveled (initially empty)
                    val routeTraveledGraphic = Graphic().apply {
                        symbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 3F)
                    }
                    mapView.graphicsOverlays[0].graphics.addAll(
                        listOf(
                            Graphic(conventionCenter, stopSymbol),
                            Graphic(aerospaceMuseum, stopSymbol),
                            routeAheadGraphic,
                            routeTraveledGraphic
                        )
                    )
                    mapView.setViewpointGeometryAsync(route?.routeGeometry, 100.0)
                    navigateButton.isEnabled = true
                }
            }
        }
    }

    private fun startNavigation() {
        // disable the start navigation button
        navigateButton.isEnabled = false

        // get the directions for the route
        route?.directionManeuvers?.let { directionManeuvers.addAll(it) }

        // create a route tracker
        val routeTracker = RouteTracker(this, routeResult, 0, true)

        // handle route tracking status changes
        routeTracker.addTrackingStatusChangedListener {
            updateTrackingStatus(it.trackingStatus)
        }

        // check if this route task supports rerouting
        if (routeTask.routeTaskInfo.isSupportsRerouting) {
            val reroutingParameters = ReroutingParameters(routeTask, routeParameters).apply {
                strategy = RouteTracker.ReroutingStrategy.TO_NEXT_WAYPOINT
                isVisitFirstStopOnStart = false
            }
            // enable automatic re-routing
            val routingFuture = routeTracker.enableReroutingAsync(reroutingParameters)
            routingFuture.addDoneListener {
                routeTracker.addRerouteStartedListener {
                    //TODO
                }
                routeTracker.addRerouteCompletedListener {
                    //TODO
                }
            }
        }
        // turn on navigation mode for the map view
        mapView.locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
        mapView.locationDisplay.addAutoPanModeChangedListener {
            //TODO
        }

        val gpxRoutePath = getExternalFilesDir(null)?.path + "/navigate_a_route_detour.gpx"
        // The location data source provided by a local GPX file
        val gpxDataSource: LocationDataSource = GpxProvider(gpxRoutePath)

        // add a data source for the location display
        mapView.locationDisplay.locationDataSource =
            RouteTrackerLocationDataSource(routeTracker, gpxDataSource)

        // use this instead if you want real location:
        //mapView.locationDisplay.locationDataSource = RouteTrackerLocationDataSource(routeTracker, AndroidLocationDataSource(this))

        // Enable the location display (this will start the location data source)
        mapView.locationDisplay.startAsync()
    }

    private fun updateTrackingStatus(trackingStatus: TrackingStatus) {

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
