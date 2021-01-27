/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.addfeaturesfeatureservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  private ServiceFeatureTable mServiceFeatureTable;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    mMapView = findViewById(R.id.mapView);

    // create a map with streets basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_STREETS);

    // create service feature table from URL
    mServiceFeatureTable = new ServiceFeatureTable(getString(R.string.service_layer_url));

    // create a feature layer from table
    FeatureLayer featureLayer = new FeatureLayer(mServiceFeatureTable);

    // add the layer to the map
    map.getOperationalLayers().add(featureLayer);

    // add a listener to the MapView to detect when a user has performed a single tap to add a new feature to
    // the service feature table
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override public boolean onSingleTapConfirmed(MotionEvent event) {
        // create a point from where the user clicked
        android.graphics.Point point = new android.graphics.Point((int) event.getX(), (int) event.getY());

        // create a map point from a point
        Point mapPoint = mMapView.screenToLocation(point);

        // add a new feature to the service feature table
        addFeature(mapPoint, mServiceFeatureTable);
        return super.onSingleTapConfirmed(event);
      }
    });

    // set map to be displayed in map view
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint( 40.0, -95.0, 10000000.0));
  }

  /**
   * Adds a new Feature to a ServiceFeatureTable and applies the changes to the
   * server.
   *
   * @param mapPoint     location to add feature
   * @param featureTable service feature table to add feature
   */
  private void addFeature(Point mapPoint, final ServiceFeatureTable featureTable) {

    // create default attributes for the feature
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("typdamage", "Destroyed");
    attributes.put("primcause", "Earthquake");

    // creates a new feature using default attributes and point
    Feature feature = featureTable.createFeature(attributes, mapPoint);

    // check if feature can be added to feature table
    if (featureTable.canAdd()) {
      // add the new feature to the feature table and to server
      featureTable.addFeatureAsync(feature).addDoneListener(() -> applyEdits(featureTable));
    } else {
      runOnUiThread(() -> logToUser(true, getString(R.string.error_cannot_add_to_feature_table)));
    }
  }

  /**
   * Sends any edits on the ServiceFeatureTable to the server.
   *
   * @param featureTable service feature table
   */
  private void applyEdits(ServiceFeatureTable featureTable) {

    // apply the changes to the server
    final ListenableFuture<List<FeatureEditResult>> editResult = featureTable.applyEditsAsync();
    editResult.addDoneListener(() -> {
      try {
        List<FeatureEditResult> editResults = editResult.get();
        // check if the server edit was successful
        if (editResults != null && !editResults.isEmpty()) {
          if (!editResults.get(0).hasCompletedWithErrors()) {
            runOnUiThread(() -> logToUser(false, getString(R.string.feature_added)));
          } else {
            throw editResults.get(0).getError();
          }
        }
      } catch (InterruptedException | ExecutionException e) {
        runOnUiThread(() -> logToUser(true, getString(R.string.error_applying_edits, e.getCause().getMessage())));
      }
    });
  }

  /**
   * Shows a Toast to user and logs to logcat.
   *
   * @param isError whether message is an error. Determines log level.
   * @param message message to display
   */
  private void logToUser(boolean isError, String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    if (isError) {
      Log.e(TAG, message);
    } else {
      Log.d(TAG, message);
    }
  }

  @Override protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
