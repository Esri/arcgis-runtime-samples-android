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
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.navigation.DestinationStatus
import com.esri.arcgisruntime.navigation.RouteTracker
import com.esri.arcgisruntime.navigation.TrackingStatus
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask
import com.esri.arcgisruntime.tasks.networkanalysis.Stop
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_navigation_controls.*
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {

  private var textToSpeech: TextToSpeech? = null
  private var isTextToSpeechInitialized = false

  private val simulatedLocationDataSource : SimulatedLocationDataSource? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map and set it to the map view
    val map = ArcGISMap(Basemap.createStreetsVector())
    mapView.setMap(map)

    // create a graphics overlay to hold our route graphics
    val graphicsOverlay = GraphicsOverlay()
    mapView.getGraphicsOverlays().add(graphicsOverlay)

    // TODO: initialize by lazy?
    // initialize text-to-speech to replay navigation voice guidance
    textToSpeech = TextToSpeech(this){ status ->
      if (status != TextToSpeech.ERROR) {
        textToSpeech?.setLanguage(Resources.getSystem().getConfiguration().locale)
        isTextToSpeechInitialized = true
      }
    }

    // clear any graphics from the current graphics overlay
    mapView.getGraphicsOverlays().get(0).getGraphics().clear()

    // generate a route with directions and stops for navigation
    val routeTask = RouteTask(this, getString(R.string.routing_service_url))
    val routeParametersFuture = routeTask.createDefaultParametersAsync()
    routeParametersFuture.addDoneListener {

      try {
        // define the route parameters
        val routeParameters = routeParametersFuture.get()
        //TODO: What is getStops()?
        routeParameters.setStops(getStops())
        routeParameters.setReturnDirections(true)
        routeParameters.setReturnStops(true)
        routeParameters.setReturnRoutes(true)
        val routeResultFuture = routeTask.solveRouteAsync(routeParameters)
        routeParametersFuture.addDoneListener {
          try {
            // get the route geometry from the route result
            val routeResult = routeResultFuture.get()
            val routeGeometry = routeResult.getRoutes().get(0).getRouteGeometry()
            // create a graphic for the route geometry
            val routeGraphic = Graphic(routeGeometry,
                SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f))
            // add it to the graphics overlay
            mapView.getGraphicsOverlays().get(0).getGraphics().add(routeGraphic)
            // set the map view view point to show the whole route
            mapView.setViewpointAsync(Viewpoint(routeGeometry.getExtent()))

            //TODO: Change this comment
            //TODO: Check that v can be deleted once the views are fixed.
            // create a button to start navigation with the given route
            navigateRouteButton.setOnClickListener{startNavigation(routeTask, routeParameters, routeResult)}

            // start navigating
            startNavigation(routeTask, routeParameters, routeResult)
          }
          catch (e: Exception) {
            when (e) {
              is InterruptedException, is ExecutionException -> {
                val error = "Error creating default route parameters: " + e.message
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                Log.e(TAG, error)
              }
            }
          }
        }
      }
      catch (e: Exception) {
        when (e) {
          is InterruptedException, is ExecutionException -> {
            val error = "Error getting the route result " + e.message
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            Log.e(TAG, error)
          }
        }
      }
    }

    // wire up recenter button
    recenterButton.setEnabled(false)
    recenterButton.setOnClickListener {
      mapView.getLocationDisplay().setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION)
      recenterButton.setEnabled(false)
    }
  }

  private fun startNavigation(routeTask: RouteTask, routeParameters: RouteParameters, routeResult: RouteResult) {

    // clear any graphics from the current graphics overlay
    mapView.getGraphicsOverlays().get(0).getGraphics().clear()

    // get the route's geometry from the route result
    val routeGeometry = routeResult.getRoutes().get(0).getRouteGeometry()
    // create a graphic (with a dashed line symbol) to represent the route
    val routeAheadGraphic = Graphic(routeGeometry,
        SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.MAGENTA, 5f))
    mapView.getGraphicsOverlays().get(0).getGraphics().add(routeAheadGraphic)
    // create a graphic (solid) to represent the route that's been traveled (initially empty)
    val routeTraveledGraphic = Graphic(routeGeometry,
        SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f))
    mapView.getGraphicsOverlays().get(0).getGraphics().add(routeTraveledGraphic)

    // get the map view's location display
    val locationDisplay = mapView.getLocationDisplay()
    // set up a simulated location data source which simulates movement along the route
    val simulatedLocationDataSource = SimulatedLocationDataSource(routeGeometry)
    // set the simulated location data source as the location data source for this app
    locationDisplay.setLocationDataSource(simulatedLocationDataSource)
    locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION)
    // if the user navigates the map view away from the location display, activate the recenter button
    //TODO: Deleted a thing here, double check it's okay
    locationDisplay.addAutoPanModeChangedListener {recenterButton.setEnabled(true)}
    // set up a RouteTracker for navigation along the calculated route
    val routeTracker = RouteTracker(getApplicationContext(), routeResult, 0)
    routeTracker.enableReroutingAsync(routeTask, routeParameters,
        RouteTracker.ReroutingStrategy.TO_NEXT_WAYPOINT, true)

    // listen for changes in location
    locationDisplay.addLocationChangedListener { locationChangedEvent ->
      // track the location and update route tracking status
      val trackLocationFuture = routeTracker.trackLocationAsync(locationChangedEvent.getLocation())
      trackLocationFuture.addDoneListener {
        // listen for new voice guidance events
        routeTracker.addNewVoiceGuidanceListener { newVoiceGuidanceEvent ->

          //TODO: What are these functions?
          // use Android's text to speech to speak the voice guidance
          speakVoiceGuidance(newVoiceGuidanceEvent.getVoiceGuidance().getText())
          nextDirectionTextView
            .setText(
              getString(
                R.string.next_direction,
                newVoiceGuidanceEvent.getVoiceGuidance().getText()
              )
            )
        }

        // get the route's tracking status
        val trackingStatus = routeTracker.getTrackingStatus()
        // set geometries for the route ahead and the remaining route
        routeAheadGraphic.setGeometry(trackingStatus.getRouteProgress().getRemainingGeometry())
        routeTraveledGraphic.setGeometry(trackingStatus.getRouteProgress().getTraversedGeometry())

        // get remaining distance information
        val remainingDistance: TrackingStatus.Distance =
          trackingStatus.getDestinationProgress().getRemainingDistance()
        // covert remaining minutes to hours:minutes:seconds
        val remainingTimeString = DateUtils
          .formatElapsedTime((trackingStatus.getDestinationProgress().getRemainingTime() * 60).toLong())

        // update text views
        distanceRemainingTextView.setText(
          getString(
            R.string.distance_remaining, remainingDistance.getDisplayText(),
            remainingDistance.getDisplayTextUnits().getPluralDisplayName()
          )
        )
        timeRemainingTextView.setText(getString(R.string.time_remaining, remainingTimeString))

        // if a destination has been reached
        if (trackingStatus.getDestinationStatus() == DestinationStatus.REACHED) {
          // if there are more destinations to visit. Greater than 1 because the start point is considered a "stop"
          if (routeTracker.getTrackingStatus().getRemainingDestinationCount() > 1) {
            // switch to the next destination
            routeTracker.switchToNextDestinationAsync()
            Toast.makeText(
              this,
              "Navigating to the second stop, the Fleet Science Center.",
              Toast.LENGTH_LONG
            ).show()
          } else {
            // the final destination has been reached, stop the simulated location data source
            //TODO: onStop is protected? Why are we directly calling onStop anyway?
            simulatedLocationDataSource.onStop()
            Toast.makeText(this, "Arrived at the final destination.", Toast.LENGTH_LONG).show()
          }
        }
      }
    }


    // start the LocationDisplay, which starts the SimulatedLocationDataSource
    locationDisplay.startAsync()
    Toast.makeText(this, "Navigating to the first stop, the USS San Diego Memorial.", Toast.LENGTH_LONG).show()
  }

  /**
   * Uses Android's text to speak to say the latest voice guidance from the RouteTracker out loud.
   */
  private fun speakVoiceGuidance(voiceGuidanceText: String) {
    //TODO: Get rid of that bang bang
    if (isTextToSpeechInitialized && !textToSpeech?.isSpeaking()!!) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        textToSpeech?.speak(voiceGuidanceText, TextToSpeech.QUEUE_FLUSH, null, null)
      } else {
        textToSpeech?.speak(voiceGuidanceText, TextToSpeech.QUEUE_FLUSH, null)
      }
    }
  }

  //ToDO: Lifecycle arrangement
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

  companion object {
    private val TAG: String = this::class.java.simpleName

    /**
     * Creates a list of stops along a route.
     */
    private fun getStops() : List<Stop> {
      //TODO: Shoudl I just throw this down to the bottom
      val stops = mutableListOf<Stop>()
      // San Diego Convention Center
      val conventionCenter = Stop(Point(-117.160386, 32.706608, SpatialReferences.getWgs84()))
      stops.add(conventionCenter)
      // USS San Diego Memorial
      val memorial = Stop(Point(-117.173034, 32.712327, SpatialReferences.getWgs84()))
      stops.add(memorial)
      // RH Fleet Aerospace Museum
      val aerospaceMuseum = Stop(Point(-117.147230, 32.730467, SpatialReferences.getWgs84()))
      stops.add(aerospaceMuseum)
      return stops
    }
  }
}
