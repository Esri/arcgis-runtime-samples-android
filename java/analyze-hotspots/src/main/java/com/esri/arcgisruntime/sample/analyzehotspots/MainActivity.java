/* Copyright 2017 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.analyzehotspots;

import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingJob;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingParameters;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingResult;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingString;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingTask;

public class MainActivity extends AppCompatActivity implements DateRangeDialogFragment.OnAnalyzeButtonClickListener,
    ProgressDialogFragment.OnProgressDialogCancelListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  // string used to query input for GeoprocessingString
  private static final String QUERY_INPUT_STRING = "(\"DATE\" > date '%1$s 00:00:00' AND \"DATE\" < date '%2$s 00:00:00')";

  private MapView mMapView;

  private GeoprocessingTask mGeoprocessingTask;

  private GeoprocessingJob mGeoprocessingJob;

  private boolean mCancelled;

  private ArcGISMapImageLayer mHotspotMapImageLayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());

    // center for initial viewpoint with Web Mercator Spatial Reference
    Point center = new Point(-13671170, 5693633, SpatialReferences.getWebMercator());

    // set initial viewpoint
    map.setInitialViewpoint(new Viewpoint(center, 57779));

    // set the map to the map view
    mMapView.setMap(map);

    // setup OnClickListener for FloatingActionButton
    FloatingActionButton calendarFab = findViewById(R.id.calendarButton);
    calendarFab.setOnClickListener(v -> showDateRangeDialog());

    // initialize geoprocessing task with the url of the service
    mGeoprocessingTask = new GeoprocessingTask(getString(R.string.hotspot_911_calls_url));
    mGeoprocessingTask.loadAsync();

    showDateRangeDialog();
  }

  @Override public void onAnalyzeButtonClick(String fromDate, String toDate) {
    analyzeHotspots(fromDate, toDate);
  }

  /**
   * Runs the geoprocessing job, updating progress while loading. On job done, loads the resulting
   * ArcGISMapImageLayer to the map and resets the Viewpoint of the MapView.
   *
   * @param from string which holds a date
   * @param to   string which holds a date
   */
  private void analyzeHotspots(final String from, final String to) {
    // cancel previous job request
    if (mGeoprocessingJob != null) {
      mGeoprocessingJob.cancel();
    }

    // a map image layer is generated as a result. Remove any layer previously added to the map
    mMapView.getMap().getOperationalLayers().remove(mHotspotMapImageLayer);

    // set canceled flag to false
    mCancelled = false;

    // parameters
    final ListenableFuture<GeoprocessingParameters> paramsFuture = mGeoprocessingTask.createDefaultParametersAsync();
    paramsFuture.addDoneListener(() -> {
      try {
        GeoprocessingParameters geoprocessingParameters = paramsFuture.get();
        geoprocessingParameters.setProcessSpatialReference(mMapView.getSpatialReference());
        geoprocessingParameters.setOutputSpatialReference(mMapView.getSpatialReference());

        String queryString = String.format(QUERY_INPUT_STRING, from, to);
        geoprocessingParameters.getInputs().put("Query", new GeoprocessingString(queryString));
        Log.i(TAG, "Query: " + queryString);

        // create job
        mGeoprocessingJob = mGeoprocessingTask.createJob(geoprocessingParameters);

        // start job
        mGeoprocessingJob.start();

        showProgressDialogFragment();

        // update progress
        mGeoprocessingJob.addProgressChangedListener(() -> {
          if (findProgressDialogFragment() != null) {
            findProgressDialogFragment().setProgress(mGeoprocessingJob.getProgress());
          }
        });

        // listen for job completion
        mGeoprocessingJob.addJobDoneListener(() -> {
          if (mGeoprocessingJob.getStatus() == Job.Status.SUCCEEDED) {
            Log.i(TAG, "Job succeeded.");

            GeoprocessingResult geoprocessingResult = mGeoprocessingJob.getResult();
            mHotspotMapImageLayer = geoprocessingResult.getMapImageLayer();

            addLayerToMapView(mHotspotMapImageLayer, mMapView);

          } else if (mCancelled) {
            Toast.makeText(MainActivity.this, "Job canceled.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Job cancelled.");
          } else {
            String error = mGeoprocessingJob.getError().getMessage();
            Toast.makeText(MainActivity.this, "Job did not succeed! " + error, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Job did not succeed! " + error);
          }
          if (findProgressDialogFragment() != null) {
            findProgressDialogFragment().dismiss();
          }
        });
      } catch (InterruptedException | ExecutionException e) {
        String error = e.getMessage();
        Toast.makeText(MainActivity.this, "Geoprocessing job failed " + error, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Geoprocessing job failed " + error);
      }
    });
  }

  /**
   * Add a {@link Layer} to a {@link MapView} by getting the {@link ArcGISMap} from the {@link MapView}. We then check
   * the {@link LoadStatus} to ensure it's loaded and set the {@link Viewpoint} of the {@link MapView} to the full
   * extent of the {@link Layer}
   *
   * @param layer   to display on a {@link MapView}
   * @param mapView to display the {@link Layer} on
   */
  private void addLayerToMapView(final Layer layer, final MapView mapView) {
    mapView.getMap().getOperationalLayers().add(layer);

    if (layer.getLoadStatus() != LoadStatus.LOADED) {
      layer.addDoneLoadingListener(() -> {
        mMapView.setViewpointGeometryAsync(layer.getFullExtent());
      });
    }
  }

  private void cancelGeoProcessingJob() {
    mCancelled = true;
    if (mGeoprocessingJob != null) {
      mGeoprocessingJob.cancel();
    }
  }

  private DateRangeDialogFragment findDateRangeDialogFragment() {
    return (DateRangeDialogFragment) getSupportFragmentManager()
        .findFragmentByTag(DateRangeDialogFragment.class.getSimpleName());
  }

  private ProgressDialogFragment findProgressDialogFragment() {
    return (ProgressDialogFragment) getSupportFragmentManager()
        .findFragmentByTag(ProgressDialogFragment.class.getSimpleName());
  }

  private void showDateRangeDialog() {
    if (findDateRangeDialogFragment() == null) {
      DateRangeDialogFragment dateRangeDialogFragment = DateRangeDialogFragment.newInstance(
          getString(R.string.date_range_dialog_title),
          getString(R.string.date_range_dialog_submit_button_text)
      );
      dateRangeDialogFragment.show(getSupportFragmentManager(), DateRangeDialogFragment.class.getSimpleName());
    }
  }

  private void showProgressDialogFragment() {
    if (findProgressDialogFragment() == null) {
      ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(
          getString(R.string.progress_dialog_fragment_title),
          getString(R.string.progress_dialog_fragment_cancel_text)
      );
      progressDialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.class.getSimpleName());
    }
  }

  @Override public void onProgressDialogCancel() {
    cancelGeoProcessingJob();
  }

  @Override
  public void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  public void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  public void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}