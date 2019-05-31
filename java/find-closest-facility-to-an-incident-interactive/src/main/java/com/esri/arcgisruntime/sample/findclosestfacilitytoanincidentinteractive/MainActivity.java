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

package com.esri.arcgisruntime.sample.findclosestfacilitytoanincidentinteractive;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityResult;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityRoute;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Facility;
import com.esri.arcgisruntime.tasks.networkanalysis.Incident;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private GraphicsOverlay mFacilityGraphicsOverlay;
  private GraphicsOverlay mIncidentGraphicsOverlay;
  private ClosestFacilityTask mClosestFacilityTask;
  private ClosestFacilityParameters mClosestFacilityParameters;
  private List<Facility> mFacilities;
  private SimpleLineSymbol mRouteSymbol;
  private Point mIncidentPoint;

  // same spatial reference of the map
  private final SpatialReference spatialReference = SpatialReferences.getWebMercator();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // create a map with a streets basemap centered on San Diego
    ArcGISMap map = new ArcGISMap(Basemap.Type.STREETS, 32.727, -117.1750, 12);

    // add the map to the map view
    mMapView.setMap(map);

    mFacilityGraphicsOverlay = new GraphicsOverlay();
    mIncidentGraphicsOverlay = new GraphicsOverlay();
    createFacilitiesAndGraphics();

    // to load graphics faster, add graphics overlay to view once all graphics are in graphics overlay
    mMapView.getGraphicsOverlays().add(mFacilityGraphicsOverlay);
    mMapView.getGraphicsOverlays().add(mIncidentGraphicsOverlay);

    // task to find the closest route between an incident and a facility
    mClosestFacilityTask = new ClosestFacilityTask(this, getString(R.string.san_diego_network_service_url));
    mClosestFacilityTask.addDoneLoadingListener(() -> {
      if (mClosestFacilityTask.getLoadStatus() == LoadStatus.LOADED) {
        ListenableFuture<ClosestFacilityParameters> closestFacilityParametersFuture = mClosestFacilityTask
            .createDefaultParametersAsync();
        closestFacilityParametersFuture.addDoneListener(() -> {
          try {
            mClosestFacilityParameters = closestFacilityParametersFuture.get();
            // set new parameters to find route
            mClosestFacilityParameters.setFacilities(mFacilities);
          } catch (ExecutionException | InterruptedException e) {
            String error = "Error generating parameters: " + e.getMessage();
            Log.e(TAG, error);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
          }
        });
      } else {
        String error = "Closest facility task failed to load: " + mClosestFacilityTask.getLoadError().getMessage();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });
    mClosestFacilityTask.loadAsync();

    // symbols that display incident(black cross) and route(blue line) to view
    SimpleMarkerSymbol incidentSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, 0xFF000000, 20);
    mRouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0000FF, 2.0f);

    // place incident were user clicks and display route to closest facility
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override public boolean onSingleTapConfirmed(MotionEvent e) {
        // remove last incident
        mIncidentGraphicsOverlay.getGraphics().clear();
        // show incident to the map view
        Point mapPoint = mMapView
            .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
        mIncidentPoint = new Point(mapPoint.getX(), mapPoint.getY(), spatialReference);
        Graphic graphic = new Graphic(mIncidentPoint, incidentSymbol);
        mIncidentGraphicsOverlay.getGraphics().add(graphic);
        populateParametersAndSolveRoute();
        return true;
      }
    });
  }

  /**
   * Creates facilities around the San Diego region. Facilities are created using point geometry which is then used to
   * make graphics for the graphics overlay.
   */
  private void createFacilitiesAndGraphics() {
    // list of known facilities in the San Diego area
    mFacilities = Arrays.asList(
        new Facility(new Point(-1.3042129900625112E7, 3860127.9479775648, spatialReference)),
        new Facility(new Point(-1.3042193400557665E7, 3862448.873041752, spatialReference)),
        new Facility(new Point(-1.3046882875518233E7, 3862704.9896770366, spatialReference)),
        new Facility(new Point(-1.3040539754780494E7, 3862924.5938606677, spatialReference)),
        new Facility(new Point(-1.3042571225655518E7, 3858981.773018156, spatialReference)),
        new Facility(new Point(-1.3039784633928463E7, 3856692.5980474586, spatialReference)),
        new Facility(new Point(-1.3049023883956768E7, 3861993.789732541, spatialReference)));

    // image for displaying facility
    String facilityUrl = getString(R.string.hospital_symbol_url);
    PictureMarkerSymbol facilitySymbol = new PictureMarkerSymbol(facilityUrl);
    facilitySymbol.setHeight(30);
    facilitySymbol.setWidth(30);

    // for each facility, create a graphic and add to graphics overlay
    for (Facility facility : mFacilities) {
      mFacilityGraphicsOverlay.getGraphics().add(new Graphic(facility.getGeometry(), facilitySymbol));
    }
  }

  /**
   * Adds facilities(hospitals) and user's incident(black cross) to closest facility parameters which will be used to
   * display the closest route from the user's incident to its' nearest facility.
   */
  private void populateParametersAndSolveRoute() {
    mClosestFacilityParameters.setIncidents(Collections.singletonList(new Incident(mIncidentPoint)));

    // find closest route using parameters from above
    ListenableFuture<ClosestFacilityResult> closestFacilityResultFuture = mClosestFacilityTask
        .solveClosestFacilityAsync(mClosestFacilityParameters);
    closestFacilityResultFuture.addDoneListener(() -> {
      try {
        ClosestFacilityResult facilityResult = closestFacilityResultFuture.get();
        // a list of closest facilities based on users incident
        List<Integer> rankedList = facilityResult.getRankedFacilityIndexes(0);
        // get the index of the closest facility to incident
        int closestFacility = rankedList.get(0);
        // get route from incident to closest facility and display to map view
        ClosestFacilityRoute route = facilityResult.getRoute(closestFacility, 0);
        mIncidentGraphicsOverlay.getGraphics().add(new Graphic(route.getRouteGeometry(), mRouteSymbol));
      } catch (ExecutionException | InterruptedException e) {
        String error;
        if (e.getMessage().contains("Unable to complete operation")) {
          error = "Incident not within the San Diego Area!";
        } else {
          error = "Error getting closest facility result: " + e.getMessage();
        }
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });
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
