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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity implements ProgressDialogFragment.OnProgressDialogDismissListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  private File mOfflineMapDirectory;

  private ListView mPreplannedAreasListView;
  private List<String> mPreplannedMapAreaNames;
  private ArrayAdapter<String> mPreplannedMapAreasAdapter;
  private ListView mDownloadedMapAreasListView;
  private List<String> mDownloadedMapAreaNames;
  private ArrayAdapter<String> mDownloadedMapAreasAdapter;
  private final List<ArcGISMap> mDownloadedMapAreas = new ArrayList<>();
  private Button mDownloadButton;

  private PreplannedMapArea mSelectedPreplannedMapArea;
  private List<PreplannedMapArea> mPreplannedMapAreas;
  private DownloadPreplannedOfflineMapJob mDownloadPreplannedOfflineMapJob;
  private MapView mMapView;
  private GraphicsOverlay mAreasOfInterestGraphicsOverlay;
  private OfflineMapTask mOfflineMapTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    // delete any previous instances of downloaded maps
    deleteDirectory(getCacheDir());

    // create up a temporary directory in the app's cache for saving downloaded preplanned maps
    mOfflineMapDirectory = new File(getCacheDir() + getString(R.string.preplanned_offline_map_dir));
    if (mOfflineMapDirectory.mkdirs()) {
      Log.i(TAG, "Created directory for offline map in " + mOfflineMapDirectory.getPath());
    } else if (mOfflineMapDirectory.exists()) {
      Log.i(TAG,
          "Did not create a new offline map directory, one already exists at " + mOfflineMapDirectory.getPath());
    } else {
      Log.e(TAG, "Error creating offline map directory at: " + mOfflineMapDirectory.getPath());
    }

    // set the authentication manager to handle challenges when accessing the portal
    // Note: The sample data is publicly available, so you shouldn't be challenged
    AuthenticationManager.setAuthenticationChallengeHandler(new DefaultAuthenticationChallengeHandler(this));

    // create a portal to ArcGIS Online
    Portal portal = new Portal(getString(R.string.arcgis_online_url));
    // create a portal item using the portal and the item id of a map service
    PortalItem portalItem = new PortalItem(portal, getString(R.string.naperville_water_network_url));
    // create an offline map task from the portal item
    mOfflineMapTask = new OfflineMapTask(portalItem);
    // create a map with the portal item
    ArcGISMap onlineMap = new ArcGISMap(portalItem);
    // show the map
    mMapView = findViewById(R.id.mapView);
    mMapView.setMap(onlineMap);

    // create an offline map task for the portal item
    OfflineMapTask offlineMapTask = new OfflineMapTask(portalItem);

    // create a graphics overlay to show the preplanned map areas extents (areas of interest)
    mAreasOfInterestGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mAreasOfInterestGraphicsOverlay);
    // create a red outline to mark the areas of interest of the preplanned map areas
    SimpleLineSymbol areaOfInterestLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5.0f);
    SimpleRenderer areaOfInterestRenderer = new SimpleRenderer();
    areaOfInterestRenderer.setSymbol(areaOfInterestLineSymbol);
    mAreasOfInterestGraphicsOverlay.setRenderer(areaOfInterestRenderer);

    createPreplannedAreasListView(onlineMap, offlineMapTask);

    createDownloadAreasListView();

    // create download button
    mDownloadButton = findViewById(R.id.downloadButton);
    mDownloadButton.setEnabled(false);
    mDownloadButton.setOnClickListener(view -> downloadPreplannedArea());
  }

  /**
   * Download the selected preplanned map area from the list view to a temporary directory. The download job is tracked in another list view.
   */
  private void downloadPreplannedArea() {
    if (mSelectedPreplannedMapArea != null) {
      // create default download parameters from the offline map task
      ListenableFuture<DownloadPreplannedOfflineMapParameters> offlineMapParametersFuture = mOfflineMapTask
          .createDefaultDownloadPreplannedOfflineMapParametersAsync(mSelectedPreplannedMapArea);
      offlineMapParametersFuture.addDoneListener(() -> {
        try {
          // get the offline map parameters
          DownloadPreplannedOfflineMapParameters offlineMapParameters = offlineMapParametersFuture.get();
          // set the update mode to not receive updates
          offlineMapParameters.setUpdateMode(PreplannedUpdateMode.NO_UPDATES);
          // create a job to download the preplanned offline map to a temporary directory
          mDownloadPreplannedOfflineMapJob = mOfflineMapTask.downloadPreplannedOfflineMap(offlineMapParameters,
              mOfflineMapDirectory.getPath() + File.separator + mSelectedPreplannedMapArea.getPortalItem().getTitle());
          // start the job
          mDownloadPreplannedOfflineMapJob.start();

          // show progress dialog for download, includes tracking progress
          showProgressDialog();

          // when the job finishes
          mDownloadPreplannedOfflineMapJob.addJobDoneListener(() -> {
            dismissDialog();
            // if there's a result from the download preplanned offline map job
            if (mDownloadPreplannedOfflineMapJob.getStatus() == Job.Status.SUCCEEDED) {
              DownloadPreplannedOfflineMapResult downloadPreplannedOfflineMapResult = mDownloadPreplannedOfflineMapJob
                  .getResult();
              if (mDownloadPreplannedOfflineMapJob != null && !downloadPreplannedOfflineMapResult.hasErrors()) {
                // get the offline map
                ArcGISMap offlineMap = downloadPreplannedOfflineMapResult.getOfflineMap();
                // add it to the map view
                mMapView.setMap(offlineMap);
                // add the map name to the list view of downloaded map areas
                mDownloadedMapAreaNames.add(offlineMap.getItem().getTitle());
                // select the downloaded map area
                mDownloadedMapAreasListView.setItemChecked(mDownloadedMapAreaNames.size() - 1, true);
                mDownloadedMapAreasAdapter.notifyDataSetChanged();
                // de-select the area in the preplanned areas list view
                mPreplannedAreasListView.clearChoices();
                mPreplannedMapAreasAdapter.notifyDataSetChanged();
                // add the offline map to a list of downloaded map areas
                mDownloadedMapAreas.add(offlineMap);
                // hide the area of interest graphics
                mAreasOfInterestGraphicsOverlay.setVisible(false);
                // disable the download button
                mDownloadButton.setEnabled(false);
              } else {
                // collect the layer and table errors into a single alert message
                StringBuilder stringBuilder = new StringBuilder("Errors: ");
                Map<Layer, ArcGISRuntimeException> layerErrors = downloadPreplannedOfflineMapResult.getLayerErrors();
                for (Map.Entry<Layer, ArcGISRuntimeException> layer : layerErrors.entrySet()) {
                  stringBuilder.append("Layer: ").append(layer.getKey().getName()).append(". Exception: ")
                      .append(layer.getValue().getMessage()).append(". ");
                }
                Map<FeatureTable, ArcGISRuntimeException> tableErrors = downloadPreplannedOfflineMapResult
                    .getTableErrors();
                for (Map.Entry<FeatureTable, ArcGISRuntimeException> table : tableErrors.entrySet()) {
                  stringBuilder.append("Table: ").append(table.getKey().getTableName()).append(". Exception: ")
                      .append(table.getValue().getMessage()).append(". ");
                }
                String error = "One or more errors occurred with the Offline Map Result: " + stringBuilder;
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                Log.e(TAG, error);
              }
            } else {
              String error = "Job finished with an error: " + mDownloadPreplannedOfflineMapJob.getError();
              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
              Log.e(TAG, error);
            }
          });
        } catch (InterruptedException | ExecutionException e) {
          String error = "Failed to generate default parameters for the download job: " + e.getCause().getMessage();
          Toast.makeText(this, error, Toast.LENGTH_LONG).show();
          Log.e(TAG, error);
        }
      });
    }
  }

  private void createPreplannedAreasListView(ArcGISMap onlineMap, OfflineMapTask offlineMapTask) {
    // create a list view which holds available preplanned map areas
    mPreplannedAreasListView = findViewById(R.id.availablePreplannedAreasListView);
    mPreplannedMapAreas = new ArrayList<>();
    mPreplannedMapAreaNames = new ArrayList<>();
    mPreplannedMapAreasAdapter = new ArrayAdapter<>(this, R.layout.item_map_area, mPreplannedMapAreaNames);
    mPreplannedAreasListView.setAdapter(mPreplannedMapAreasAdapter);
    // get the preplanned map areas from the offline map task and show them in the list view
    ListenableFuture<List<PreplannedMapArea>> preplannedMapAreasFuture = offlineMapTask.getPreplannedMapAreasAsync();
    preplannedMapAreasFuture.addDoneListener(() -> {
      try {
        // get the preplanned areas and add them to the list view
        mPreplannedMapAreas = preplannedMapAreasFuture.get();
        for (PreplannedMapArea preplannedMapArea : mPreplannedMapAreas) {
          mPreplannedMapAreaNames.add(preplannedMapArea.getPortalItem().getTitle());
        }
        mPreplannedMapAreasAdapter.notifyDataSetChanged();
        // load each area and show a red border around their area of interest
        for (PreplannedMapArea preplannedMapArea : mPreplannedMapAreas) {
          preplannedMapArea.loadAsync();
          preplannedMapArea.addDoneLoadingListener(() -> {
            if (preplannedMapArea.getLoadStatus() == LoadStatus.LOADED) {
              mAreasOfInterestGraphicsOverlay.getGraphics().add(new Graphic(preplannedMapArea.getAreaOfInterest()));
            } else {
              String error = "Failed to load preplanned map area: " + preplannedMapArea.getLoadError().getMessage();
              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
              Log.e(TAG, error);
            }
          });
        }
        // on list view click
        mPreplannedAreasListView.setOnItemClickListener((adapterView, view, i, l) -> {
          mSelectedPreplannedMapArea = mPreplannedMapAreas.get(i);
          if (mSelectedPreplannedMapArea != null) {
            // show graphics overlay which highlights available preplanned map areas
            mAreasOfInterestGraphicsOverlay.setVisible(true);
            // clear the download jobs list view selection
            mDownloadedMapAreasListView.clearChoices();
            mDownloadedMapAreasAdapter.notifyDataSetChanged();
            // show the online map with the areas of interest
            mMapView.setMap(onlineMap);
            mAreasOfInterestGraphicsOverlay.setVisible(true);
            // set the viewpoint to the preplanned map area's area of interest
            Envelope areaOfInterest = GeometryEngine.buffer(mSelectedPreplannedMapArea.getAreaOfInterest(), 50)
                .getExtent();
            mMapView.setViewpointAsync(new Viewpoint(areaOfInterest), 1.5f);
            // enable download button only for those map areas which have not been downloaded already
            mDownloadButton.setEnabled(!new File(getCacheDir() + getString(R.string.preplanned_offline_map_dir) + File.separator
                    + mSelectedPreplannedMapArea.getPortalItem().getTitle()).exists());
          } else {
            mDownloadButton.setEnabled(false);
          }
        });
      } catch (InterruptedException | ExecutionException e) {
        String error = "Failed to get the Preplanned Map Areas from the Offline Map Task.";
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  private void createDownloadAreasListView() {
    // create a list view which holds downloaded map areas
    mDownloadedMapAreasListView = findViewById(R.id.downloadedMapAreasListView);
    mDownloadedMapAreaNames = new ArrayList<>();
    mDownloadedMapAreasAdapter = new ArrayAdapter<>(this, R.layout.item_map_area, mDownloadedMapAreaNames);
    mDownloadedMapAreasListView.setAdapter(mDownloadedMapAreasAdapter);
    mDownloadedMapAreasListView.setOnItemClickListener((adapterView, view, i, l) -> {
      // set the downloaded map to the map view
      mMapView.setMap(mDownloadedMapAreas.get(i));
      // disable the download button
      mDownloadButton.setEnabled(false);
      // clear the available map areas list view selection
      mPreplannedAreasListView.clearChoices();
      mPreplannedMapAreasAdapter.notifyDataSetChanged();

      // hide the graphics overlays
      mAreasOfInterestGraphicsOverlay.setVisible(false);
    });
  }

  /**
   * Dismiss the dialog.
   */
  private void dismissDialog() {
    // dismiss progress dialog
    if (findProgressDialogFragment() != null) {
      findProgressDialogFragment().dismiss();
    }
  }

  /**
   * Show dialog and track progress.
   */
  private void showProgressDialog() {
    // show progress of the download preplanned offline map job in a dialog
    if (findProgressDialogFragment() == null) {
      ProgressDialogFragment progressDialogFragment = ProgressDialogFragment
          .newInstance("Download preplanned offline map job", "Downloading the requested preplanned map area...",
              "Cancel");
      progressDialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.class.getSimpleName());

      // track progress
      mDownloadPreplannedOfflineMapJob.addProgressChangedListener(() -> {
        if (findProgressDialogFragment() != null) {
          findProgressDialogFragment().setProgress(mDownloadPreplannedOfflineMapJob.getProgress());
        }
      });
    }
  }

  /**
   * Find and return the progress dialog fragment.
   *
   * @return the progress dialog fragment.
   */
  private ProgressDialogFragment findProgressDialogFragment() {
    return (ProgressDialogFragment) getSupportFragmentManager()
        .findFragmentByTag(ProgressDialogFragment.class.getSimpleName());
  }

  /**
   * Callback to cancel the download preplanned offline map job on progress dialog cancel button click.
   */
  @Override public void onProgressDialogDismiss() {
    if (mDownloadPreplannedOfflineMapJob != null) {
      mDownloadPreplannedOfflineMapJob.cancel();
    }
  }

  /**
   * Recursively deletes all files in the given directory.
   *
   * @param dir to delete
   */
  private static boolean deleteDirectory(File dir) {
    if (dir != null && dir.isDirectory()) {
      String[] children = dir.list();
      for (String child : children) {
        boolean success = deleteDirectory(new File(dir, child));
        if (!success) {
          return false;
        }
      }
      return dir.delete();
    } else if (dir != null && dir.isFile()) {
      return dir.delete();
    } else {
      return false;
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
}
