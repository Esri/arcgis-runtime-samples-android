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

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.geoprocessing.*;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements DateRangeDialogFragment.OnAnalyzeButtonClickListener,
    ProgressDialogFragment.OnProgressDialogDismissListener {

  private final String TAG = MainActivity.class.getSimpleName();

  // String used to query input for GeoprocessingString
  private static final String QUERY_INPUT_STRING = "(\"DATE\" > date '%1$s 00:00:00' AND \"DATE\" < date '%2$s 00:00:00')";

  private MapView mMapView;

  private GeoprocessingTask mGeoprocessingTask;

  private GeoprocessingJob mGeoprocessingJob;

  private boolean mCancelled;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());

    //center for initial viewpoint with Web Mercator Spatial Reference
    Point center = new Point(-13671170, 5693633, SpatialReferences.getWebMercator());

    //set initial viewpoint
    map.setInitialViewpoint(new Viewpoint(center, 57779));

    // set the map to the map view
    mMapView.setMap(map);

    // setup OnClickListener for FloatingActionButton
    FloatingActionButton calendarFab = findViewById(R.id.calendarButton);
    calendarFab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        showDateRangeDialog();
      }
    });

    // initialize geoprocessing task with the url of the service
    mGeoprocessingTask = new GeoprocessingTask(getString(R.string.hotspot_911_calls_url));
    mGeoprocessingTask.loadAsync();
  }

  /**
   * Creates the date range dialog. Includes listeners to handle click events, which call showCalendar(...) or
   * analyzeHotspots(...).
   */
  private void showDateRangeDialog() {
    if (getSupportFragmentManager().findFragmentByTag(DateRangeDialogFragment.class.getSimpleName()) == null) {
      DateRangeDialogFragment dateRangeDialogFragment = DateRangeDialogFragment.newInstance(
          getString(R.string.date_range_dialog_title),
          getString(R.string.date_range_dialog_submit_button_text)
      );
      dateRangeDialogFragment.show(getSupportFragmentManager(), DateRangeDialogFragment.class.getSimpleName());
    }
  }

  @Override public void onAnalyzeButtonClick(String fromDate, String toDate) {
    analyzeHotspots(fromDate, toDate);
  }

  @Override public void onProgressDialogDismiss() {
    // set canceled flag to true
    mCancelled = true;
    if (mGeoprocessingJob != null) {
      mGeoprocessingJob.cancel();
    }
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
    mMapView.getMap().getOperationalLayers().clear();

    // set canceled flag to false
    mCancelled = false;

    // parameters
    final ListenableFuture<GeoprocessingParameters> paramsFuture = mGeoprocessingTask.createDefaultParametersAsync();
    paramsFuture.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          GeoprocessingParameters geoprocessingParameters = paramsFuture.get();
          geoprocessingParameters.setProcessSpatialReference(mMapView.getSpatialReference());
          geoprocessingParameters.setOutputSpatialReference(mMapView.getSpatialReference());

          String queryString = String.format(QUERY_INPUT_STRING, from, to);
          geoprocessingParameters.getInputs().put("Query", new GeoprocessingString(queryString));
          Log.i(TAG, "Query: " + queryString.toString());

          // create job
          mGeoprocessingJob = mGeoprocessingTask.createJob(geoprocessingParameters);

          // start job
          mGeoprocessingJob.start();

          if (findProgressDialogFragment() == null) {
            ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(
                getString(R.string.progress_dialog_fragment_title),
                getString(R.string.progress_dialog_fragment_cancel_text)
            );
            progressDialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.class.getSimpleName());
          }

          // update progress
          mGeoprocessingJob.addProgressChangedListener(new Runnable() {
            @Override public void run() {
              if (findProgressDialogFragment() != null) {
                findProgressDialogFragment().setProgress(mGeoprocessingJob.getProgress());
              }
            }
          });

          // listen for job completion
          mGeoprocessingJob.addJobDoneListener(new Runnable() {
            @Override public void run() {
              if (mGeoprocessingJob.getStatus() == Job.Status.SUCCEEDED) {
                Log.i(TAG, "Job succeeded.");

                GeoprocessingResult geoprocessingResult = mGeoprocessingJob.getResult();
                final ArcGISMapImageLayer hotspotMapImageLayer = geoprocessingResult.getMapImageLayer();

                // add the new layer to the map
                mMapView.getMap().getOperationalLayers().add(hotspotMapImageLayer);

                hotspotMapImageLayer.addDoneLoadingListener(new Runnable() {
                  @Override public void run() {
                    // set the map viewpoint to the MapImageLayer, once loaded
                    mMapView.setViewpointGeometryAsync(hotspotMapImageLayer.getFullExtent());
                  }
                });
              } else if (mCancelled) {
                Toast.makeText(MainActivity.this, "Job canceled.", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Job cancelled.");
              } else {
                Log.e(TAG, "Job did not succeed!");
                Toast.makeText(MainActivity.this, "Job did not succeed", Toast.LENGTH_LONG).show();
              }
              if (findProgressDialogFragment() != null) {
                findProgressDialogFragment().dismiss();
              }
            }
          });
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private ProgressDialogFragment findProgressDialogFragment() {
    return (ProgressDialogFragment) getSupportFragmentManager()
        .findFragmentByTag(ProgressDialogFragment.class.getSimpleName());
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
    showDateRangeDialog();
  }

  @Override
  protected void onDestroy() {
    if (mGeoprocessingTask != null) {
      mGeoprocessingTask.cancelLoad();
    }

    if (mGeoprocessingJob != null) {
      mGeoprocessingJob.cancel();
    }
    mMapView.dispose();
    super.onDestroy();
  }

}