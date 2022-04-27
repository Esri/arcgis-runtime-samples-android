/*
 *  Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.navigateroute

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.format.DateUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.location.RouteTrackerLocationDataSource
import com.esri.arcgisruntime.location.SimulatedLocationDataSource
import com.esri.arcgisruntime.location.SimulationParameters
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.navigation.DestinationStatus
import com.esri.arcgisruntime.navigation.RouteTracker
import com.esri.arcgisruntime.navigation.TrackingStatus
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask
import com.esri.arcgisruntime.tasks.networkanalysis.Stop
import com.esri.arcgisruntime.sample.navigateroute.databinding.ActivityMainBinding
import java.util.Calendar
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {

    private val TAG: String = this::class.java.simpleName

    private var textToSpeech: TextToSpeech? = null

    private var isTextToSpeechInitialized = false

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val navigateRouteButton: Button by lazy {
        activityMainBinding.navigationControls.navigateRouteButton
    }

    private val recenterButton: Button by lazy {
        activityMainBinding.navigationControls.recenterButton
    }

    private val distanceRemainingTextView: TextView by lazy {
        activityMainBinding.navigationControls.distanceRemainingTextView
    }

    private val timeRemainingTextView: TextView by lazy {
        activityMainBinding.navigationControls.timeRemainingTextView
    }

    private val nextDirectionTextView: TextView by lazy {
        activityMainBinding.navigationControls.nextDirectionTextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map and set it to the map view
        mapView.map = ArcGISMap(BasemapStyle.ARCGIS_STREETS)

        // create a graphics overlay to hold our route graphics and clear any graphics
        mapView.graphicsOverlays.add(GraphicsOverlay())

        // create text-to-speech to replay navigation voice guidance
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech?.language = Resources.getSystem()
                    .configuration.locale
                isTextToSpeechInitialized = true
            }
        }

        // generate a route with directions and stops for navigation
        val routeTask = RouteTask(this, getString(R.string.routing_service_url))
        val routeParametersFuture = routeTask.createDefaultParametersAsync()
        routeParametersFuture.addDoneListener {

            // define the route parameters
            val routeParameters = routeParametersFuture.get().apply {
                try {
                    setStops(routeStops)
                    isReturnDirections = true
                    isReturnStops = true
                    isReturnRoutes = true
                } catch (e: Exception) {
                    when (e) {
                        is InterruptedException, is ExecutionException -> {
                            val error = "Error getting the default route parameters: " + e.message
                            Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                            Log.e(TAG, error)
                        }
                        else -> throw e
                    }
                }
            }

            val routeResultFuture = routeTask.solveRouteAsync(routeParameters)
            routeResultFuture.addDoneListener {
                try {
                    // get the route geometry from the route result
                    val routeResult = routeResultFuture.get()
                    val routeGeometry = routeResult.routes[0].routeGeometry
                    // create a graphic for the route geometry
                    val routeGraphic = Graphic(
                        routeGeometry,
                        SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f)
                    )
                    // add it to the graphics overlay
                    mapView.graphicsOverlays[0].graphics.add(routeGraphic)
                    // set the map view view point to show the whole route
                    mapView.setViewpointAsync(Viewpoint(routeGeometry.extent))

                    // set button to start navigation with the given route
                    navigateRouteButton.setOnClickListener {
                        startNavigation(
                            routeTask,
                            routeParameters,
                            routeResult
                        )
                    }

                    // start navigating
                    startNavigation(routeTask, routeParameters, routeResult)
                } catch (e: Exception) {
                    when (e) {
                        is InterruptedException, is ExecutionException -> {
                            val error = "Error creating the route result: " + e.message
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                            Log.e(TAG, error)
                        }
                        else -> throw e
                    }
                }
            }
        }

        // wire up recenter button
        recenterButton.apply {
            isEnabled = false
            setOnClickListener {
                mapView.locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
                recenterButton.isEnabled = false
            }
        }
    }

    /**
     * Start the navigation along the provided route.
     *
     * @param routeTask used to generate the route.
     * @param routeParameters to describe the route.
     * @param routeResult solved from the routeTask.
     * */
    private fun startNavigation(
        routeTask: RouteTask,
        routeParameters: RouteParameters,
        routeResult: RouteResult
    ) {

        // clear any graphics from the current graphics overlay
        mapView.graphicsOverlays[0].graphics.clear()

        // get the route's geometry from the route result
        val routeGeometry = routeResult.routes[0].routeGeometry
        // create a graphic (with a dashed line symbol) to represent the route
        val routeAheadGraphic = Graphic(
            routeGeometry,
            SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.MAGENTA, 5f)
        )
        // create a graphic (solid) to represent the route that's been traveled (initially empty)
        val routeTraveledGraphic = Graphic(
            routeGeometry,
            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f)
        )
        // add the graphics to the mapView's graphics overlays
        mapView.graphicsOverlays[0].graphics.addAll(listOf(routeAheadGraphic, routeTraveledGraphic))

        // set up a simulated location data source which simulates movement along the route
        val simulationParameters = SimulationParameters(Calendar.getInstance(), 35.0, 5.0, 5.0)
        val simulatedLocationDataSource = SimulatedLocationDataSource().apply {
            setLocations(routeGeometry, simulationParameters)
        }

        // set up a RouteTracker for navigation along the calculated route
        val routeTracker = RouteTracker(applicationContext, routeResult, 0, true).apply {
            enableReroutingAsync(
                routeTask,
                routeParameters,
                RouteTracker.ReroutingStrategy.TO_NEXT_WAYPOINT,
                true
            )
        }

        // create a route tracker location data source to snap the location display to the route
        val routeTrackerLocationDataSource =
            RouteTrackerLocationDataSource(routeTracker, simulatedLocationDataSource)
        // get the map view's location display and set it up
        val locationDisplay = mapView.locationDisplay.apply {
            // set the simulated location data source as the location data source for this app
            locationDataSource = routeTrackerLocationDataSource
            autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
            // if the user navigates the map view away from the location display, activate the recenter button
            addAutoPanModeChangedListener { recenterButton.isEnabled = true }
        }

        // listen for changes in location
        locationDisplay.addLocationChangedListener {
            // get the route's tracking status
            val trackingStatus = routeTracker.trackingStatus
            // set geometries for the route ahead and the remaining route
            routeAheadGraphic.geometry = trackingStatus.routeProgress.remainingGeometry
            routeTraveledGraphic.geometry = trackingStatus.routeProgress.traversedGeometry

            // get remaining distance information
            val remainingDistance: TrackingStatus.Distance =
                trackingStatus.destinationProgress.remainingDistance
            // covert remaining minutes to hours:minutes:seconds
            val remainingTimeString = DateUtils
                .formatElapsedTime((trackingStatus.destinationProgress.remainingTime * 60).toLong())

            // update text views
            distanceRemainingTextView.text = getString(
                R.string.distance_remaining, remainingDistance.displayText,
                remainingDistance.displayTextUnits.pluralDisplayName
            )
            timeRemainingTextView.text = getString(R.string.time_remaining, remainingTimeString)

            // listen for new voice guidance events
            routeTracker.addNewVoiceGuidanceListener { newVoiceGuidanceEvent ->
                // use Android's text to speech to speak the voice guidance
                speakVoiceGuidance(newVoiceGuidanceEvent.voiceGuidance.text)
                nextDirectionTextView.text = getString(
                    R.string.next_direction,
                    newVoiceGuidanceEvent.voiceGuidance.text
                )
            }

            // if a destination has been reached
            if (trackingStatus.destinationStatus == DestinationStatus.REACHED) {
                // if there are more destinations to visit. Greater than 1 because the start point is considered a "stop"
                if (routeTracker.trackingStatus.remainingDestinationCount > 1) {
                    // switch to the next destination
                    routeTracker.switchToNextDestinationAsync()
                    Toast.makeText(
                        this,
                        "Navigating to the second stop, the Fleet Science Center.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // the final destination has been reached, stop the simulated location data source
                    simulatedLocationDataSource.stop()
                    routeTrackerLocationDataSource.stop()
                    Toast.makeText(this, "Arrived at the final destination.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
        // start the LocationDisplay, which starts the RouteTrackerLocationDataSource and SimulatedLocationDataSource
        locationDisplay.startAsync()
        Toast.makeText(
            this,
            "Navigating to the first stop, the USS San Diego Memorial.",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Uses Android's text to speak to say the latest voice guidance from the RouteTracker out loud.
     *
     * @param voiceGuidanceText to be converted to speech
     */
    private fun speakVoiceGuidance(voiceGuidanceText: String) {
        if (isTextToSpeechInitialized && textToSpeech?.isSpeaking == false) {
            textToSpeech?.speak(voiceGuidanceText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onPause() {
        mapView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }
}

private val routeStops by lazy {
    listOf(
        // San Diego Convention Center
        Stop(Point(-117.160386, 32.706608, SpatialReferences.getWgs84())),
        // USS San Diego Memorial
        Stop(Point(-117.173034, 32.712327, SpatialReferences.getWgs84())),
        // RH Fleet Aerospace Museum
        Stop(Point(-117.147230, 32.730467, SpatialReferences.getWgs84()))
    )
}
