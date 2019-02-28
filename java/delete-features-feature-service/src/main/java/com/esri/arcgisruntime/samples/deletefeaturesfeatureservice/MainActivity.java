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

package com.esri.arcgisruntime.samples.deletefeaturesfeatureservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  private ServiceFeatureTable mFeatureTable;

  private FeatureLayer mFeatureLayer;

  @SuppressLint("ClickableViewAccessibility")
  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = findViewById(R.id.mapView);

    // create a map with streets basemap
    ArcGISMap map = new ArcGISMap(Basemap.Type.STREETS, 40, -95, 4);

    // create service feature table from URL
    mFeatureTable = new ServiceFeatureTable(getString(R.string.feature_layer_url));

    // create a feature layer from table
    mFeatureLayer = new FeatureLayer(mFeatureTable);

    // add the layer to the ArcGISMap
    map.getOperationalLayers().add(mFeatureLayer);

    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override public boolean onSingleTapConfirmed(MotionEvent event) {
        // create a point from where the user clicked
        android.graphics.Point point = new android.graphics.Point((int) event.getX(), (int) event.getY());

        // create a map point from a point
        Point mapPoint = mMapView.screenToLocation(point);

        // for a wrapped around map, the point coordinates include the wrapped around value
        // for a service in projected coordinate system, this wrapped around value has to be normalized
        Point normalizedMapPoint = (Point) GeometryEngine.normalizeCentralMeridian(mapPoint);

        // identify the clicked feature
        ListenableFuture<IdentifyLayerResult> results = mMapView.identifyLayerAsync(mFeatureLayer, point, 1, false);
        results.addDoneListener(() -> {
          try {
            IdentifyLayerResult layer = results.get();
            // search the layers for identified features
            List<Feature> foundFeatures = new ArrayList<>();

            for (GeoElement element : layer.getElements()) {
              if (element instanceof Feature) {
                foundFeatures.add((Feature) element);
              }
            }

            if (foundFeatures.size() > 0) {
              inflateCallout(mMapView, foundFeatures.get(0), normalizedMapPoint).show();
              mFeatureLayer.selectFeature(foundFeatures.get(0));
            }
          } catch (InterruptedException | ExecutionException e) {
            logToUser(getString(R.string.error_getting_identify_result, e.getCause().getMessage()));
          }
        });
        return super.onSingleTapConfirmed(event);
      }
    });

    // set ArcGISMap to be displayed in map view
    mMapView.setMap(map);
  }

  private Callout inflateCallout(MapView mapView, GeoElement geoElement, Point point) {
    Callout callout = mapView.getCallout();
    View view = LayoutInflater.from(this).inflate(R.layout.view_callout, null);
    view.findViewById(R.id.calloutViewCallToAction).setOnClickListener(v -> {
      // get selected features
      ListenableFuture<FeatureQueryResult> selectionResult = mFeatureLayer.getSelectedFeaturesAsync();
      selectionResult.addDoneListener(() -> {
        try {
          FeatureQueryResult selected = selectionResult.get();
          // delete selected features
          deleteFeatures(selected, mFeatureTable, new Runnable() {
            @Override public void run() {
              applyEdits(mFeatureTable);
              callout.dismiss();
            }
          });
        } catch (InterruptedException | ExecutionException e) {
          logToUser(e.getCause().getMessage());
        }
      });
    });
    callout.setContent(view);
    callout.setGeoElement(geoElement, point);
    return callout;
  }

  /**
   * Deletes features from a ServiceFeatureTable and applies the changes to the
   * server.
   */
  private void deleteFeatures(FeatureQueryResult features, ServiceFeatureTable featureTable,
      Runnable onDeleteFeaturesDoneListener) {
    // delete feature from the feature table and apply edit to server
    featureTable.deleteFeaturesAsync(features).addDoneListener(onDeleteFeaturesDoneListener);
  }

  private void applyEdits(ServiceFeatureTable featureTable) {
    // apply the changes to the server
    ListenableFuture<List<FeatureEditResult>> editResult = featureTable.applyEditsAsync();
    editResult.addDoneListener(() -> {
      try {
        List<FeatureEditResult> edits = editResult.get();
        // check if the server edit was successful
        if (edits != null && edits.size() > 0) {
          if (!edits.get(0).hasCompletedWithErrors()) {
            logToUser("Feature successfully deleted");
          } else {
            throw edits.get(0).getError();
          }
        }
      } catch (InterruptedException | ExecutionException e) {
        logToUser(e.getCause().getMessage());
      }
    });
  }

  /**
   * Shows a Toast to user and logs to logcat.
   *
   * @param message message to display
   */
  private void logToUser(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    Log.d(TAG, message);
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