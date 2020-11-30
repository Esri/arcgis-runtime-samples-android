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

import java.util.List;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity implements ConfirmDeleteFeatureDialog.OnButtonClickedListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  private ServiceFeatureTable mFeatureTable;

  private FeatureLayer mFeatureLayer;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    mMapView = findViewById(R.id.mapView);

    // create a map with streets basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_STREETS);
    map.setInitialViewpoint(new Viewpoint( 40, -95, 100000000));

    // create service feature table from URL
    mFeatureTable = new ServiceFeatureTable(getString(R.string.feature_layer_url));

    // create a feature layer from table
    mFeatureLayer = new FeatureLayer(mFeatureTable);

    // add the layer to the map
    map.getOperationalLayers().add(mFeatureLayer);

    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override public boolean onSingleTapConfirmed(MotionEvent event) {
        // create a point from where the user clicked
        android.graphics.Point screenPoint = new android.graphics.Point((int) event.getX(), (int) event.getY());

        // create a map point from a screen point
        Point mapPoint = mMapView.screenToLocation(screenPoint);

        // identify the clicked feature
        ListenableFuture<IdentifyLayerResult> identifyLayerFuture = mMapView
            .identifyLayerAsync(mFeatureLayer, screenPoint, 1, false);
        identifyLayerFuture.addDoneListener(() -> {
          try {
            IdentifyLayerResult layer = identifyLayerFuture.get();
            // get first element found and ensure that it is an instance of Feature before allowing user to delete
            // using callout
            if (!layer.getElements().isEmpty() && layer.getElements().get(0) instanceof Feature) {
              inflateCallout(mMapView, layer.getElements().get(0), mapPoint).show();
            }
          } catch (InterruptedException | ExecutionException e) {
            logToUser(true, getString(R.string.error_getting_identify_result, e.getCause().getMessage()));
          }
        });
        return super.onSingleTapConfirmed(event);
      }
    });

    // set map to be displayed in map view
    mMapView.setMap(map);
  }

  /**
   * Method gets an instance of {@link Callout} from a {@link MapView} and inflates a {@link View} from a layout
   * to display as the content of the {@link Callout}.
   *
   * @param mapView instance of {@link MapView} where the {@link Callout} is to be displayed
   * @param feature used to set the {@link GeoElement} of the {@link Callout}
   * @param point   the location of the user's tap
   * @return a {@link Callout} to display on a {@link MapView}
   */
  private Callout inflateCallout(MapView mapView, GeoElement feature, Point point) {
    Callout callout = mapView.getCallout();
    View view = LayoutInflater.from(this).inflate(R.layout.view_callout, null);
    view.findViewById(R.id.calloutViewCallToAction).setOnClickListener(v -> {
      confirmDeletion(feature.getAttributes().get("objectid").toString());
      callout.dismiss();
    });
    callout.setContent(view);
    callout.setGeoElement(feature, point);
    return callout;
  }

  /**
   * Method displays instance of {@link ConfirmDeleteFeatureDialog} to allow user to confirm their intent to delete
   * a {@link Feature}.
   *
   * @param featureId id of feature to be deleted
   */
  private void confirmDeletion(String featureId) {
    ConfirmDeleteFeatureDialog.newInstance(featureId)
        .show(getSupportFragmentManager(), ConfirmDeleteFeatureDialog.class.getSimpleName());
  }

  /**
   * Callback from {@link ConfirmDeleteFeatureDialog}, invoked when positive button has been clicked in dialog.
   *
   * @param featureId id of feature to be deleted
   */
  @Override public void onDeleteFeatureClicked(String featureId) {
    // query feature layer to find element by id
    QueryParameters queryParameters = new QueryParameters();
    queryParameters.setWhereClause(String.format("OBJECTID = %s", featureId));

    ListenableFuture<FeatureQueryResult> featureQueryResult = mFeatureLayer.getFeatureTable()
        .queryFeaturesAsync(queryParameters);
    featureQueryResult.addDoneListener(() -> {
      try {
        // check result has a feature
        if (featureQueryResult.get().iterator().hasNext()) {
          // attempt to get first feature from result as it should be the only feature
          Feature foundFeature = featureQueryResult.get().iterator().next();
          // delete found features
          deleteFeature(foundFeature, mFeatureTable, () -> applyEdits(mFeatureTable));
        }
      } catch (InterruptedException | ExecutionException e) {
        logToUser(true, getString(R.string.error_feature_deletion, e.getCause().getMessage()));
      }
    });
  }

  /**
   * Deletes a feature from a {@link ServiceFeatureTable} and applies the changes to the
   * server.
   *
   * @param feature                     {@link Feature} to delete
   * @param featureTable                {@link ServiceFeatureTable} to delete {@link Feature} from
   * @param onDeleteFeatureDoneListener {@link Runnable} to be invoked when action has completed
   */
  private void deleteFeature(Feature feature, ServiceFeatureTable featureTable,
      Runnable onDeleteFeatureDoneListener) {
    featureTable.deleteFeatureAsync(feature).addDoneListener(onDeleteFeatureDoneListener);
  }

  /**
   * Sends any edits on the {@link ServiceFeatureTable} to the server.
   *
   * @param featureTable {@link ServiceFeatureTable} to apply edits to
   */
  private void applyEdits(ServiceFeatureTable featureTable) {
    // apply the changes to the server
    ListenableFuture<List<FeatureEditResult>> featureEditsFuture = featureTable.applyEditsAsync();
    featureEditsFuture.addDoneListener(() -> {
      try {
        // check result has an edit
        if (featureEditsFuture.get().iterator().hasNext()) {
          // attempt to get first edit from result as it should be the only edit
          FeatureEditResult edit = featureEditsFuture.get().iterator().next();
          // check if the server edit was successful
          if (!edit.hasCompletedWithErrors()) {
            logToUser(false, getString(R.string.success_feature_deleted));
          } else {
            throw edit.getError();
          }
        }
      } catch (InterruptedException | ExecutionException e) {
        logToUser(true, getString(R.string.error_applying_edits, e.getCause().getMessage()));
      }
    });
  }

  /**
   * Shows a Toast to user and logs to logcat.
   *
   * @param message to display to user and log to LogCat
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
