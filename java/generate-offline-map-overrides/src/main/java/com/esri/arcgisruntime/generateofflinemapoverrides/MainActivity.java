/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.generateofflinemapoverrides;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapJob;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapParameterOverrides;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapParameters;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapResult;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapParametersKey;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask;
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheParameters;
import com.esri.arcgisruntime.tasks.vectortilecache.ExportVectorTilesParameters;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private Button mGenerateOfflineMapOverridesButton;
  private GraphicsOverlay mGraphicsOverlay;
  private Graphic mDownloadArea;
  private GenerateOfflineMapParameterOverrides mParameterOverrides;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // access MapView from layout
    mMapView = findViewById(R.id.mapView);

    // access button to take the map offline and disable it until map is loaded
    mGenerateOfflineMapOverridesButton = findViewById(R.id.generateOfflineMapOverridesButton);
    mGenerateOfflineMapOverridesButton.setEnabled(false);

    // handle authentication with the portal
    AuthenticationManager.setAuthenticationChallengeHandler(new DefaultAuthenticationChallengeHandler(this));

    // create a portal item with the itemId of the web map
    Portal portal = new Portal(getString(R.string.portal_url), true);
    PortalItem portalItem = new PortalItem(portal, getString(R.string.item_id));

    // create a map with the portal item
    ArcGISMap map = new ArcGISMap(portalItem);

    // request write permission
    String[] reqPermission = { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // for API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      map.addDoneLoadingListener(() -> {
        if (map.getLoadStatus() == LoadStatus.LOADED) {
          // enable offline map button only after permission is granted and map is loaded
          mGenerateOfflineMapOverridesButton.setEnabled(true);
        }
      });
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }

    // set the map to the map view
    mMapView.setMap(map);

    // create a graphics overlay for the map view
    mGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    // create a graphic to show a box around the extent we want to download
    mDownloadArea = new Graphic();
    mGraphicsOverlay.getGraphics().add(mDownloadArea);
    SimpleLineSymbol simpleLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2);
    mDownloadArea.setSymbol(simpleLineSymbol);

    // update the download area box whenever the viewpoint changes
    mMapView.addViewpointChangedListener(viewpointChangedEvent -> {
      if (map.getLoadStatus() == LoadStatus.LOADED) {
        // upper left corner of the area to take offline
        android.graphics.Point minScreenPoint = new android.graphics.Point(200, 200);
        // lower right corner of the downloaded area
        android.graphics.Point maxScreenPoint = new android.graphics.Point(mMapView.getWidth() - 200,
            mMapView.getHeight() - 200);
        // convert screen points to map points
        Point minPoint = mMapView.screenToLocation(minScreenPoint);
        Point maxPoint = mMapView.screenToLocation(maxScreenPoint);
        // use the points to define and return an envelope
        if (minPoint != null && maxPoint != null) {
          Envelope envelope = new Envelope(minPoint, maxPoint);
          mDownloadArea.setGeometry(envelope);
        }
      }
    });

    // when the button is clicked, start the offline map task job
    mGenerateOfflineMapOverridesButton.setOnClickListener(v -> {
      showParametersDialog();
    });
  }

  private void showParametersDialog() {

    AlertDialog.Builder overrideParametersDialogBuilder = new AlertDialog.Builder(this);
    AlertDialog overrideParametersDialog = overrideParametersDialogBuilder.create();
    View overrideParametersView = getLayoutInflater().inflate(R.layout.override_parameters_dialog, null);

    // min and max seek bars
    SeekBar minScaleSeekBar = overrideParametersView.findViewById(R.id.minScaleSeekBar);
    SeekBar maxScaleSeekBar = overrideParametersView.findViewById(R.id.maxScaleSeekBar);
    minScaleSeekBar.setMax(22);
    minScaleSeekBar.setProgress(15);
    TextView currMinScale = overrideParametersView.findViewById(R.id.currMinScaleTextView);
    currMinScale.setText(String.valueOf(minScaleSeekBar.getProgress()));
    minScaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currMinScale.setText(String.valueOf(progress));
        if (progress >= maxScaleSeekBar.getProgress()) {
          maxScaleSeekBar.setProgress(progress + 1);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
    maxScaleSeekBar.setMax(23);
    maxScaleSeekBar.setProgress(23);
    TextView currMaxScale = overrideParametersView.findViewById(R.id.currMaxScaleTextview);
    currMaxScale.setText(String.valueOf(maxScaleSeekBar.getProgress()));
    maxScaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currMaxScale.setText(String.valueOf(progress));
        if (progress <= minScaleSeekBar.getProgress()) {
          minScaleSeekBar.setProgress(progress - 1);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // extent buffer seek bar
    SeekBar extentBufferDistanceSeekBar = overrideParametersView.findViewById(R.id.extentBufferDistanceSeekBar);
    extentBufferDistanceSeekBar.setMax(500);
    extentBufferDistanceSeekBar.setProgress(300);
    TextView currExtentBuffer = overrideParametersView.findViewById(R.id.currExtentBufferDistanceTextView);
    currExtentBuffer.setText(String.valueOf(extentBufferDistanceSeekBar.getProgress()));
    extentBufferDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currExtentBuffer.setText(String.valueOf(progress));
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // include layers checkboxes
    CheckBox systemValves = overrideParametersDialog.findViewById(R.id.systemValvesCheckBox);
    CheckBox serviceConnections = overrideParametersDialog.findViewById(R.id.serviceConnectionsCheckBox);

    // min hydrant flow rate seek bar
    SeekBar minHydrantFlowRateSeekBar = overrideParametersView.findViewById(R.id.minHydrantFlowRateSeekBar);
    minHydrantFlowRateSeekBar.setMax(2000);
    minHydrantFlowRateSeekBar.setProgress(500);
    TextView currMinHydrant = overrideParametersView.findViewById(R.id.currMinHydrantFlowRateTextView);
    currMinHydrant.setText(String.valueOf(extentBufferDistanceSeekBar.getProgress()));
    minHydrantFlowRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currMinHydrant.setText(String.valueOf(progress));
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // crop layer to extent checkbox
    CheckBox waterPipes = overrideParametersDialog.findViewById(R.id.waterPipesCheckBox);

    // setup dialog
    overrideParametersDialogBuilder.setView(overrideParametersView)
        .setTitle("Override Parameters")
        .setCancelable(true)
        .setNegativeButton("Cancel", (dialog, which) -> overrideParametersDialog.dismiss())
        .setPositiveButton("Start Job", (dialog, which) -> {

          generateOfflineMap(minScaleSeekBar.getProgress(), maxScaleSeekBar.getProgress(),
              extentBufferDistanceSeekBar.getProgress(), systemValves.isChecked(), serviceConnections.isChecked(),
              minHydrantFlowRateSeekBar.getProgress(), waterPipes.isChecked());
        })
        .show();
  }

  /**
   * Use the generate offline map job to generate an offline map.
   */
  private void generateOfflineMap(int minScale, int maxScale, int bufferDistance, boolean includeSystemValves,
      boolean includeServiceConnections, int flowRate, boolean cropWaterPipes) {

    // create a progress dialog to show download progress
    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Generate Offline Map Job");
    progressDialog.setMessage("Taking map offline...");
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setIndeterminate(false);
    progressDialog.setProgress(0);

    // delete any offline map already in the cache
    String tempDirectoryPath = getCacheDir() + File.separator + "offlineMap";
    deleteDirectory(new File(tempDirectoryPath));

    GenerateOfflineMapParameters generateOfflineMapParameters = new GenerateOfflineMapParameters(
        mDownloadArea.getGeometry(), minScale, maxScale);

    // create an offline map offlineMapTask with the map
    OfflineMapTask offlineMapTask = new OfflineMapTask(mMapView.getMap());

    // create parameter overrides for greater control
    ListenableFuture<GenerateOfflineMapParameterOverrides> parameterOverridesFuture = offlineMapTask
        .createGenerateOfflineMapParameterOverridesAsync(generateOfflineMapParameters);
    parameterOverridesFuture.addDoneListener(() -> {
      try {
        // get the parameter overrides
        mParameterOverrides = parameterOverridesFuture.get();

        // use the base map as the offline map parameters key
        OfflineMapParametersKey baseMapKey = new OfflineMapParametersKey(
            mMapView.getMap().getBasemap().getBaseLayers().get(0));


        // work with export tile cache parameter overrides
        Map<OfflineMapParametersKey, ExportTileCacheParameters> exportTileCacheParameters = mParameterOverrides
            .getExportTileCacheParameters();
        // add levels of detail based on min and max scales
        for (int i = minScale; i < maxScale; i++) {
          exportTileCacheParameters.get(baseMapKey).getLevelIDs().add(i);
        }
        // set the area of interest to be the initial area of interest with the given buffer distance
        exportTileCacheParameters.get(baseMapKey)
            .setAreaOfInterest(GeometryEngine.buffer(generateOfflineMapParameters.getAreaOfInterest(), bufferDistance));

        if (!includeSystemValves) {
          removeFeatureLayer("System Valve");
        }

        if (!includeServiceConnections) {
          removeFeatureLayer("Service Connection");
        }


        Map<OfflineMapParametersKey, ExportVectorTilesParameters> exportVectorTileCacheParameters = mParameterOverrides
            .getExportVectorTilesParameters();

        // create an offline map job with the download directory path and parameters and start the job
        GenerateOfflineMapJob job = offlineMapTask
            .generateOfflineMap(generateOfflineMapParameters, tempDirectoryPath, mParameterOverrides);

        // replace the current map with the result offline map when the job finishes
        job.addJobDoneListener(() -> {
          if (job.getStatus() == Job.Status.SUCCEEDED) {
            GenerateOfflineMapResult result = job.getResult();
            mMapView.setMap(result.getOfflineMap());
            mGraphicsOverlay.getGraphics().clear();
            mGenerateOfflineMapOverridesButton.setEnabled(false);
            Toast.makeText(this, "Now displaying offline map.", Toast.LENGTH_LONG).show();
          } else {
            String error = "Error in generate offline map job: " + job.getError().getAdditionalMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
          progressDialog.dismiss();
        });

        // show the job's progress with the progress dialog
        job.addProgressChangedListener(() -> progressDialog.setProgress(job.getProgress()));

        // start the job
        job.start();

      } catch (InterruptedException | ExecutionException e) {
        String error = "Error creating parameter overrides: " + e.getCause().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  private void removeFeatureLayer(String layerName) {

    // get the named feature layer
    FeatureLayer targetLayer = getFeatureLayerByName(layerName);

    // get the feature layer's offline map parameter key
    OfflineMapParametersKey key = new OfflineMapParametersKey(targetLayer);

    Map<OfflineMapParametersKey, GenerateGeodatabaseParameters> generateGeodatabaseParameters = mParameterOverrides
        .getGenerateGeodatabaseParameters();

    // remove the target layer from the geodatabase parameters
    generateGeodatabaseParameters.get(key).getLayerOptions().remove(targetLayer);

  }

  /**
   * Attempts to get the named feature layer from the map's operational layers.
   *
   * @param layerName as a String
   * @return the named feature layer, or null, if not found or if named layer is not a feature layer
   */
  private FeatureLayer getFeatureLayerByName(String layerName) {
    // find the feature layer with the given name
    LayerList operationalLayers = mMapView.getMap().getOperationalLayers();
    for (Layer layer : operationalLayers) {
      if (layer instanceof FeatureLayer && layer.getName().equals(layerName)) {
        return (FeatureLayer) layer;
      }
    }
    return null;
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      mMapView.getMap().addDoneLoadingListener(() -> {
        if (mMapView.getMap().getLoadStatus() == LoadStatus.LOADED) {
          // enable offline map button only after permission is granted and map is loaded
          mGenerateOfflineMapOverridesButton.setEnabled(true);
        }
      });
      Log.d(TAG, "permission granted");
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.offline_map_write_permission_denied), Toast.LENGTH_SHORT).show();
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

  /**
   * Recursively deletes all files in the given directory.
   *
   * @param file to delete
   */
  private static void deleteDirectory(File file) {
    if (file.isDirectory())
      for (File subFile : file.listFiles()) {
        deleteDirectory(subFile);
      }
    if (!file.delete()) {
      Log.e(TAG, "Failed to delete file: " + file.getPath());
    }
  }
}

