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

package com.esri.arcgisruntime.sample.findclosestfacilitytomultipleincidentsservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityResult;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityRoute;
import com.esri.arcgisruntime.tasks.networkanalysis.ClosestFacilityTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Facility;
import com.esri.arcgisruntime.tasks.networkanalysis.Incident;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private ClosestFacilityTask mClosestFacilityTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get a reference to the solve routes button and disable it
    Button solveRoutesButton = findViewById(R.id.solveRoutesButton);
    solveRoutesButton.setEnabled(false);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // create a ArcGISMap with a Basemap instance with an Imagery base layer
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_STREETS_RELIEF);
    mMapView.setMap(map);

    // create a graphics overlay and add it to the map
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // create Symbols for displaying facilities
    PictureMarkerSymbol facilitySymbol = new PictureMarkerSymbol(getString(R.string.fire_station_symbol_url));
    facilitySymbol.setHeight(30);
    facilitySymbol.setWidth(30);
    PictureMarkerSymbol incidentSymbol = new PictureMarkerSymbol(getString(R.string.crime_symbol_url));
    incidentSymbol.setHeight(30);
    incidentSymbol.setWidth(30);

    // create a line symbol to mark the route
    SimpleLineSymbol simpleLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5.0f);

    // create a closest facility task from a network analysis service
    mClosestFacilityTask = new ClosestFacilityTask(this,
        getString(R.string.san_diego_network_analysis_service_url));

    // create a table for facilities using the feature service
    FeatureTable facilitiesFeatureTable = new ServiceFeatureTable(getString(R.string.san_diego_facilities_service_url));
    // create a feature layer from the table, apply facilities icon
    FeatureLayer facilitiesFeatureLayer = new FeatureLayer(facilitiesFeatureTable);
    facilitiesFeatureLayer.setRenderer(new SimpleRenderer(facilitySymbol));
    // create a table for incidents using the feature service
    FeatureTable incidentsFeatureTable = new ServiceFeatureTable(getString(R.string.san_diego_incidents_service_url));
    // create a feature layer from the table, apply incident icon
    FeatureLayer incidentsFeatureLayer = new FeatureLayer(incidentsFeatureTable);
    incidentsFeatureLayer.setRenderer(new SimpleRenderer(incidentSymbol));

    // add the layers to the map
    map.getOperationalLayers().addAll(Arrays.asList(facilitiesFeatureLayer, incidentsFeatureLayer));

    // create the list to store the facilities
    List<Facility> facilities = new ArrayList<>();
    // create the list to store the incidents
    List<Incident> incidents = new ArrayList<>();

    // wait for the feature layers to load to retrieve the facilities and incidents
    facilitiesFeatureLayer.addDoneLoadingListener(() -> incidentsFeatureLayer.addDoneLoadingListener(() -> {
          if (facilitiesFeatureLayer.getLoadStatus() == LoadStatus.LOADED
              && incidentsFeatureLayer.getLoadStatus() == LoadStatus.LOADED) {
            // zoom to the extent of the combined feature layers
            Envelope fullFeatureLayerExtent = GeometryEngine
                .combineExtents(facilitiesFeatureLayer.getFullExtent(), incidentsFeatureLayer.getFullExtent());
            mMapView.setViewpointGeometryAsync(fullFeatureLayerExtent, 90);
            // create query parameters to select all features
            QueryParameters queryParameters = new QueryParameters();
            queryParameters.setWhereClause("1=1");
            // retrieve a list of all facilities
            ListenableFuture<FeatureQueryResult> result = facilitiesFeatureTable.queryFeaturesAsync(queryParameters);
            result.addDoneListener(() -> {
              try {
                FeatureQueryResult facilitiesResult = result.get();
                // add the found facilities to the list
                for (Feature facilityFeature : facilitiesResult) {
                  // since we know our feature layer only contains point features, we can cast them as Point in order to create a Facility
                  facilities.add(new Facility((Point) facilityFeature.getGeometry()));
                }
              } catch (InterruptedException | ExecutionException e) {
                String error = "Error retrieving list of facilities: " + e.getMessage();
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                Log.e(TAG, error);
              }
            });
            // retrieve a list of all incidents
            ListenableFuture<FeatureQueryResult> incidentsQueryResult = incidentsFeatureTable
                .queryFeaturesAsync(queryParameters);
            incidentsQueryResult.addDoneListener(() -> {
              try {
                FeatureQueryResult incidentsResult = incidentsQueryResult.get();
                // add the found incidents to the list
                for (Feature incidentFeature : incidentsResult) {
                  // since we know our feature layer only contains point features, we can cast them as Point in order to create an Incident
                  incidents.add(new Incident((Point) incidentFeature.getGeometry()));
                }
              } catch (InterruptedException | ExecutionException e) {
                String error = "Error retrieving list of incidents: " + e.getMessage();
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                Log.e(TAG, error);
              }
            });
            // enable the 'solve routes' button
            solveRoutesButton.setEnabled(true);
            // resolve button press
            solveRoutesButton.setOnClickListener(v -> {
              // disable the 'solve routes' button and show the progress indicator
              solveRoutesButton.setEnabled(false);
              // start the routing task
              mClosestFacilityTask.loadAsync();
              mClosestFacilityTask.addDoneLoadingListener(() -> {
                if (mClosestFacilityTask.getLoadStatus() == LoadStatus.LOADED) {
                  try {
                    // create default parameters for the task and add facilities and incidents to parameters
                    ClosestFacilityParameters closestFacilityParameters = mClosestFacilityTask
                        .createDefaultParametersAsync().get();
                    closestFacilityParameters.setFacilities(facilities);
                    closestFacilityParameters.setIncidents(incidents);
                    // solve closest facilities
                    try {
                      // use the task to solve for the closest facility
                      ListenableFuture<ClosestFacilityResult> closestFacilityTaskResult = mClosestFacilityTask
                          .solveClosestFacilityAsync(closestFacilityParameters);
                      closestFacilityTaskResult.addDoneListener(() -> {
                        try {
                          ClosestFacilityResult closestFacilityResult = closestFacilityTaskResult.get();
                          // find the closest facility for each incident
                          for (int i = 0; i < incidents.size(); i++) {
                            // get the index of the closest facility to incident
                            Integer closestFacilityIndex = closestFacilityResult.getRankedFacilityIndexes(i).get(0);
                            // get the route to the closest facility
                            ClosestFacilityRoute closestFacilityRoute = closestFacilityResult
                                .getRoute(closestFacilityIndex, i);
                            // display the route on the graphics overlay
                            graphicsOverlay.getGraphics()
                                .add(new Graphic(closestFacilityRoute.getRouteGeometry(), simpleLineSymbol));
                          }
                        } catch (ExecutionException | InterruptedException ex) {
                          String error = "Error getting the closest facility task result: " + ex.getMessage();
                          Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                          Log.e(TAG, error);
                        }
                      });
                    } catch (Exception ex) {
                      String error = "Error solving the closest facility task: " + ex.getMessage();
                      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                      Log.e(TAG, error);
                    }
                  } catch (InterruptedException | ExecutionException ex) {
                    String error = "Error getting default route parameters: " + ex.getMessage();
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, error);
                  }
                } else {
                  String error = "Error loading route task: " + mClosestFacilityTask.getLoadError().getMessage();
                  Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                  Log.e(TAG, error);
                }
              });
            });
          }
        })
    );
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
