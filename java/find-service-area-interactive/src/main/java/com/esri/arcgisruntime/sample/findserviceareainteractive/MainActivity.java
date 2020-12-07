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

package com.esri.arcgisruntime.sample.findserviceareainteractive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.PolylineBarrier;
import com.esri.arcgisruntime.tasks.networkanalysis.ServiceAreaFacility;
import com.esri.arcgisruntime.tasks.networkanalysis.ServiceAreaParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.ServiceAreaPolygon;
import com.esri.arcgisruntime.tasks.networkanalysis.ServiceAreaPolygonDetail;
import com.esri.arcgisruntime.tasks.networkanalysis.ServiceAreaResult;
import com.esri.arcgisruntime.tasks.networkanalysis.ServiceAreaTask;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private ServiceAreaParameters mServiceAreaParameters;
  private PolylineBuilder mBarrierBuilder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // create a map with a streets base map
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_STREETS);
    mMapView.setMap(map);

    // set the view point to San Diego, where the service task area is
    mMapView.setViewpoint(new Viewpoint(32.73, -117.14, 75000));

    // create service area task from url
    ServiceAreaTask serviceAreaTask = new ServiceAreaTask(this, getString(R.string.san_diego_service_area));
    serviceAreaTask.loadAsync();
    // create default parameters from task
    ListenableFuture<ServiceAreaParameters> serviceAreaParametersFuture = serviceAreaTask
        .createDefaultParametersAsync();
    serviceAreaParametersFuture.addDoneListener(() -> {
      try {
        mServiceAreaParameters = serviceAreaParametersFuture.get();
        mServiceAreaParameters.setPolygonDetail(ServiceAreaPolygonDetail.HIGH);
        mServiceAreaParameters.setReturnPolygons(true);
        // adding another service area of 2 minutes
        // default parameters have a default service area of 5 minutes
        mServiceAreaParameters.getDefaultImpedanceCutoffs().addAll(Collections.singletonList(2.0));
      } catch (ExecutionException | InterruptedException e) {
        String error = "Error creating service area parameters: " + e;
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });

    // create graphics overlays to show facilities, barriers and service areas
    GraphicsOverlay serviceAreasOverlay = new GraphicsOverlay();
    GraphicsOverlay facilityOverlay = new GraphicsOverlay();
    GraphicsOverlay barrierOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().addAll(Arrays.asList(serviceAreasOverlay, barrierOverlay, facilityOverlay));

    mBarrierBuilder = new PolylineBuilder(mMapView.getSpatialReference());
    List<ServiceAreaFacility> serviceAreaFacilities = new ArrayList<>();

    SimpleLineSymbol barrierLine = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 3.0f);
    ArrayList<SimpleFillSymbol> fillSymbols = new ArrayList<>();
    fillSymbols.add(new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, getResources().getColor(R.color.colorTransparentRed), null));
    fillSymbols.add(new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, getResources().getColor(R.color.colorTransparentOrange), null));

    // icon used to display facilities to map view
    PictureMarkerSymbol facilitySymbol = new PictureMarkerSymbol(getString(R.string.hospital_icon_url));
    facilitySymbol.setHeight(30);
    facilitySymbol.setWidth(30);

    Button addFacilityButton = findViewById(R.id.addFacilityButton);
    Button addBarrierButton = findViewById(R.id.addBarrierButton);
    addFacilityButton.setOnClickListener(v -> {
      addFacilityButton.setSelected(true);
      addBarrierButton.setSelected(false);
    });
    addBarrierButton.setOnClickListener(v -> {
      addBarrierButton.setSelected(true);
      addFacilityButton.setSelected(false);
      mBarrierBuilder = new PolylineBuilder(mMapView.getSpatialReference());
    });

    Button showServiceAreasButton = findViewById(R.id.showServiceAreasButton);
    showServiceAreasButton.setOnClickListener(
        v -> showServiceAreas(serviceAreaFacilities, barrierOverlay, serviceAreasOverlay, serviceAreaTask, fillSymbols,
            addFacilityButton, addBarrierButton));

    Button resetButton = findViewById(R.id.resetButton);
    resetButton.setOnClickListener(
        v -> clearRouteAndGraphics(addFacilityButton, addBarrierButton, serviceAreaFacilities, facilityOverlay, serviceAreasOverlay, barrierOverlay));

    // creates facilities and barriers at user's clicked location
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override public boolean onSingleTapConfirmed(MotionEvent e) {
        // create a point where the user tapped
        android.graphics.Point point = new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY()));
        Point mapPoint = mMapView.screenToLocation(point);
        if (addFacilityButton.isSelected()) {
          // create facility from point and display to map view
          addServicePoint(mapPoint, facilitySymbol, serviceAreaFacilities, facilityOverlay);
        } else if (addBarrierButton.isSelected()) {
          // create barrier and display to map view
          mBarrierBuilder.addPoint(new Point(mapPoint.getX(), mapPoint.getY(), mMapView.getSpatialReference()));
          barrierOverlay.getGraphics()
              .add(barrierOverlay.getGraphics().size(), new Graphic(mBarrierBuilder.toGeometry(), barrierLine));
        }
        return super.onSingleTapConfirmed(e);
      }
    });
  }

  /**
   * Add the given point to the list of service areas and use it to create a facility graphic, which is then added to
   * the facility overlay.
   */
  private void addServicePoint(Point mapPoint, PictureMarkerSymbol facilitySymbol,
      List<ServiceAreaFacility> serviceAreaFacilities, GraphicsOverlay facilityOverlay) {
    Point servicePoint = new Point(mapPoint.getX(), mapPoint.getY(), mMapView.getSpatialReference());
    serviceAreaFacilities.add(new ServiceAreaFacility(servicePoint));
    facilityOverlay.getGraphics().add(new Graphic(servicePoint, facilitySymbol));
  }

  /**
   * Clears all graphics from map view and clears all facilities and barriers from service area parameters.
   */
  private void clearRouteAndGraphics(Button addFacilityButton, Button addBarrierButton,
      List<ServiceAreaFacility> serviceAreaFacilities, GraphicsOverlay facilityOverlay,
      GraphicsOverlay serviceAreasOverlay, GraphicsOverlay barrierOverlay) {
    addFacilityButton.setSelected(false);
    addBarrierButton.setSelected(false);
    mServiceAreaParameters.clearFacilities();
    mServiceAreaParameters.clearPolylineBarriers();
    serviceAreaFacilities.clear();
    facilityOverlay.getGraphics().clear();
    serviceAreasOverlay.getGraphics().clear();
    barrierOverlay.getGraphics().clear();
  }

  /**
   * Solves the service area task using the facilities and barriers that were added to the map view.
   * All service areas that are return will be displayed to the map view.
   */
  private void showServiceAreas(List<ServiceAreaFacility> serviceAreaFacilities, GraphicsOverlay barrierOverlay,
      GraphicsOverlay serviceAreasOverlay, ServiceAreaTask serviceAreaTask, ArrayList<SimpleFillSymbol> fillSymbols,
      Button addFacilityButton, Button addBarrierButton) {

    // need at least one facility for the task to work
    if (!serviceAreaFacilities.isEmpty()) {
      // un-select add facility and add barrier buttons
      addFacilityButton.setSelected(false);
      addBarrierButton.setSelected(false);
      List<PolylineBarrier> polylineBarriers = new ArrayList<>();
      for (Graphic barrierGraphic : barrierOverlay.getGraphics()) {
        polylineBarriers.add(new PolylineBarrier((Polyline) barrierGraphic.getGeometry()));
        mServiceAreaParameters.setPolylineBarriers(polylineBarriers);
      }

      serviceAreasOverlay.getGraphics().clear();
      mServiceAreaParameters.setFacilities(serviceAreaFacilities);
      // find service areas around facility using parameters that were set
      ListenableFuture<ServiceAreaResult> result = serviceAreaTask.solveServiceAreaAsync(mServiceAreaParameters);
      result.addDoneListener(() -> {
        try {
          // display all service areas that were found to mapview
          List<Graphic> graphics = serviceAreasOverlay.getGraphics();
          ServiceAreaResult serviceAreaResult = result.get();
          for (int i = 0; i < serviceAreaFacilities.size(); i++) {
            List<ServiceAreaPolygon> polygons = serviceAreaResult.getResultPolygons(i);
            // could be more than one service area
            for (int j = 0; j < polygons.size(); j++) {
              graphics.add(new Graphic(polygons.get(j).getGeometry(), fillSymbols.get(j % 2)));
            }
          }
        } catch (ExecutionException | InterruptedException e) {
          String error = e.getMessage().contains("Unable to complete operation") ?
              "Facility not within San Diego area!" + e :
              "Error getting the service area result: " + e;
          Log.e(TAG, error);
          Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
      });
    } else {
      Toast.makeText(this, "Must have at least one Facility on the map!", Toast.LENGTH_LONG).show();
    }
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
