/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.downloadpreplannedmaparea;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.arcgisruntime.tasks.offlinemap.DownloadPreplannedOfflineMapJob;
import com.esri.arcgisruntime.tasks.offlinemap.DownloadPreplannedOfflineMapParameters;
import com.esri.arcgisruntime.tasks.offlinemap.DownloadPreplannedOfflineMapResult;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask;
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedMapArea;
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedUpdateMode;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private ListView mPreplannedAreasListView;
  private List<PreplannedMapArea> mPreplannedMapAreas;
  private ListView mDownloadedMapAreasListView;
  private List<ArcGISMap> mDownloadedMapAreas;
  private Button mDownloadButton;

  private MapView mMapView;
  private GraphicsOverlay mAreasOfInterestGraphicsOverlay;
  private OfflineMapTask mOfflineMapTask;
  private File mOfflineMapDirectory;


  private ArcGISMap mOnlineMap;
  private PreplannedMapArea mSelectedPreplannedMapArea;
  private ArrayAdapter<String> mDownloadedMapAreasAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create up a temporary directory in the app's cache for saving downloaded preplanned maps
    mOfflineMapDirectory = new File(getCacheDir() + "/preplanned_offline_map");
    if (mOfflineMapDirectory.mkdirs()) {
      Log.i(TAG, "Created directory for offline maps in " + mOfflineMapDirectory.getPath());
    } else if (mOfflineMapDirectory.exists()) {
      Log.i(TAG, "Did not create a new offline maps directory, because it already exists at " + mOfflineMapDirectory
          .getPath());
    } else {
      Log.e(TAG, "Error creating offline maps directory at " + mOfflineMapDirectory.getPath());
    }

    // create a portal to ArcGIS Online
    Portal portal = new Portal("https://www.arcgis.com/");

    // set the authentication manager to handle OAuth challenges when accessing the portal
    AuthenticationManager.setAuthenticationChallengeHandler(new DefaultAuthenticationChallengeHandler());

    // create a portal item using the portal and the item id of a map service
    PortalItem portalItem = new PortalItem(portal, "acc027394bc84c2fb04d1ed317aac674");

    // create a map with the portal item
    ArcGISMap onlineMap = new ArcGISMap(portalItem);

    // show the map
    mMapView.setMap(onlineMap);

    // create a graphics overlay to show the preplanned map areas extents (areas of interest)
    GraphicsOverlay areasOfInterestGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(areasOfInterestGraphicsOverlay);

    // create a red outline to mark the areas of interest of the preplanned map areas
    SimpleLineSymbol areaOfInterestLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0x80FF0000, 5.0f);
    SimpleRenderer areaOfInterestRenderer = new SimpleRenderer();
    areaOfInterestRenderer.setSymbol(areaOfInterestLineSymbol);
    areasOfInterestGraphicsOverlay.setRenderer(areaOfInterestRenderer);

    // create an offline map task for the portal item
    OfflineMapTask offlineMapTask = new OfflineMapTask(portalItem);

    // use a list view which shows the preplanned area's titles
    mPreplannedAreasListView = findViewById(R.id.availablePreplannedAreasListView);
    mPreplannedAreasListView.setAdapter(new ArrayAdapter<>(this, R.layout.item_map_area, mPreplannedMapAreas));

    // get the preplanned map areas from the offline map task and show them in the list view
    ListenableFuture<List<PreplannedMapArea>> preplannedMapAreasFuture = offlineMapTask.getPreplannedMapAreasAsync();
    preplannedMapAreasFuture.addDoneListener(() -> {
      try {
        // get the preplanned areas and add them to the list view
        List<PreplannedMapArea> preplannedMapAreas = preplannedMapAreasFuture.get();
        mPreplannedMapAreas.addAll(preplannedMapAreas);

        // load each area and show a red border around their area of interest
        for (PreplannedMapArea preplannedMapArea : preplannedMapAreas) {
          preplannedMapArea.loadAsync();
          preplannedMapArea.addDoneLoadingListener(() -> {
            if (preplannedMapArea.getLoadStatus() == LoadStatus.LOADED) {
              areasOfInterestGraphicsOverlay.getGraphics().add(new Graphic(preplannedMapArea.getAreaOfInterest()));
            } else {
              String error = "Failed to load preplanned map area: " + preplannedMapArea.getLoadError().getMessage();
              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
              Log.e(TAG, error);
            }
          });
        }
      } catch (InterruptedException | ExecutionException e) {
        String error = "Failed to get the Preplanned Map Areas from the Offline Map Task.";
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });

    mPreplannedAreasListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        PreplannedMapArea selectedPreplannedMapArea = preplannedAreasListView.getSelectionModel().getSelectedItem();
        if (selectedPreplannedMapArea != null) {

          // clear the download jobs list view selection
          downloadJobsListView.getSelectionModel().clearSelection();

          // show the online map with the areas of interest
          mMapView.setMap(onlineMap);
          areasOfInterestGraphicsOverlay.setVisible(true);

          // set the viewpoint to the preplanned map area's area of interest
          Envelope areaOfInterest = GeometryEngine.buffer(selectedPreplannedMapArea.getAreaOfInterest(), 100).getExtent();
          mMapView.setViewpointAsync(new Viewpoint(areaOfInterest), 1.5f);
        }
      }

      @Override public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    // disable the download button when no area is selected
    downloadButton.disableProperty().bind(preplannedAreasListView.getSelectionModel().selectedItemProperty().isNull());

    // use a cell factory which shows the download preplanned offline map job's progress and title
    downloadJobsListView.setCellFactory(c -> new DownloadPreplannedOfflineMapJobListCell());

    ChangeListener<DownloadPreplannedOfflineMapJob> selectedDownloadChangeListener = new ChangeListener<>() {
      @Override
      public void changed(ObservableValue<? extends DownloadPreplannedOfflineMapJob> observable, DownloadPreplannedOfflineMapJob oldValue, DownloadPreplannedOfflineMapJob newValue) {
        DownloadPreplannedOfflineMapJob selectedJob = downloadJobsListView.getSelectionModel().getSelectedItem();
        if (selectedJob != null) {

          // hide the preplanned map areas and clear the preplanned area list view's selection
          areasOfInterestGraphicsOverlay.setVisible(false);
          preplannedAreasListView.getSelectionModel().clearSelection();

          if (selectedJob.getStatus() == Job.Status.SUCCEEDED) {
            DownloadPreplannedOfflineMapResult result = selectedJob.getResult();

            // check if the result has errors
            if (result.hasErrors()) {

              // collect the layer and table errors into a single alert message
              StringBuilder stringBuilder = new StringBuilder("Errors: ");

              Map<Layer, ArcGISRuntimeException> layerErrors = result.getLayerErrors();
              layerErrors.forEach((layer, exception) ->
                  stringBuilder.append("Layer: ").append(layer.getName()).append(". Exception: ").append(exception.getMessage()).append(". ")
              );

              Map<FeatureTable, ArcGISRuntimeException> tableError = result.getTableErrors();
              tableError.forEach((table, exception) ->
                  stringBuilder.append("Table: ").append(table.getTableName()).append(". Exception: ").append(exception.getMessage()).append(". ")
              );

              new Alert(Alert.AlertType.ERROR, "One or more errors occurred with the Offline Map Result: " + stringBuilder.toString()).show();
            } else {
              // show the offline map in the map view
              ArcGISMap downloadOfflineMap = result.getOfflineMap();
              mapView.setMap(downloadOfflineMap);
            }

          } else {
            // alert the user the job is still in progress if selected before the job is done
            new Alert(Alert.AlertType.WARNING, "Job status: " + selectedJob.getStatus()).show();

            // when the job is done, re-trigger the listener to show the job's result if it is still selected
            selectedJob.addJobDoneListener(() ->
                this.changed(observable, oldValue, downloadJobsListView.getSelectionModel().getSelectedItem())
            );
          }
        }
      }
    };

    downloadJobsListView.getSelectionModel().selectedItemProperty().addListener(selectedDownloadChangeListener);

  } catch (Exception e) {
    // on any exception, print the stacktrace
    e.printStackTrace();
  }
}

  /**
   * Download the selected preplanned map area from the list view to a temporary directory. The download job is tracked in another list view.
   */
  private void handleDownloadPreplannedArea() {
    PreplannedMapArea selectedMapArea = preplannedAreasListView.getSelectionModel().getSelectedItem();
    if (selectedMapArea != null) {

      // hide the preplanned areas and clear the selection
      preplannedAreasListView.getSelectionModel().clearSelection();

      // create default download parameters from the offline map task
      ListenableFuture<DownloadPreplannedOfflineMapParameters> downloadPreplannedOfflineMapParametersFuture = offlineMapTask.createDefaultDownloadPreplannedOfflineMapParametersAsync(selectedMapArea);
      downloadPreplannedOfflineMapParametersFuture.addDoneListener(() -> {
        try {
          DownloadPreplannedOfflineMapParameters downloadPreplannedOfflineMapParameters = downloadPreplannedOfflineMapParametersFuture.get();

          // set the update mode to not receive updates
          downloadPreplannedOfflineMapParameters.setUpdateMode(PreplannedUpdateMode.NO_UPDATES);

          // create a job to download the preplanned offline map to a temporary directory
          Path path = Files.createTempDirectory(selectedMapArea.getPortalItem().getTitle());
          path.toFile().deleteOnExit();
          DownloadPreplannedOfflineMapJob downloadPreplannedOfflineMapJob = offlineMapTask.downloadPreplannedOfflineMap(downloadPreplannedOfflineMapParameters, path.toFile().getAbsolutePath());

          // start the job
          downloadPreplannedOfflineMapJob.start();

          // track the job in the second list view
          downloadJobsListView.getItems().add(downloadPreplannedOfflineMapJob);

        } catch (InterruptedException | ExecutionException e) {
          new Alert(Alert.AlertType.ERROR, "Failed to generate default parameters for the download job.").show();
        } catch(IOException e) {
          new Alert(Alert.AlertType.ERROR, "Failed to create a temporary directory for the download").show();
        }
      });
    }
  }

  public void deleteCache() {
    try {
      File directory = getApplicationContext().getCacheDir();
      deleteDirectory(directory);
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    deleteCache();
    super.onDestroy();
  }
}
