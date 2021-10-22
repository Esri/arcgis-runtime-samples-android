/*
 * Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.generateofflinemapwithlocalbasemap;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapJob;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapParameters;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapResult;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
        implements ProgressDialogFragment.OnProgressDialogDismissListener, LocalBasemapAlertDialogFragment.OnClickListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private Button mTakeMapOfflineButton;
  private GraphicsOverlay mGraphicsOverlay;
  private Graphic mDownloadArea;

  private GenerateOfflineMapJob mGenerateOfflineMapJob;
  private GenerateOfflineMapParameters mGenerateOfflineMapParameters;
  private String mLocalBasemapDirectory;
  private OfflineMapTask mOfflineMapTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required
    // to access basemaps and other location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // access button to take the map offline and disable it until a download area has been defined
    mTakeMapOfflineButton = findViewById(R.id.takeMapOfflineButton);
    mTakeMapOfflineButton.setEnabled(false);

    // create a portal item with the itemId of the web map
    Portal portal = new Portal(getString(R.string.portal_url), false);
    PortalItem portalItem = new PortalItem(portal, getString(R.string.item_id));

    // create a map with the portal item and set to the map view
    ArcGISMap map = new ArcGISMap(portalItem);
    map.addDoneLoadingListener(() -> {
      if (map.getLoadStatus() == LoadStatus.LOADED) {
        // limit the map scale to the largest layer scale
        map.setMaxScale(map.getOperationalLayers().get(6).getMaxScale());
        map.setMinScale(map.getOperationalLayers().get(6).getMinScale());
        // enable the take map offline button only after the map is loaded
        mTakeMapOfflineButton.setEnabled(true);
      } else {
        String error = "Map failed to load: " + map.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
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
      updateDownloadArea();
    });

    // when the button is clicked, start the offline map task job
    mTakeMapOfflineButton.setOnClickListener(v -> {

      updateDownloadArea();

      // specify the extent, min scale, and max scale as parameters
      double minScale = mMapView.getMapScale();
      double maxScale = mMapView.getMap().getMaxScale();
      // minScale must always be larger than maxScale
      if (minScale <= maxScale) {
        minScale = maxScale + 1;
      }

      // create an offline map task with the map
      mOfflineMapTask = new OfflineMapTask(mMapView.getMap());

      // create default generate offline map parameters
      ListenableFuture<GenerateOfflineMapParameters> generateOfflineMapParametersFuture = mOfflineMapTask
              .createDefaultGenerateOfflineMapParametersAsync(mDownloadArea.getGeometry(), minScale, maxScale);
      generateOfflineMapParametersFuture.addDoneListener(() -> {
        try {
          mGenerateOfflineMapParameters = generateOfflineMapParametersFuture.get();
          // set the path to the references basemap directory
          mGenerateOfflineMapParameters.setReferenceBasemapFilename(getString(R.string.naperville_tpkx));
          mGenerateOfflineMapParameters.setReferenceBasemapDirectory(getExternalFilesDir(null) + getString(R.string.naperville_tpkx));
          // name of local basemap file as supplied by the map's author
          String localBasemapFileName = mGenerateOfflineMapParameters.getReferenceBasemapFilename();
          // check if the offline map parameters include reference to a basemap file
          if (!localBasemapFileName.isEmpty()) {
            // search for the given file name in the app's scoped storage
            File localBasemapFile = searchForFile(getExternalFilesDir(null), localBasemapFileName);
            // if a file of the given name was found
            if (localBasemapFile != null) {
              // get the file's directory
              mLocalBasemapDirectory = localBasemapFile.getParent();
              showLocalBasemapAlertDialog(localBasemapFileName);
              Log.i(TAG, "Local basemap file found in: " + mLocalBasemapDirectory);
            } else {
              String error = "Local basemap file " + localBasemapFileName + " not found!";
              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
              Log.e(TAG, error);
            }
          } else {
            String message = "The map's author has not specified a local basemap";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            Log.i(TAG, message);
          }
        } catch (ExecutionException | InterruptedException e) {
          String error = "Error creating generate offline map parameters: " + e.getMessage();
          Toast.makeText(this, error, Toast.LENGTH_LONG).show();
          Log.e(TAG, error);
        }
      });
    });
  }

  /**
   * Function to update the download area on map viewpoint change
   * or on button click.
   */
  private void updateDownloadArea() {
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

  /**
   * Use the generate offline map job to generate an offline map.
   */
  private void generateOfflineMap() {

    // cancel previous job request
    if (mGenerateOfflineMapJob != null) {
      mGenerateOfflineMapJob.cancel();
    }

    mTakeMapOfflineButton.setEnabled(false);

    // delete any offline map already in the cache
    String tempDirectoryPath = getCacheDir() + File.separator + "offlineMap";
    deleteDirectory(new File(tempDirectoryPath));

    // create an offline map job with the download directory path and parameters and start the job
    mGenerateOfflineMapJob = mOfflineMapTask.generateOfflineMap(mGenerateOfflineMapParameters, tempDirectoryPath);

    // replace the current map with the result offline map when the job finishes
    mGenerateOfflineMapJob.addJobDoneListener(() -> {
      if (mGenerateOfflineMapJob.getStatus() == Job.Status.SUCCEEDED) {
        GenerateOfflineMapResult result = mGenerateOfflineMapJob.getResult();
        mMapView.setMap(result.getOfflineMap());
        mGraphicsOverlay.getGraphics().clear();
        findProgressDialogFragment().dismiss();
        Toast.makeText(this, "Now displaying offline map.", Toast.LENGTH_LONG).show();
      } else {
        String error = "Error in generate offline map job: " + mGenerateOfflineMapJob.getError().getAdditionalMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });

    // start the job
    mGenerateOfflineMapJob.start();

    if (findProgressDialogFragment() == null) {
      ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(
              getString(R.string.generate_offline_map_job_title),
              getString(R.string.taking_map_offline_message),
              getString(R.string.cancel)
      );
      progressDialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.class.getSimpleName());
    }

    // show the job's progress with the progress dialog
    mGenerateOfflineMapJob.addProgressChangedListener(() -> {
      if (findProgressDialogFragment() != null) {
        findProgressDialogFragment().setProgress(mGenerateOfflineMapJob.getProgress());
      }
    });
  }

  /**
   * Creates an alert notifying the user that a local basemap has been found on the device and asks whether the user
   * wishes to use that basemap, rather than download one with the rest of the generate offline map job.
   */
  private void showLocalBasemapAlertDialog(String localBasemapFileName) {
    if (getSupportFragmentManager().findFragmentByTag(LocalBasemapAlertDialogFragment.class.getSimpleName()) == null) {
      LocalBasemapAlertDialogFragment localBasemapAlertFragment = LocalBasemapAlertDialogFragment.newInstance(
              getString(R.string.local_basemap_found),
              getString(R.string.local_basemap_found_message, localBasemapFileName),
              getString(R.string.yes),
              getString(R.string.no)
      );
      localBasemapAlertFragment
              .show(getSupportFragmentManager(), LocalBasemapAlertDialogFragment.class.getSimpleName());
    }
  }

  /**
   * Callback from the local basemap alert dialog. Sets the reference basemap directory and calls generateOfflineMap().
   */
  @Override
  public void onPositiveClick() {
    // set the directory of the local base map to the parameters
    mGenerateOfflineMapParameters.setReferenceBasemapDirectory(mLocalBasemapDirectory);
    // call generate offline map with parameters which now contain a reference basemap directory
    generateOfflineMap();
  }

  /**
   * Callback from the local basemap alert dialog. Leaves the reference basemap directory empty and calls
   * generateOfflineMap().
   */
  @Override
  public void onNegativeClick() {
    // call generate offline map with parameters which contain an empty string for reference basemap directory
    generateOfflineMap();
  }

  /**
   * Find and return the progress dialog fragment.
   * @return the progress dialog fragment.
   */
  private ProgressDialogFragment findProgressDialogFragment() {
    return (ProgressDialogFragment) getSupportFragmentManager()
            .findFragmentByTag(ProgressDialogFragment.class.getSimpleName());
  }

  @Override
  public void onProgressDialogDismiss() {
    if (mGenerateOfflineMapJob != null) {
      mGenerateOfflineMapJob.cancel();
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
   * Recursively search the given file for the given file name.
   *
   * @param file     to search in
   * @param fileName to search for
   * @return the file being searched for or, of not found, null
   */
  private static File searchForFile(File file, String fileName) {
    if (file.isDirectory()) {
      File[] arr = file.listFiles();
      for (File f : arr) {
        File found = searchForFile(f, fileName);
        if (found != null)
          return found;
      }
    } else {
      if (file.getName().equals(fileName)) {
        return file;
      }
    }
    return null;
  }

  /**
   * Recursively deletes all files in the given directory.
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

