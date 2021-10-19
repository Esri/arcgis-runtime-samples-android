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

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
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
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateLayerOption;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapJob;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapParameterOverrides;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapParameters;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapResult;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapParametersKey;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask;
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheParameters;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private Button mGenerateOfflineMapOverridesButton;
  private MapView mMapView;
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

    // authentication with an API key or named user is required
    // to access basemaps and other location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // create a portal item with the itemId of the web map
    Portal portal = new Portal(getString(R.string.portal_url), false);
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

    // define the download area graphic
    mDownloadArea = new Graphic();
    mGraphicsOverlay.getGraphics().add(mDownloadArea);
    SimpleLineSymbol simpleLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2);
    mDownloadArea.setSymbol(simpleLineSymbol);

    // update the download area box whenever the viewpoint changes
    mMapView.addViewpointChangedListener(viewpointChangedEvent -> {
      if (map.getLoadStatus() == LoadStatus.LOADED) {
        mDownloadArea.setGeometry(createDownloadAreaGeometry());
      }
    });

    // when the button is clicked, start the offline map task job
    mGenerateOfflineMapOverridesButton.setOnClickListener(v -> showParametersDialog());
  }

  /**
   * Create an envelope representing the download area, used to define an area of interest
   *
   * @return download area Envelope
   */
  private Envelope createDownloadAreaGeometry() {
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
      return new Envelope(minPoint, maxPoint);
    }
    return null;
  }

  /**
   * Creates parameters dialog and handles processing of input to generateOfflineMap(...) when Start Job button is clicked.
   */
  private void showParametersDialog() {

    View overrideParametersView = getLayoutInflater().inflate(R.layout.override_parameters_dialog, null);

    // min and max seek bars
    TextView currMinScaleTextView = overrideParametersView.findViewById(R.id.currMinScaleTextView);
    TextView currMaxScaleTextView = overrideParametersView.findViewById(R.id.currMaxScaleTextview);

    SeekBar minScaleSeekBar = buildSeekBar(overrideParametersView.findViewById(R.id.minScaleSeekBar),
        currMinScaleTextView, 22, 15);
    SeekBar maxScaleSeekBar = buildSeekBar(overrideParametersView.findViewById(R.id.maxScaleSeekBar),
        currMaxScaleTextView, 23, 20);
    minScaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currMinScaleTextView.setText(String.valueOf(progress));
        if (progress >= maxScaleSeekBar.getProgress()) {
          // set max to 1 more than min value (since max must always be greater than min)
          currMaxScaleTextView.setText(String.valueOf(progress + 1));
          maxScaleSeekBar.setProgress(progress + 1);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
    maxScaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currMaxScaleTextView.setText(String.valueOf(progress));
        if (progress <= minScaleSeekBar.getProgress()) {
          // set min to 1 less than max value (since min must always be less than max)
          currMinScaleTextView.setText(String.valueOf(progress - 1));
          minScaleSeekBar.setProgress(progress - 1);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // extent buffer seek bar
    SeekBar extentBufferDistanceSeekBar = buildSeekBar(
        overrideParametersView.findViewById(R.id.extentBufferDistanceSeekBar),
        overrideParametersView.findViewById(R.id.currExtentBufferDistanceTextView), 500, 300);

    // include layers checkboxes
    CheckBox systemValves = overrideParametersView.findViewById(R.id.systemValvesCheckBox);
    CheckBox serviceConnections = overrideParametersView.findViewById(R.id.serviceConnectionsCheckBox);

    // min hydrant flow rate seek bar
    SeekBar minHydrantFlowRateSeekBar = buildSeekBar(
        overrideParametersView.findViewById(R.id.minHydrantFlowRateSeekBar),
        overrideParametersView.findViewById(R.id.currMinHydrantFlowRateTextView), 2000, 500);

    // crop layer to extent checkbox
    CheckBox waterPipes = overrideParametersView.findViewById(R.id.waterPipesCheckBox);

    // setup dialog
    AlertDialog.Builder overrideParametersDialogBuilder = new AlertDialog.Builder(this);
    AlertDialog overrideParametersDialog = overrideParametersDialogBuilder.create();
    overrideParametersDialogBuilder.setView(overrideParametersView)
        .setTitle("Override Parameters")
        .setCancelable(true)
        .setNegativeButton("Cancel", (dialog, which) -> overrideParametersDialog.dismiss())
        .setPositiveButton("Start Job",
            (dialog, which) -> {
              // re-create download area geometry in case user hasn't changed the Viewpoint
              mDownloadArea.setGeometry(createDownloadAreaGeometry());
              defineParameters(minScaleSeekBar.getProgress(), maxScaleSeekBar.getProgress(),
                  extentBufferDistanceSeekBar.getProgress(), systemValves.isChecked(), serviceConnections.isChecked(),
                  minHydrantFlowRateSeekBar.getProgress(), waterPipes.isChecked());
            })
        .show();
  }

  /**
   * Use parameters from the override parameters dialog to define parameter overrides.
   *
   * @param minScale                  levelId
   * @param maxScale                  levelId
   * @param bufferDistance            around the given area of interest
   * @param includeSystemValves       whether to include System Valves layer
   * @param includeServiceConnections whether to include the Service Connections layer
   * @param flowRate                  to limit hydrants in a where clause
   * @param cropWaterPipes            whether to crop the pipes layer
   */
  private void defineParameters(int minScale, int maxScale, int bufferDistance, boolean includeSystemValves,
      boolean includeServiceConnections, int flowRate, boolean cropWaterPipes) {
    // create an offline map offlineMapTask with the map
    OfflineMapTask offlineMapTask = new OfflineMapTask(mMapView.getMap());
    // create default generate offline map parameters from the offline map task
    ListenableFuture<GenerateOfflineMapParameters> generateOfflineMapParametersFuture = offlineMapTask
        .createDefaultGenerateOfflineMapParametersAsync(mDownloadArea.getGeometry());
    generateOfflineMapParametersFuture.addDoneListener(() -> {
      try {
        final GenerateOfflineMapParameters generateOfflineMapParameters = generateOfflineMapParametersFuture.get();
        // don't let generate offline map parameters continue on errors (including canceling during authentication)
        generateOfflineMapParameters.setContinueOnErrors(false);
        // create parameter overrides for greater control
        ListenableFuture<GenerateOfflineMapParameterOverrides> parameterOverridesFuture = offlineMapTask
            .createGenerateOfflineMapParameterOverridesAsync(generateOfflineMapParameters);
        parameterOverridesFuture.addDoneListener(() -> {
          try {
            // get the parameter overrides
            mParameterOverrides = parameterOverridesFuture.get();
            // set basemap scale and area of interest
            setBasemapScaleAndAreaOfInterest(minScale, maxScale, bufferDistance);
            // exclude system valve layer
            if (!includeSystemValves) {
              excludeLayerFromDownload("System Valve");
            }
            // exclude service connection layer
            if (!includeServiceConnections) {
              excludeLayerFromDownload("Service Connection");
            }
            // crop pipes layer
            if (cropWaterPipes) {
              for (GenerateLayerOption generateLayerOption : getGenerateGeodatabaseParametersLayerOptions("Main")) {
                generateLayerOption.setUseGeometry(true);
              }
            }
            // set flow rate where clause on the hydrant layer
            for (GenerateLayerOption generateLayerOption : getGenerateGeodatabaseParametersLayerOptions("Hydrant")) {
              if (generateLayerOption.getLayerId() == getServiceLayerId(Objects
                  .requireNonNull(getFeatureLayerByName("Hydrant")))) {
                generateLayerOption.setWhereClause("FLOW >= " + flowRate);
                generateLayerOption.setQueryOption(GenerateLayerOption.QueryOption.USE_FILTER);
              }
            }
            // start a an offline map job from the task and parameters
            generateOfflineMap(offlineMapTask, generateOfflineMapParameters);
          } catch (InterruptedException | ExecutionException e) {
            String error = "Error creating parameter overrides: " + e.getCause().getMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
      } catch (InterruptedException | ExecutionException e) {
        String error = "Error generating default generate offline map parameters: " + e.getCause().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Use the generate offline map job to generate an offline map.
   */
  private void generateOfflineMap(OfflineMapTask offlineMapTask,
      GenerateOfflineMapParameters generateOfflineMapParameters) {
    // delete any offline map already in the cache
    String tempDirectoryPath = getCacheDir() + File.separator + "offlineMap";
    deleteDirectory(new File(tempDirectoryPath));
    // create an offline map job with the download directory path and parameters and start the job
    GenerateOfflineMapJob job = offlineMapTask
        .generateOfflineMap(generateOfflineMapParameters, tempDirectoryPath, mParameterOverrides);
    // show the job's progress in a progress dialog
    showProgressDialog(job);
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
    });
    // start the job
    job.start();
  }

  /**
   * Set basemap scale and area of interest using the given values
   *
   * @param minScale       levelId
   * @param maxScale       levelId
   * @param bufferDistance around the given area of interest
   */
  private void setBasemapScaleAndAreaOfInterest(int minScale, int maxScale, int bufferDistance) {
    // get the export tile cache parameters
    ExportTileCacheParameters exportTileCacheParameters = getExportTileCacheParameters(
        mMapView.getMap().getBasemap().getBaseLayers().get(0));
    // create a new sublist of LODs in the range requested by the user
    exportTileCacheParameters.getLevelIDs().clear();
    for (int i = minScale; i < maxScale; i++) {
      exportTileCacheParameters.getLevelIDs().add(i);
    }
    // set the area of interest to the original download area plus a buffer
    exportTileCacheParameters.setAreaOfInterest(GeometryEngine.buffer(mDownloadArea.getGeometry(), bufferDistance));
  }

  /**
   * Remove the layer named from the generate layer options list in the generate geodatabase parameters.
   *
   * @param layerName as a string
   */
  private void excludeLayerFromDownload(String layerName) {
    // get the named feature layer
    FeatureLayer targetLayer = getFeatureLayerByName(layerName);
    // get the layer's id
    long targetLayerId = getServiceLayerId(targetLayer);
    // get the layer's layer options
    List<GenerateLayerOption> layerOptions = getGenerateGeodatabaseParametersLayerOptions(layerName);
    // remove the target layer
    for (GenerateLayerOption layerOption : layerOptions) {
      if (layerOption.getLayerId() == targetLayerId) {
        layerOptions.remove(layerOption);
        break;
      }
    }
  }

  /**
   * Helper method to get export tile cache parameters for the given layer.
   *
   * @param layer to get parameters for
   * @return ExportTileCacheParameters for the given layer
   */
  private ExportTileCacheParameters getExportTileCacheParameters(Layer layer) {
    OfflineMapParametersKey key = new OfflineMapParametersKey(layer);
    return mParameterOverrides.getExportTileCacheParameters().get(key);
  }

  /**
   * Helper method to get generate geodatabase parameters for the given layer.
   *
   * @param layer to get parameters for
   * @return GenerateGeodatabaseParameters for the given layer
   */
  private GenerateGeodatabaseParameters getGenerateGeodatabaseParameters(Layer layer) {
    OfflineMapParametersKey key = new OfflineMapParametersKey(layer);
    return mParameterOverrides.getGenerateGeodatabaseParameters().get(key);
  }

  /**
   * Helper method to get the generate geodatabase parameters layer options for the given layer.
   *
   * @param layerName to get layer options for
   * @return list of GenerateLayerOptions
   */
  private List<GenerateLayerOption> getGenerateGeodatabaseParametersLayerOptions(String layerName) {
    // get the named feature layer
    FeatureLayer targetFeatureLayer = getFeatureLayerByName(layerName);
    // get the generate geodatabase parameters for the layer
    GenerateGeodatabaseParameters generateGeodatabaseParameters = getGenerateGeodatabaseParameters(targetFeatureLayer);
    // return the layer options
    return generateGeodatabaseParameters.getLayerOptions();
  }

  /**
   * Helper method to get the service layer id for the given feature layer
   *
   * @param featureLayer to get service id for
   * @return service layer id as a long
   */
  private long getServiceLayerId(FeatureLayer featureLayer) {
    ServiceFeatureTable serviceFeatureTable = (ServiceFeatureTable) featureLayer.getFeatureTable();
    return serviceFeatureTable.getLayerInfo().getServiceLayerId();
  }

  /**
   * Helper method to get the named feature layer from the map's operational layers.
   *
   * @param layerName as a String
   * @return the named feature layer, or null, if not found or if named layer is not a feature layer
   */
  private FeatureLayer getFeatureLayerByName(String layerName) {
    LayerList operationalLayers = mMapView.getMap().getOperationalLayers();
    for (Layer layer : operationalLayers) {
      if (layer instanceof FeatureLayer && layer.getName().equals(layerName)) {
        return (FeatureLayer) layer;
      }
    }
    return null;
  }

  /**
   * Shows a progress dialog for the given job.
   *
   * @param job to track progress from
   */
  private void showProgressDialog(Job job) {
    // create a progress dialog to show download progress
    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Generate Offline Map Job");
    progressDialog.setMessage("Taking map offline...");
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setIndeterminate(false);
    progressDialog.setProgress(0);
    progressDialog.setCanceledOnTouchOutside(false);
    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> job.cancel());
    progressDialog.show();

    // show the job's progress with the progress dialog
    job.addProgressChangedListener(() -> progressDialog.setProgress(job.getProgress()));

    // dismiss dialog when job is done
    job.addJobDoneListener(progressDialog::dismiss);
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
   * Builds a seek bar and handles updating of the associated current seek bar text view.
   *
   * @param seekBar             view to build
   * @param currSeekBarTextView to be updated when the seek bar progress changes
   * @param max                 max value for the seek bar
   * @param progress            initial progress position of the seek bar
   * @return the built seek bar
   */
  private static SeekBar buildSeekBar(SeekBar seekBar, TextView currSeekBarTextView, int max, int progress) {
    seekBar.setMax(max);
    seekBar.setProgress(progress);
    currSeekBarTextView.setText(String.valueOf(seekBar.getProgress()));
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currSeekBarTextView.setText(String.valueOf(progress));
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
    return seekBar;
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

