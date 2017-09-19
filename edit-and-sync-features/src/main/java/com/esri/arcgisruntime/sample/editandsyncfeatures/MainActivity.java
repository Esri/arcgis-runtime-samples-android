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

package com.esri.arcgisruntime.sample.editandsyncfeatures;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseJob;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private FeatureLayer mGeodatabaseFeatureLayer;
  private Feature mFeature;

  private TextView mProgressTextView;
  private RelativeLayout mProgressLayout;

  private EditState mEditState;

  // Enumeration to track which phase of the workflow the sample is in
  public enum EditState
  {
    NotReady, // Geodatabase has not yet been generated
    Editing, // A feature is in the process of being moved
    Ready // The geodatabase is ready for synchronization or further edits
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // set edit state to not ready until geodatabase job has completed successfully
    mEditState = EditState.NotReady;

    // define permission to request
    String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this, reqPermission[0]) != PackageManager.PERMISSION_GRANTED) {
      // request permission
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }

    // use local tile package for the base map
    TileCache sanFrancisco = new TileCache(
        Environment.getExternalStorageDirectory() + getString(R.string.san_francisco_tpk));
    ArcGISTiledLayer tiledLayer = new ArcGISTiledLayer(sanFrancisco);

    // create a map view and add a map
    mMapView = (MapView) findViewById(R.id.mapView);
    final ArcGISMap map = new ArcGISMap(new Basemap(tiledLayer));
    mMapView.setMap(map);

    // create a graphics overlay and symbol to mark the extent
    final GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);
    final SimpleLineSymbol boundarySymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5);

    // inflate button and progress layout
    final Button genGeodatabaseButton = (Button) findViewById(R.id.genGeodatabaseButton);
    mProgressLayout = (RelativeLayout) findViewById(R.id.progressLayout);
    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.taskProgressBar);
    mProgressTextView = (TextView) findViewById(R.id.progressTextView);

    // create a geodatabase sync task
    final GeodatabaseSyncTask geodatabaseSyncTask = new GeodatabaseSyncTask(getString(R.string.wildfire_sync));
    geodatabaseSyncTask.loadAsync();
    geodatabaseSyncTask.addDoneLoadingListener(new Runnable() {
      @Override public void run() {

        // generate the geodatabase sync task
        genGeodatabaseButton.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {

            // show the progress layout
            progressBar.setProgress(0);
            mProgressLayout.setVisibility(View.VISIBLE);

            // clear any previous operational layers and graphics if button clicked more than once
            map.getOperationalLayers().clear();
            graphicsOverlay.getGraphics().clear();

            // show the extent used as a graphic
            final Envelope extent = mMapView.getVisibleArea().getExtent();
            Graphic boundary = new Graphic(extent, boundarySymbol);
            graphicsOverlay.getGraphics().add(boundary);

            // create generate geodatabase parameters for the current extent
            final ListenableFuture<GenerateGeodatabaseParameters> defaultParameters = geodatabaseSyncTask
                .createDefaultGenerateGeodatabaseParametersAsync(extent);
            defaultParameters.addDoneListener(new Runnable() {
              @Override public void run() {
                try {
                  // set parameters and don't include attachments
                  GenerateGeodatabaseParameters parameters = defaultParameters.get();
                  parameters.setReturnAttachments(false);

                  // define the local path where the geodatabase will be stored
                  final String localGeodatabasePath =
                      getCacheDir().toString() + File.separator + getString(R.string.file_name);

                  // create and start the job
                  final GenerateGeodatabaseJob generateGeodatabaseJob = geodatabaseSyncTask
                      .generateGeodatabaseAsync(parameters, localGeodatabasePath);
                  generateGeodatabaseJob.start();
                  mProgressTextView.setText(getString(R.string.progress_started));

                  // update progress
                  generateGeodatabaseJob.addProgressChangedListener(new Runnable() {
                    @Override public void run() {
                      progressBar.setProgress(generateGeodatabaseJob.getProgress());
                      mProgressTextView.setText(getString(R.string.progress_fetching));
                    }
                  });

                  // get geodatabase when done
                  generateGeodatabaseJob.addJobDoneListener(new Runnable() {
                    @Override public void run() {
                      mProgressLayout.setVisibility(View.INVISIBLE);
                      if (generateGeodatabaseJob.getStatus() == Job.Status.SUCCEEDED) {
                        final Geodatabase geodatabase = generateGeodatabaseJob.getResult();
                        geodatabase.loadAsync();
                        geodatabase.addDoneLoadingListener(new Runnable() {
                          @Override public void run() {
                            if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                              mProgressTextView.setText(getString(R.string.progress_done));
                              for (GeodatabaseFeatureTable geodatabaseFeatureTable : geodatabase
                                  .getGeodatabaseFeatureTables()) {
                                geodatabaseFeatureTable.loadAsync();
                                mGeodatabaseFeatureLayer = new FeatureLayer(geodatabaseFeatureTable);
                                mGeodatabaseFeatureLayer.setSelectionColor(Color.BLUE);
                                mGeodatabaseFeatureLayer.setSelectionWidth(10.0);
                                map.getOperationalLayers().add(mGeodatabaseFeatureLayer);

                                // add listener to handle screen once geodatabase is loaded and added to the MapView
                                mMapView
                                    .setOnTouchListener(new DefaultMapViewOnTouchListener(MainActivity.this, mMapView) {
                                      @Override
                                      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                                        if (mEditState == EditState.Ready) {
                                          identifyFeature(motionEvent);
                                        } else if (mEditState == EditState.Editing) {
                                          Log.d(TAG, "editting");
                                          moveSelectedFeatureTo(motionEvent);
                                        }
                                        return true;
                                      }
                                    });
                              }
                              genGeodatabaseButton.setVisibility(View.GONE);
                              Log.i(TAG, "Local geodatabase stored at: " + localGeodatabasePath);
                            } else {
                              Log.e(TAG, "Error loading geodatabase: " + geodatabase.getLoadError().getMessage());
                            }
                          }
                        });
                        // set edit state to ready
                        mEditState = EditState.Ready;
                      } else if (generateGeodatabaseJob.getError() != null) {
                        Log.e(TAG, "Error generating geodatabase: " + generateGeodatabaseJob.getError().getMessage());
                      } else {
                        Log.e(TAG, "Unknown Error generating geodatabase");
                      }
                    }
                  });
                } catch (InterruptedException | ExecutionException e) {
                  Log.e(TAG, "Error generating geodatabase parameters : " + e.getMessage());
                }
              }
            });
          }
        });
      }
    });
  }

  /**
   * Identifies the Graphic at the tapped point.
   *
   * @param motionEvent containing a tapped screen point
   */
  private void identifyFeature(MotionEvent motionEvent) {
    // get the screen point
    android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
        Math.round(motionEvent.getY()));
    // get the point that was clicked and convert it to a point in map coordinates
    Point mapPoint = mMapView.screenToLocation(screenPoint);
    // define a tolerance for identifying the feature
    int tolerance = 10;
    double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
    // create objects required to do a selection with a query
    Envelope envelope = new Envelope(mapPoint.getX() - mapTolerance, mapPoint.getY() - mapTolerance,
        mapPoint.getX() + mapTolerance, mapPoint.getY() + mapTolerance, mMapView.getSpatialReference());
    QueryParameters query = new QueryParameters();
    query.setGeometry(envelope);
    // call select features
    final ListenableFuture<FeatureQueryResult> featureQueryResultFuture = mGeodatabaseFeatureLayer
        .selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW);
    // add done loading listener to fire when the selection returns
    featureQueryResultFuture.addDoneListener(new Runnable() {
      @Override
      public void run() {
        try {
          //call get on the future to get the result
          FeatureQueryResult result = featureQueryResultFuture.get();
          // create an Iterator
          Iterator<Feature> iterator = result.iterator();
          // cycle through selections
          int counter = 0;
          while (iterator.hasNext()) {
            mFeature = iterator.next();
            counter++;
            if (counter > 0) {
              mEditState = EditState.Editing;
            }
            Log.d(getResources().getString(R.string.app_name),
                "Selection #: " + counter + " Table name: " + mFeature.getFeatureTable().getTableName());
          }
          Toast.makeText(getApplicationContext(), counter + " features selected", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
          Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e.getMessage());
        }
      }
    });
  }

  /**
   * Moves a selected feature to the given point.
   *
   * @param motionEvent containing a tapped screen point
   */
  private void moveSelectedFeatureTo(MotionEvent motionEvent) {
    mEditState = EditState.NotReady;
    // get the screen point
    android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
        Math.round(motionEvent.getY()));
    // get the point that was clicked and convert it to a point in map coordinates
    Point mapPoint = mMapView.screenToLocation(screenPoint);

    mFeature.setGeometry(mapPoint);
    mFeature.getFeatureTable().updateFeatureAsync(mFeature);

  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }
}
