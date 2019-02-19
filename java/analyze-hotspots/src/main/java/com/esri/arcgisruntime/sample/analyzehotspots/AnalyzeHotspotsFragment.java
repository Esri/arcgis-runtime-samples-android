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

package com.esri.arcgisruntime.sample.analyzehotspots;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.esri.arcgisruntime.tasks.geoprocessing.*;

import java.util.concurrent.ExecutionException;

public class AnalyzeHotspotsFragment extends Fragment {

  private static final String TAG = AnalyzeHotspotsFragment.class.getSimpleName();

  // String used to query input for GeoprocessingString
  private static final String QUERY_INPUT_STRING = "(\"DATE\" > date '%1$s 00:00:00' AND \"DATE\" < date '%2$s 00:00:00')";

  private MapView mMapView;

  private GeoprocessingTask mGeoprocessingTask;

  private GeoprocessingJob mGeoprocessingJob;

  private boolean mCancelled;

  private ArcGISMapImageLayer mHotspotMapImageLayer;

  public static AnalyzeHotspotsFragment newInstance() {
    return new AnalyzeHotspotsFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);

    if (savedInstanceState == null) {
      // initialize geoprocessing task with the url of the service
      mGeoprocessingTask = new GeoprocessingTask(getString(R.string.hotspot_911_calls_url));
      mGeoprocessingTask.loadAsync();
    }

    showDateRangeDialog();
  }

  @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_analyze_hotspots, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // inflate MapView from layout
    mMapView = view.findViewById(R.id.mapView);

    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());

    //center for initial viewpoint with Web Mercator Spatial Reference
    Point center = new Point(-13671170, 5693633, SpatialReferences.getWebMercator());

    //set initial viewpoint
    map.setInitialViewpoint(new Viewpoint(center, 57779));

    // set the map to the map view
    mMapView.setMap(map);

    // If the layer has already been created, add it to the map view
    if (mHotspotMapImageLayer != null) {
      addLayerToMapView(mHotspotMapImageLayer, mMapView);

      // If the layer has already been loaded, set the viewpoint of the map view equal to the full extent of the layer
      if (mHotspotMapImageLayer.getLoadStatus() == LoadStatus.LOADED) {
        // set the map viewpoint to the MapImageLayer, once loaded
        mMapView.setViewpoint(new Viewpoint(mHotspotMapImageLayer.getFullExtent()));
      }
    }

    // setup OnClickListener for FloatingActionButton
    FloatingActionButton calendarFab = view.findViewById(R.id.calendarButton);
    calendarFab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        showDateRangeDialog();
      }
    });
  }

  /**
   * Runs the geoprocessing job, updating progress while loading. On job done, loads the resulting
   * ArcGISMapImageLayer to the map and resets the Viewpoint of the MapView.
   *
   * @param from string which holds a date
   * @param to   string which holds a date
   */
  void analyzeHotspots(final String from, final String to) {
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

          showProgressDialogFragment();

          // update progress
          mGeoprocessingJob.addProgressChangedListener(new Runnable() {
            @Override public void run() {
              if (findProgressDialogFragment() != null) {
                findProgressDialogFragment().setProgress(mGeoprocessingJob.getProgress());
                Log.d(TAG, "Updating progress: " + mGeoprocessingJob.getProgress());
              }
            }
          });

          // listen for job completion
          mGeoprocessingJob.addJobDoneListener(new Runnable() {
            @Override public void run() {
              if (mGeoprocessingJob.getStatus() == Job.Status.SUCCEEDED) {
                Log.i(TAG, "Job succeeded.");

                GeoprocessingResult geoprocessingResult = mGeoprocessingJob.getResult();
                mHotspotMapImageLayer = geoprocessingResult.getMapImageLayer();

                addLayerToMapView(mHotspotMapImageLayer, mMapView);

              } else if (mCancelled) {
                Toast.makeText(getContext(), "Job canceled.", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Job cancelled.");
              } else {
                Log.e(TAG, "Job did not succeed!");
                Toast.makeText(getContext(), "Job did not succeed", Toast.LENGTH_LONG).show();
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

  private void addLayerToMapView(final Layer layer, final MapView mapView) {
    // add the new layer to the map
    mapView.getMap().getOperationalLayers().add(layer);

    // Add a listener to set the viewpoint of the map view when it is finished loading only if the layer has not already
    // finished loading
    if (layer.getLoadStatus() != LoadStatus.LOADED) {
      layer.addDoneLoadingListener(new Runnable() {
        @Override public void run() {
          // set the map viewpoint to the MapImageLayer, once loaded
          mMapView.setViewpointGeometryAsync(layer.getFullExtent());
        }
      });
    }
  }

  void cancelGeoProcessingJob() {
    mCancelled = true;
    if (mGeoprocessingJob != null) {
      mGeoprocessingJob.cancel();
    }
  }

  private DateRangeDialogFragment findDateRangeDialogFragment() {
    return (DateRangeDialogFragment) getChildFragmentManager()
        .findFragmentByTag(DateRangeDialogFragment.class.getSimpleName());
  }

  private ProgressDialogFragment findProgressDialogFragment() {
    return (ProgressDialogFragment) getChildFragmentManager()
        .findFragmentByTag(ProgressDialogFragment.class.getSimpleName());
  }

  private void showDateRangeDialog() {
    if (findDateRangeDialogFragment() == null) {
      DateRangeDialogFragment dateRangeDialogFragment = DateRangeDialogFragment.newInstance(
          getString(R.string.date_range_dialog_title),
          getString(R.string.date_range_dialog_submit_button_text)
      );
      dateRangeDialogFragment.show(getChildFragmentManager(), DateRangeDialogFragment.class.getSimpleName());
    }
  }

  private void showProgressDialogFragment() {
    if (findProgressDialogFragment() == null) {
      ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(
          getString(R.string.progress_dialog_fragment_title),
          getString(R.string.progress_dialog_fragment_cancel_text)
      );
      progressDialogFragment.show(getChildFragmentManager(), ProgressDialogFragment.class.getSimpleName());
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  public void onPause() {
    // We have to disassociate the layer from the map before reattaching it when the Fragment is recreating
    mMapView.getMap().getOperationalLayers().remove(mHotspotMapImageLayer);
    mMapView.pause();
    super.onPause();
  }

  @Override
  public void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
