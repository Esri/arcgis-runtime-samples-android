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

package com.esri.arcgisruntime.sample.generateofflinemapwithlocalbasemap;

import java.io.File;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

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
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapJob;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapParameters;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapResult;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private Button mTakeMapOfflineButton;
  private GraphicsOverlay mGraphicsOverlay;
  private Graphic mDownloadArea;

  private boolean mShouldUseLocalBasemap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // access button to take the map offline and disable it until map is loaded
    mTakeMapOfflineButton = findViewById(R.id.takeMapOfflineButton);
    mTakeMapOfflineButton.setEnabled(false);

    requestWritePermission();

    // handle authentication with the portal
    AuthenticationManager.setAuthenticationChallengeHandler(new DefaultAuthenticationChallengeHandler(this));

    // create a portal item with the itemId of the web map
    Portal portal = new Portal(getString(R.string.portal_url), false);
    PortalItem portalItem = new PortalItem(portal, getString(R.string.item_id));

    // create a map with the portal item
    ArcGISMap map = new ArcGISMap(portalItem);
    map.addDoneLoadingListener(() -> {
      if (map.getLoadStatus() == LoadStatus.LOADED) {

        // enable the map offline button only after the map is loaded
        mTakeMapOfflineButton.setEnabled(true);

        // limit the map scale to the largest layer scale
        map.setMaxScale(map.getOperationalLayers().get(6).getMaxScale());
        map.setMinScale(map.getOperationalLayers().get(6).getMinScale());
      } else {
        String error = "Map failed to load: " + map.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });

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
  }

  /**
   * Use the generate offline map job to generate an offline map.
   */
  private void setupOfflineMapTaskAndGenerateOfflineMapParameters() {

    // when the button is clicked, start the offline map task job
    mTakeMapOfflineButton.setOnClickListener(v -> {

      // specify the extent, min scale, and max scale as parameters
      double minScale = mMapView.getMapScale();
      double maxScale = mMapView.getMap().getMaxScale();
      // minScale must always be larger than maxScale
      if (minScale <= maxScale) {
        minScale = maxScale + 1;
      }

      // create an offline map task with the map
      OfflineMapTask offlineMapTask = new OfflineMapTask(mMapView.getMap());

      // create default generate offline map parameters
      ListenableFuture<GenerateOfflineMapParameters> generateOfflineMapParametersFuture = offlineMapTask
          .createDefaultGenerateOfflineMapParametersAsync(mDownloadArea.getGeometry(), minScale, maxScale);
      generateOfflineMapParametersFuture.addDoneListener(() -> {
        try {
          GenerateOfflineMapParameters generateOfflineMapParameters = generateOfflineMapParametersFuture.get();
          // define the samples directory file
          File samplesDirectory = new File(
              Environment.getExternalStorageDirectory() + getString(R.string.samples_directory));
          // name of local basemap file as supplied by the map's author
          String localBasemapFileName = generateOfflineMapParameters.getReferenceBasemapFilename();
          // check if the offline map parameters include reference to a basemap file
          if (!localBasemapFileName.isEmpty()) {
            // search for the given file name (in this case, in the ArcGIS/Samples directory)
            File localBasemapFile = searchForFile(samplesDirectory, localBasemapFileName);
            // if a file of the given name was found
            if (localBasemapFile != null) {
              // get the file's directory
              String localBasemapDirectory = localBasemapFile.getParent();
              AlertDialog.Builder localDialogBuilder = showLocalBasemapDialog(localBasemapFileName);
              localDialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                // set the directory of the local base map to the parameters
                generateOfflineMapParameters.setReferenceBasemapDirectory(localBasemapDirectory);
                // call generate offline map with parameters which now contain a reference basemap directory
                generateOfflineMap(offlineMapTask, generateOfflineMapParameters);
              });
              localDialogBuilder.setNegativeButton("NO",
                  (dialog, which) -> {
                    // call generate offline map with parameters which contain an empty string for reference basemap directory
                    generateOfflineMap(offlineMapTask, generateOfflineMapParameters);
                  });
              localDialogBuilder.show();
              Log.i(TAG, "Local basemap file found in: " + localBasemapDirectory);
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

  private void generateOfflineMap(OfflineMapTask offlineMapTask, GenerateOfflineMapParameters generateOfflineMapParameters) {

    // create a progress dialog to show download progress
    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Generate Offline Map Job");
    progressDialog.setMessage("Taking map offline...");
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setIndeterminate(false);
    progressDialog.setProgress(0);
    progressDialog.show();

    // delete any offline map already in the cache
    String tempDirectoryPath = getCacheDir() + File.separator + "offlineMap";
    deleteDirectory(new File(tempDirectoryPath));

    // create an offline map job with the download directory path and parameters and start the job
    GenerateOfflineMapJob job = offlineMapTask
        .generateOfflineMap(generateOfflineMapParameters, tempDirectoryPath);

    // replace the current map with the result offline map when the job finishes
    job.addJobDoneListener(() -> {
      if (job.getStatus() == Job.Status.SUCCEEDED) {
        GenerateOfflineMapResult result = job.getResult();
        mMapView.setMap(result.getOfflineMap());
        mGraphicsOverlay.getGraphics().clear();
        mTakeMapOfflineButton.setEnabled(false);
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
  }

  private AlertDialog.Builder showLocalBasemapDialog(String localBasemapFileName) {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setTitle("Local basemap found on the device");
    alertDialogBuilder.setMessage("The local basemap file " + localBasemapFileName
        + " was found on the device. Would you like to use the local file instead of an online basemap?");
    return alertDialogBuilder;
  }

  /**
   *
   */
  private void requestWritePermission() {
    // request write permission
    String[] reqPermission = { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // for API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      setupOfflineMapTaskAndGenerateOfflineMapParameters();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      setupOfflineMapTaskAndGenerateOfflineMapParameters();
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

