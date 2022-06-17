/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package com.esri.arcgisruntime.sample.navigateroute;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.location.RouteTrackerLocationDataSource;
import com.esri.arcgisruntime.location.SimulatedLocationDataSource;
import com.esri.arcgisruntime.location.SimulationParameters;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.navigation.DestinationStatus;
import com.esri.arcgisruntime.navigation.ReroutingParameters;
import com.esri.arcgisruntime.navigation.RouteTracker;
import com.esri.arcgisruntime.navigation.TrackingStatus;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private TextToSpeech mTextToSpeech;
  private boolean mIsTextToSpeechInitialized = false;

  private SimulatedLocationDataSource mSimulatedLocationDataSource;

  private MapView mMapView;
  private RouteTracker mRouteTracker;
  private Graphic mRouteAheadGraphic;
  private Graphic mRouteTraveledGraphic;
  private Button mRecenterButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a map and set it to the map view
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_STREETS);
    mMapView.setMap(map);

    // create a graphics overlay to hold our route graphics
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // initialize text-to-speech to replay navigation voice guidance
    mTextToSpeech = new TextToSpeech(this, status -> {
      if (status != TextToSpeech.ERROR) {
        mTextToSpeech.setLanguage(Resources.getSystem().getConfiguration().locale);
        mIsTextToSpeechInitialized = true;
      }
    });

    // clear any graphics from the current graphics overlay
    mMapView.getGraphicsOverlays().get(0).getGraphics().clear();

    // generate a route with directions and stops for navigation
    RouteTask routeTask = new RouteTask(this, getString(R.string.routing_service_url));
    ListenableFuture<RouteParameters> routeParametersFuture = routeTask.createDefaultParametersAsync();
    routeParametersFuture.addDoneListener(() -> {

      try {
        // define the route parameters
        RouteParameters routeParameters = routeParametersFuture.get();
        routeParameters.setStops(getStops());
        routeParameters.setReturnDirections(true);
        routeParameters.setReturnStops(true);
        routeParameters.setReturnRoutes(true);
        ListenableFuture<RouteResult> routeResultFuture = routeTask.solveRouteAsync(routeParameters);
        routeParametersFuture.addDoneListener(() -> {
          try {
            // get the route geometry from the route result
            RouteResult routeResult = routeResultFuture.get();
            Polyline routeGeometry = routeResult.getRoutes().get(0).getRouteGeometry();
            // create a graphic for the route geometry
            Graphic routeGraphic = new Graphic(routeGeometry,
                new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f));
            // add it to the graphics overlay
            mMapView.getGraphicsOverlays().get(0).getGraphics().add(routeGraphic);
            // set the map view view point to show the whole route
            mMapView.setViewpointAsync(new Viewpoint(routeGeometry.getExtent()));

            // create a button to start navigation with the given route
            Button navigateRouteButton = findViewById(R.id.navigateRouteButton);
            navigateRouteButton.setOnClickListener(v -> startNavigation(routeTask, routeParameters, routeResult));

            // start navigating
            startNavigation(routeTask, routeParameters, routeResult);
          } catch (ExecutionException | InterruptedException e) {
            String error = "Error creating default route parameters: " + e.getMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
      } catch (InterruptedException | ExecutionException e) {
        String error = "Error getting the route result " + e.getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });

    // wire up recenter button
    mRecenterButton = findViewById(R.id.recenterButton);
    mRecenterButton.setEnabled(false);
    mRecenterButton.setOnClickListener(v -> {
      mMapView.getLocationDisplay().setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
      mRecenterButton.setEnabled(false);
    });
  }

  private void startNavigation(RouteTask routeTask, RouteParameters routeParameters, RouteResult routeResult) {

    // clear any graphics from the current graphics overlay
    mMapView.getGraphicsOverlays().get(0).getGraphics().clear();

    // get the route's geometry from the route result
    Polyline routeGeometry = routeResult.getRoutes().get(0).getRouteGeometry();
    // create a graphic (with a dashed line symbol) to represent the route
    mRouteAheadGraphic = new Graphic(routeGeometry,
        new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.MAGENTA, 5f));
    mMapView.getGraphicsOverlays().get(0).getGraphics().add(mRouteAheadGraphic);
    // create a graphic (solid) to represent the route that's been traveled (initially empty)
    mRouteTraveledGraphic = new Graphic(routeGeometry,
        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f));
    mMapView.getGraphicsOverlays().get(0).getGraphics().add(mRouteTraveledGraphic);

    // get the map view's location display
    LocationDisplay locationDisplay = mMapView.getLocationDisplay();
    // set up a simulated location data source which simulates movement along the route
    mSimulatedLocationDataSource = new SimulatedLocationDataSource();
    SimulationParameters simulationParameters = new SimulationParameters(Calendar.getInstance(), 35, 5, 5);
    mSimulatedLocationDataSource.setLocations(routeGeometry, simulationParameters);

    // set up a RouteTracker for navigation along the calculated route
    mRouteTracker = new RouteTracker(getApplicationContext(), routeResult, 0, true);
    ReroutingParameters reroutingParameters = new ReroutingParameters(routeTask, routeParameters);
    mRouteTracker.enableReroutingAsync(reroutingParameters);

    // create a route tracker location data source to snap the location display to the route
    RouteTrackerLocationDataSource routeTrackerLocationDataSource = new RouteTrackerLocationDataSource(mRouteTracker, mSimulatedLocationDataSource);
    // set the route tracker location data source as the location data source for this app
    locationDisplay.setLocationDataSource(routeTrackerLocationDataSource);
    locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
    // if the user navigates the map view away from the location display, activate the recenter button
    locationDisplay.addAutoPanModeChangedListener(autoPanModeChangedEvent -> mRecenterButton.setEnabled(true));

    // get a reference to navigation text views
    TextView distanceRemainingTextView = findViewById(R.id.distanceRemainingTextView);
    TextView timeRemainingTextView = findViewById(R.id.timeRemainingTextView);
    TextView nextDirectionTextView = findViewById(R.id.nextDirectionTextView);

    // listen for changes in location
    locationDisplay.addLocationChangedListener(locationChangedEvent -> {
        // listen for new voice guidance events
        mRouteTracker.addNewVoiceGuidanceListener(newVoiceGuidanceEvent -> {
          // use Android's text to speech to speak the voice guidance
          speakVoiceGuidance(newVoiceGuidanceEvent.getVoiceGuidance().getText());
          nextDirectionTextView
              .setText(getString(R.string.next_direction, newVoiceGuidanceEvent.getVoiceGuidance().getText()));
        });

        // get the route's tracking status
        TrackingStatus trackingStatus = mRouteTracker.getTrackingStatus();
        // set geometries for the route ahead and the remaining route
        mRouteAheadGraphic.setGeometry(trackingStatus.getRouteProgress().getRemainingGeometry());
        mRouteTraveledGraphic.setGeometry(trackingStatus.getRouteProgress().getTraversedGeometry());

        // get remaining distance information
        TrackingStatus.Distance remainingDistance = trackingStatus.getDestinationProgress().getRemainingDistance();
        // covert remaining minutes to hours:minutes:seconds
        String remainingTimeString = DateUtils
            .formatElapsedTime((long) (trackingStatus.getDestinationProgress().getRemainingTime() * 60));

        // update text views
        distanceRemainingTextView.setText(getString(R.string.distance_remaining, remainingDistance.getDisplayText(),
            remainingDistance.getDisplayTextUnits().getPluralDisplayName()));
        timeRemainingTextView.setText(getString(R.string.time_remaining, remainingTimeString));

        // if a destination has been reached
        if (trackingStatus.getDestinationStatus() == DestinationStatus.REACHED) {
          // if there are more destinations to visit. Greater than 1 because the start point is considered a "stop"
          if (mRouteTracker.getTrackingStatus().getRemainingDestinationCount() > 1) {
            // switch to the next destination
            mRouteTracker.switchToNextDestinationAsync();
            Toast.makeText(this, "Navigating to the second stop, the Fleet Science Center.", Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(this, "Arrived at the final destination.", Toast.LENGTH_LONG).show();
          }
        }
    });

    // start the LocationDisplay, which starts the RouteTrackerLocationDataSource and SimulatedLocationDataSource
    locationDisplay.startAsync();
    Toast.makeText(this, "Navigating to the first stop, the USS San Diego Memorial.", Toast.LENGTH_LONG).show();
  }

  /**
   * Uses Android's text to speak to say the latest voice guidance from the RouteTracker out loud.
   */
  private void speakVoiceGuidance(String voiceGuidanceText) {
    if (mIsTextToSpeechInitialized && !mTextToSpeech.isSpeaking()) {
        mTextToSpeech.speak(voiceGuidanceText, TextToSpeech.QUEUE_FLUSH, null);
    }
  }

  /**
   * Creates a list of stops along a route.
   */
  private static List<Stop> getStops() {
    List<Stop> stops = new ArrayList<>(3);
    // San Diego Convention Center
    Stop conventionCenter = new Stop(new Point(-117.160386, 32.706608, SpatialReferences.getWgs84()));
    stops.add(conventionCenter);
    // USS San Diego Memorial
    Stop memorial = new Stop(new Point(-117.173034, 32.712327, SpatialReferences.getWgs84()));
    stops.add(memorial);
    // RH Fleet Aerospace Museum
    Stop aerospaceMuseum = new Stop(new Point(-117.147230, 32.730467, SpatialReferences.getWgs84()));
    stops.add(aerospaceMuseum);
    return stops;
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
