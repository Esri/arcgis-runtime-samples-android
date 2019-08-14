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

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
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
import com.esri.arcgisruntime.mapping.MobileMapPackage;
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
  private ListView mDownloadedMapAreasListView;
  private Button mDownloadButton;

  private MapView mMapView;
  private GraphicsOverlay mAreasOfInterestGraphicsOverlay;
  private OfflineMapTask mOfflineMapTask;
  private File mOfflineMapDirectory;
  private List<PreplannedMapArea> mPreplannedMapAreas;
  private List<ArcGISMap> mDownloadedMapAreas;
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

    // get to the download button and disable it
    mDownloadButton = findViewById(R.id.downloadButton);
    mDownloadButton.setEnabled(false);
    mDownloadButton.setOnClickListener(v -> downloadPreplannedMapArea());

    // create a portal to ArcGIS Online
    Portal portal = new Portal("https://www.arcgis.com/");

    // set the authentication manager to handle OAuth challenges when accessing the portal
    AuthenticationManager.setAuthenticationChallengeHandler(new DefaultAuthenticationChallengeHandler(this));

    // create a portal item using the portal and the item id of a map service
    PortalItem portalItem = new PortalItem(portal, "acc027394bc84c2fb04d1ed317aac674");

    // create a map with the portal item
    mOnlineMap = new ArcGISMap(portalItem);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // show the map
    mMapView.setMap(mOnlineMap);

    // create a graphics overlay to show the preplanned map areas extents (areas of interest)
    mAreasOfInterestGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mAreasOfInterestGraphicsOverlay);

    // create a red outline to mark the areas of interest of the preplanned map areas
    SimpleLineSymbol areaOfInterestLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0x80FF0000, 5.0f);
    // create simple renderer for areas of interest, and set it to use the line symbol
    SimpleRenderer areaOfInterestRenderer = new SimpleRenderer();
    areaOfInterestRenderer.setSymbol(areaOfInterestLineSymbol);
    mAreasOfInterestGraphicsOverlay.setRenderer(areaOfInterestRenderer);

    // create an offline map task for the portal item
    mOfflineMapTask = new OfflineMapTask(portalItem);

    // find the available preplanned map areas
    ListenableFuture<List<PreplannedMapArea>> preplannedMapAreasFuture = mOfflineMapTask.getPreplannedMapAreasAsync();
    preplannedMapAreasFuture.addDoneListener(() -> {
      try {
        // get the preplanned areas and add them to the list view
        mPreplannedMapAreas = preplannedMapAreasFuture.get();

        // create a list of pre planned map area names
        List<String> preplannedMapAreaNames = new ArrayList<>();
        // populate the list
        for (PreplannedMapArea preplannedMapArea : mPreplannedMapAreas) {
          preplannedMapAreaNames.add(preplannedMapArea.getPortalItem().getTitle());
        }

        // get a reference to the preplanned areas list view
        mPreplannedAreasListView = findViewById(R.id.availablePreplannedAreasListView);

        // add the list to the preplanned areas list view
        ListAdapter preplannedMapAreaListViewAdapter = new ArrayAdapter<>(this,
            R.layout.item_map_area, R.id.mapAreaTextView, preplannedMapAreaNames);
        mPreplannedAreasListView.setAdapter(preplannedMapAreaListViewAdapter);

        // load each item and get the area of interest
        for (PreplannedMapArea mapArea : mPreplannedMapAreas) {
          mapArea.loadAsync();
          mapArea.addDoneLoadingListener(() -> {
            if (mapArea.getLoadStatus() == LoadStatus.LOADED) {

              // create a graphic for the area of interest and add it to the graphics overlay
              mAreasOfInterestGraphicsOverlay.getGraphics().add(new Graphic(mapArea.getAreaOfInterest()));
            }
          });
        }

        // enable the download button
        mDownloadButton.setEnabled(true);

        // create a listener that changes the viewpoint to the extent of the preplanned map area when an item is selected in the list view
        mPreplannedAreasListView.setOnItemClickListener((parent, view, position, id) -> {
          // get the preplanned area at the tapped position
          mSelectedPreplannedMapArea = mPreplannedMapAreas.get(position);
          if (mSelectedPreplannedMapArea != null) {
            // show the online map
            mMapView.setMap(mOnlineMap);
            // get the extent of the area of interest, and add a buffer
            Envelope areaOfInterest = GeometryEngine.buffer(mSelectedPreplannedMapArea.getAreaOfInterest(), 100).getExtent();
            // set the point of view to the area of interest
            mMapView.setViewpointAsync(new Viewpoint(areaOfInterest), 1.50F);
          }
        });
      } catch (InterruptedException | ExecutionException e) {
        String error = "Failed to get the preplanned map areas from the offline map task: " + e.getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });

    // keep a list of which map's have already been downloade
    mDownloadedMapAreas = new ArrayList<>();
    // get a reference to the downloaded areas list view
    mDownloadedMapAreasListView = findViewById(R.id.downloadedMapAreasListView);
    // add the list to the preplanned areas list view
    mDownloadedMapAreasAdapter = new ArrayAdapter<>(this,
        R.layout.item_map_area, R.id.mapAreaTextView, new ArrayList<>());
    mDownloadedMapAreasListView.setAdapter(mDownloadedMapAreasAdapter);
    // create a listener that changes the viewpoint to the extent of the downloaded map area when an item is selected in the list view
    mDownloadedMapAreasListView.setOnItemClickListener((parent, view, position, id) -> {
        // get the preplanned area at the tapped position
      ArcGISMap downloadedMapArea = mDownloadedMapAreas.get(position);
        if (downloadedMapArea != null) {
          // show the online map
          mMapView.setMap(downloadedMapArea);
      }
  });
  }

  /**
   * Downloads and shows the selected preplanned map area.
   */
  private void downloadPreplannedMapArea() {

    // if a preplanned areas has been selected in the list view
    if (mSelectedPreplannedMapArea != null) {

      // create a folder path where the map package will be downloaded to
      File preplannedAreaFolder = new File(
          mOfflineMapDirectory + "/" + mSelectedPreplannedMapArea.getPortalItem().getTitle());

      // download the preplanned area if it has not been downloaded previously
      if (!preplannedAreaFolder.exists()) {

        // disable the download button
        mDownloadButton.setEnabled(false);

        // create download parameters from the task
        ListenableFuture<DownloadPreplannedOfflineMapParameters> downloadPreplannedOfflineMapParametersFuture = mOfflineMapTask
            .createDefaultDownloadPreplannedOfflineMapParametersAsync(mSelectedPreplannedMapArea);

        downloadPreplannedOfflineMapParametersFuture.addDoneListener(() -> {
          try {
            DownloadPreplannedOfflineMapParameters downloadPreplannedOfflineMapParameters = downloadPreplannedOfflineMapParametersFuture
                .get();
            // set the parameters for the offline map to not receive updates
            downloadPreplannedOfflineMapParameters.setUpdateMode(PreplannedUpdateMode.NO_UPDATES);
            // create the job with the parameters and download path
            DownloadPreplannedOfflineMapJob downloadPreplannedOfflineMapJob = mOfflineMapTask
                .downloadPreplannedOfflineMap(downloadPreplannedOfflineMapParameters, preplannedAreaFolder.getPath());
            // start the job and wait for it to complete
            downloadPreplannedOfflineMapJob.start();
            downloadPreplannedOfflineMapJob.addJobDoneListener(() -> {

              // hide the graphics overlay with the areas of interest
              mAreasOfInterestGraphicsOverlay.setVisible(false);
              // if the job succeeded
              if (downloadPreplannedOfflineMapJob.getStatus() == Job.Status.SUCCEEDED) {
                // get the result of the job
                DownloadPreplannedOfflineMapResult downloadPreplannedOfflineMapResult = downloadPreplannedOfflineMapJob
                    .getResult();
                // check if the result has any errors and display them
                checkForOfflineMapResultErrors(downloadPreplannedOfflineMapResult);
                // show the result in the map view
                mMapView.setMap(downloadPreplannedOfflineMapResult.getOfflineMap());
                // re-enable the download button
                mDownloadButton.setEnabled(true);
                // add the downloaded map to a list of downloaded map areas
                mDownloadedMapAreas.add(downloadPreplannedOfflineMapResult.getOfflineMap());
                mDownloadedMapAreasAdapter.add(downloadPreplannedOfflineMapResult.getOfflineMap().getItem().getTitle());
                mDownloadedMapAreasAdapter.notifyDataSetChanged();
                // display error details if the job fails
              } else if (downloadPreplannedOfflineMapJob.getStatus() == Job.Status.FAILED) {
                String error =
                    "Download Preplanned Offline Map Job failed: " + downloadPreplannedOfflineMapJob.getError()
                        .getMessage();
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                Log.e(TAG, error);
              }
            });
          } catch (InterruptedException | ExecutionException e) {
            String error =
                "Could not create Default Parameters for the Download Preplanned Offline Map Job: " + e.getMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
        // if the area is already downloaded, open it
      } else {
        // hide the graphics overlay with the areas of interest
        mAreasOfInterestGraphicsOverlay.setVisible(false);
        // open and load the mobile map package
        MobileMapPackage localMapArea = new MobileMapPackage(preplannedAreaFolder.getPath());
        localMapArea.loadAsync();
        // display the map from the mobile map package
        localMapArea.addDoneLoadingListener(() -> mMapView.setMap(localMapArea.getMaps().get(0)));
      }

    } else {
      String error = "No preplanned map area selected for downloading.";
      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Checks for layer and table errors of an offline map result, and displays them if present.
   *
   * @param downloadPreplannedOfflineMapResult the result to query for errors.
   */
  private void checkForOfflineMapResultErrors(DownloadPreplannedOfflineMapResult downloadPreplannedOfflineMapResult) {
    if (downloadPreplannedOfflineMapResult.hasErrors()) {
      // accumulate all layer and table errors into a single message
      StringBuilder stringBuilder = new StringBuilder("Errors: ");
      Map<Layer, ArcGISRuntimeException> layerErrors = downloadPreplannedOfflineMapResult.getLayerErrors();
      for (Map.Entry<Layer, ArcGISRuntimeException> error : layerErrors.entrySet()) {
        stringBuilder.append("Layer: ").append(error.getKey().getName()).append(". Exception: ")
            .append(error.getValue().getMessage()).append(". ");
      }
      Map<FeatureTable, ArcGISRuntimeException> tableErrors = downloadPreplannedOfflineMapResult.getTableErrors();
      for (Map.Entry<FeatureTable, ArcGISRuntimeException> error : tableErrors.entrySet()) {
        stringBuilder.append("Table: ").append(error.getKey().getTableName()).append(". Exception: ")
            .append(error.getValue().getMessage()).append(". ");
      }
      String error = "One or more errors occurred with the Offline Map Result: " + stringBuilder;
      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      Log.e(TAG, error);
    }
  }

  public static void deleteCache(Context context) {
    try {
      File directory = context.getCacheDir();
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
    deleteCache(getApplicationContext());
    super.onDestroy();
  }
}
