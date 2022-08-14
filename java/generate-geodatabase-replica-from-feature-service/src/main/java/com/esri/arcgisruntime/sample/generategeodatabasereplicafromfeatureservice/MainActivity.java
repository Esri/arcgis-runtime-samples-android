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

package com.esri.arcgisruntime.sample.generategeodatabasereplicafromfeatureservice;

import java.io.File;
import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
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

  private TextView mProgressTextView;
  private RelativeLayout mProgressLayout;
  
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private GeodatabaseSyncTask mGeodatabaseSyncTask;
  private Geodatabase mGeodatabase;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // use local tile package for the base map
    TileCache sanFrancisco = new TileCache(getExternalFilesDir(null) + getString(R.string.san_francisco_tpkx));
    ArcGISTiledLayer tiledLayer = new ArcGISTiledLayer(sanFrancisco);

    // create a map view and add a map
    mMapView = findViewById(R.id.mapView);
    final ArcGISMap map = new ArcGISMap(new Basemap(tiledLayer));
    mMapView.setMap(map);

    // create a graphics overlay and symbol to mark the extent
    final GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);
    final SimpleLineSymbol boundarySymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5);

    // inflate button and progress layout
    final Button genGeodatabaseButton = findViewById(R.id.genGeodatabaseButton);
    mProgressLayout = findViewById(R.id.progressLayout);
    final ProgressBar progressBar = findViewById(R.id.taskProgressBar);
    mProgressTextView = findViewById(R.id.progressTextView);

    // create a geodatabase sync task
    mGeodatabaseSyncTask = new GeodatabaseSyncTask(getString(R.string.wildfire_sync));
    mGeodatabaseSyncTask.loadAsync();
    mGeodatabaseSyncTask.addDoneLoadingListener(() -> {

      // generate the geodatabase sync task
      genGeodatabaseButton.setOnClickListener(v -> {

        // show the progress layout
        progressBar.setProgress(0);
        mProgressLayout.setVisibility(View.VISIBLE);

        // clear any previous operational layers and graphics if button clicked more than once
        map.getOperationalLayers().clear();
        graphicsOverlay.getGraphics().clear();

        // show the extent used as a graphic
        Envelope extent = mMapView.getVisibleArea().getExtent();
        Graphic boundary = new Graphic(extent, boundarySymbol);
        graphicsOverlay.getGraphics().add(boundary);

        // create generate geodatabase parameters for the current extent
        final ListenableFuture<GenerateGeodatabaseParameters> defaultParameters = mGeodatabaseSyncTask
            .createDefaultGenerateGeodatabaseParametersAsync(extent);
        defaultParameters.addDoneListener(() -> {
          try {
            // set parameters and don't include attachments
            GenerateGeodatabaseParameters parameters = defaultParameters.get();
            parameters.setReturnAttachments(false);

            // define the local path where the geodatabase will be stored
            final String localGeodatabasePath =
                getCacheDir() + File.separator + getString(R.string.wildfire_geodatabase);

            // create and start the job
            final GenerateGeodatabaseJob generateGeodatabaseJob = mGeodatabaseSyncTask
                .generateGeodatabase(parameters, localGeodatabasePath);
            generateGeodatabaseJob.start();
            mProgressTextView.setText(getString(R.string.progress_started));

            // update progress
            generateGeodatabaseJob.addProgressChangedListener(() -> {
              progressBar.setProgress(generateGeodatabaseJob.getProgress());
              mProgressTextView.setText(getString(R.string.progress_fetching));
            });

            // get geodatabase when done
            generateGeodatabaseJob.addJobDoneListener(() -> {
              mProgressLayout.setVisibility(View.INVISIBLE);
              if (generateGeodatabaseJob.getStatus() == Job.Status.SUCCEEDED) {
                mGeodatabase = generateGeodatabaseJob.getResult();
                mGeodatabase.loadAsync();
                mGeodatabase.addDoneLoadingListener(() -> {
                  if (mGeodatabase.getLoadStatus() == LoadStatus.LOADED) {
                    mProgressTextView.setText(getString(R.string.progress_done));
                    for (GeodatabaseFeatureTable geodatabaseFeatureTable : mGeodatabase
                        .getGeodatabaseFeatureTables()) {
                      geodatabaseFeatureTable.loadAsync();
                      map.getOperationalLayers().add(new FeatureLayer(geodatabaseFeatureTable));
                    }
                    genGeodatabaseButton.setVisibility(View.GONE);
                    Log.i(TAG, "Local geodatabase stored at: " + localGeodatabasePath);
                  } else {
                    Log.e(TAG, "Error loading geodatabase: " + mGeodatabase.getLoadError().getMessage());
                  }
                });
                // unregister since we're not syncing
                ListenableFuture unregisterGeodatabase = mGeodatabaseSyncTask.unregisterGeodatabaseAsync(mGeodatabase);
                unregisterGeodatabase.addDoneListener(() -> {
                  Log.i(TAG, "Geodatabase unregistered since we wont be editing it in this sample.");
                  Toast.makeText(MainActivity.this,
                      "Geodatabase unregistered since we wont be editing it in this sample.",
                      Toast.LENGTH_LONG).show();
                });
              } else if (generateGeodatabaseJob.getError() != null) {
                Log.e(TAG, "Error generating geodatabase: " + generateGeodatabaseJob.getError().getMessage());
              } else {
                Log.e(TAG, "Unknown Error generating geodatabase");
              }
            });
          } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Error generating geodatabase parameters : " + e.getMessage());
          }
        });
      });
    });
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

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }
}
