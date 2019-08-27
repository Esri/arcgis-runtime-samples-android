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
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.navigation.RouteTracker;
import com.esri.arcgisruntime.navigation.VoiceGuidance;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  private TextToSpeech mTextToSpeech;
  private boolean mIsTextToSpeechInitialized = false;
  private RouteTracker mRouteTracker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    requestLocationPermission();

  }

  private void startNavigation() {
    // initialize text-to-speech to replay navigation voice guidance
    mTextToSpeech = new TextToSpeech(this, status -> {
      if (status != TextToSpeech.ERROR) {
        mTextToSpeech.setLanguage(Locale.UK);
        mIsTextToSpeechInitialized = true;
      }
    });

    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(Basemap.createLightGrayCanvas());
    mMapView.setMap(map);

    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // generate a route with directions and stops for navigation
    RouteTask routeTask = new RouteTask(this,
        "http://sampleserver6.arcgisonline.com/arcgis/rest/services/NetworkAnalysis/SanDiego/NAServer/Route");
    ListenableFuture<RouteParameters> routeParametersFuture = routeTask.createDefaultParametersAsync();
    routeParametersFuture.addDoneListener(() -> {

      try {
        RouteParameters routeParameters = routeParametersFuture.get();

        routeParameters.setStops(getStops());
        routeParameters.setReturnDirections(true);
        routeParameters.setReturnStops(true);
        ListenableFuture<RouteResult> routeResultFuture = routeTask.solveRouteAsync(routeParameters);
        routeParametersFuture.addDoneListener(() -> {

          try {
            RouteResult routeResult = routeResultFuture.get();

            Polyline routeGeometry = routeResult.getRoutes().get(0).getRouteGeometry();

            // show the route as a graphic in the map
            Graphic routeGraphic = new Graphic(routeGeometry,
                new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f));
            mMapView.getGraphicsOverlays().get(0).getGraphics().add(routeGraphic);
            mMapView.setViewpointGeometryAsync(routeGeometry.getExtent());

            LocationDisplay locationDisplay = mMapView.getLocationDisplay();

            // set up a RouteLocationDataSource which simulates movement along the route
            locationDisplay.setLocationDataSource(new RouteLocationDataSource(routeGeometry, MainActivity.this));
            locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);

            // set up a RouteTracker for navigation along the calculated route
            mRouteTracker = new RouteTracker(getApplicationContext(), routeResult, 0);
            mRouteTracker.enableReroutingAsync(routeTask, routeParameters, RouteTracker.ReroutingStrategy.TO_NEXT_WAYPOINT, true);



            // Listen to LocationChanged events. On each location change track the location and replay the voice guidance
            locationDisplay.addLocationChangedListener(locationChangedEvent ->  {



                  mRouteTracker.trackLocationAsync(locationDisplay.getLocation()).addDoneListener(() -> {
                   speakNavigationInstructions();
                  });
                });

            // start the LocationDisplay, which will start the RouteLocationDataSource, which starts simulating movement
            // along the route
            locationDisplay.startAsync();

            locationDisplay.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
              @Override public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
                Log.d(TAG, "Location display: " + mMapView.getLocationDisplay().getLocation().getPosition().getX() + ", " + mMapView.getLocationDisplay().getLocation().getPosition().getY());
              }
            });

          } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
          }
        });
      } catch (ExecutionException | InterruptedException e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * Uses [TextToSpeech] to "speak" the latest voice guidance from the RouteTracker.
   *
   * @since 100.6.0
   */
  private void speakNavigationInstructions() {
    if (mIsTextToSpeechInitialized && !mTextToSpeech.isSpeaking()) {

      VoiceGuidance voiceGuidance = mRouteTracker.generateVoiceGuidance();
      if (voiceGuidance != null) {
        Log.d(TAG, voiceGuidance.getText());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          mTextToSpeech.speak(voiceGuidance.getText(), TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
          mTextToSpeech.speak(voiceGuidance.getText(), TextToSpeech.QUEUE_FLUSH, null);
        }
      }
    }
  }

  /**
   * Creates a list of stops along a route.
   *
   * @since 100.6.0
   */
  private List<Stop> getStops() {
    List<Stop> stops = new ArrayList<>(2);

    Stop oldPointLomaLighthouse = new Stop(new Point(-13051402.823238, 3852286.346837, SpatialReference.create(3857)));
    Stop plazaDePanama = new Stop(new Point(-13041351.177987,3859676.882442, SpatialReference.create(3857)));

    stops.add(oldPointLomaLighthouse);
    stops.add(plazaDePanama);

    return stops;
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestLocationPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.ACCESS_FINE_LOCATION };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      // do something
      startNavigation();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      // do something
      startNavigation();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  /**
   * A LocationDataSource that simulates movement along the specified route. Upon start of the RouteLocationDataSource,
   * a timer is started, which updates the location along the route at fixed intervals.
   *
   * @since 100.6.0
   */
  class RouteLocationDataSource extends LocationDataSource {

    private Context mContext;

    private Point mCurrentLocation;
    private Polyline mRoute;

    private Timer mTimer;

    private double distance = 0.0;
    private double distanceInterval = .001;

    RouteLocationDataSource(Polyline route, Context context) {
      mContext = context;
      mRoute = route;
      onStart();
    }

    @Override
    protected void onStop() {
      mTimer.cancel();
    }

    @Override
    protected void onStart() {
      Handler handler = new Handler(mContext.getMainLooper());
      handler.post(() -> {

        if (!mMapView.getLocationDisplay().isStarted()) {
          mMapView.getLocationDisplay().startAsync();
          Log.d(TAG, "Starting in loop");
        } else {
          Log.d(TAG, "Location display not started");
        }

        // start at the beginning of the route
        mCurrentLocation = mRoute.getParts().get(0).getStartPoint();
        updateLocation(
            new LocationDataSource.Location(mCurrentLocation));

        SimpleMarkerSymbol simpleMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10);
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay(GraphicsOverlay.RenderingMode.DYNAMIC);
        mMapView.getGraphicsOverlays().add(graphicsOverlay);

        mTimer = new Timer("RouteLocationDataSource Timer", false);
        mTimer.scheduleAtFixedRate(new TimerTask() {
          @Override public void run() {
            // update current location by moving [distanceInterval] meters along the route
            mCurrentLocation = GeometryEngine.createPointAlong(mRoute, distance);
            updateLocation(new Location(mCurrentLocation));

            Graphic graphic = new Graphic(mCurrentLocation, simpleMarkerSymbol);
            graphicsOverlay.getGraphics().clear();
            graphicsOverlay.getGraphics().add(graphic);

            distance += distanceInterval;

            // stop the LocationDataSource at the end of the route so no more location updates will occur,
            // which will stop the voice guidance instructions.
            if (GeometryEngine.createPointAlong(mRoute, distance).equals(mRoute.getParts().get(0).getEndPoint())) {
              stop();
            }
          }
        }, 0, 1000);
      });
    }
  }
}
