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

package com.esri.arcgisruntime.sample.generateofflinemap;

import java.io.File;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
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
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private ArcGISMap mMap;
  private OfflineMapTask mOfflineMapTask;
  private GenerateOfflineMapParameters mOfflineMapParameters;
  private GraphicsOverlay mGraphicsOverlay;
  private SimpleLineSymbol mBoundarySymbol;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // request write permissions
    getWritePermission();

    // define authentication challenge handler to prompt user for username and password
    DefaultAuthenticationChallengeHandler handler = new DefaultAuthenticationChallengeHandler(this);
    AuthenticationManager.setAuthenticationChallengeHandler(handler);
    // get the portal url for ArcGIS Online
    Portal portal = new Portal(getResources().getString(R.string.portal_url), true);
    // get the pre-defined portal id and portal url
    PortalItem portalItem = new PortalItem(portal, getString(R.string.portal_key));

    //instantiate offline map task
    mOfflineMapTask = new OfflineMapTask(portalItem);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map from a PortalItem
    mMap = new ArcGISMap(portalItem);
    // set the map to be displayed in this view
    mMapView.setMap(mMap);

    // create graphic boundary from the extent and add it to the graphics overlay
    mGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
    mBoundarySymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5);

    // button to trigger takeMapOffLine()
    Button generateOfflineMapButton = findViewById(R.id.generateOfflineMapButton);
    generateOfflineMapButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        takeMapOffline();
      }
    });
  }

  /**
   * Creates parameters for offline map job using current view as extent. Then runs the job and sets the job result
   * map to the map view.
   */
  private void takeMapOffline() {
    if (mMap.getLoadStatus() == LoadStatus.LOADED) {

      // use current visible area as extent
      Envelope extent = mMapView.getVisibleArea().getExtent();

      // draw boundary around the extent used in the offline map parameters
      showBoundary(extent);

      // clear prior version of the local geodatabase
      clearDownloadDirectory();

      // define generate offline map parameters using the extent defined above
      final ListenableFuture<GenerateOfflineMapParameters> parametersFuture = mOfflineMapTask
          .createDefaultGenerateOfflineMapParametersAsync(extent);

      // wait for the offline map task to be done
      parametersFuture.addDoneListener(new Runnable() {
        @Override public void run() {
          try {
            // get the generated parameters
            mOfflineMapParameters = parametersFuture.get();

            // define the job with parameters and local storage location
            final GenerateOfflineMapJob generateOfflineMapJob = mOfflineMapTask
                .generateOfflineMap(mOfflineMapParameters,
                    Environment.getExternalStorageDirectory() + getString(R.string.offline_map_local_directory)
                        + getString(R.string.file_name));
            // start the job
            generateOfflineMapJob.start();

            // create a progress dialog to show progress of the generate offline map job
            final ProgressDialog progressDialog = createProgressDialog(generateOfflineMapJob);

            generateOfflineMapJob.addJobDoneListener(new Runnable() {
              @Override public void run() {
                if (generateOfflineMapJob.getStatus() == Job.Status.SUCCEEDED) {
                  // dismiss the progress dialog
                  progressDialog.dismiss();
                  // get the offline map from the offline map job
                  ArcGISMap offlineMap = generateOfflineMapJob.getResult().getOfflineMap();
                  // replace the current map with the offline map
                  mMapView.setMap(offlineMap);
                } else {
                  Log.e(TAG, "Could not generate offline map : " + generateOfflineMapJob.getError());
                }
              }
            });
          } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Error generating offline map parameters: " + e.getMessage());
            Toast.makeText(MainActivity.this, "Error generating offline map parameters: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
          }
        }
      });
    } else {
      Log.e(TAG, "Cannot generate offline map until map had loaded.");
      Toast.makeText(MainActivity.this, "Cannot generate offline map until map had loaded.", Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Clears any graphics from the graphics overlay and adds a new boundary symbol.
   *
   * @param extent of area chosen to generate an offline map.
   */
  private void showBoundary(Envelope extent) {
    // clear any previous graphics
    mGraphicsOverlay.getGraphics().clear();
    Graphic boundary = new Graphic(extent, mBoundarySymbol);
    mGraphicsOverlay.getGraphics().add(boundary);
  }

  /**
   * Creates a progress dialog for showing progress of the generate offline map job.
   *
   * @param generateOfflineMapJob instance of the job.
   * @return reference to the progress dialog.
   */
  private ProgressDialog createProgressDialog(final GenerateOfflineMapJob generateOfflineMapJob) {
    // create a dialog to show progress of the generate offline map job
    final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
    progressDialog.setTitle("Running generate offline map job");
    progressDialog.setIndeterminate(false);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setMax(100);
    progressDialog.setCancelable(false);
    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // dismiss the progress dialog
        dialog.dismiss();
        // cancel the generate offline map job
        generateOfflineMapJob.cancel();
      }
    });
    progressDialog.show();

    // update progress from the offline map job
    generateOfflineMapJob.addProgressChangedListener(new Runnable() {
      @Override public void run() {
        progressDialog.setProgress(generateOfflineMapJob.getProgress());
      }
    });

    return progressDialog;
  }

  /**
   * Create a local directory on the device for storage of the offline map (.geodatabase) generated in the job.
   * Deletes any old versions of the .geodatabase, if required.
   */
  private void clearDownloadDirectory() {
    // create directory for file at .../ArcGIS/Samples/OfflineMap/
    File offlineMapDirectory = new File(Environment.getExternalStorageDirectory(),
        getString(R.string.offline_map_local_directory));
    if (!offlineMapDirectory.exists()) {
      boolean dirCreated = offlineMapDirectory.mkdirs();
      if (dirCreated) {
        Log.i(TAG, "Local OfflineMap directory created.");
      } else {
        Log.e(TAG, "Error creating local OfflineMap directory.");
      }
    } else {
      Log.i(TAG, "No local OfflineMap directory created, one already exists.");
      File napervilleWaterNetworkFile = new File(
          Environment.getExternalStorageDirectory() + getString(R.string.offline_map_local_directory),
          getString(R.string.file_name));
      Log.i(TAG, "Local offline map stored at: " + napervilleWaterNetworkFile.getAbsolutePath());
      if (napervilleWaterNetworkFile.exists()) {
        deleteAll(napervilleWaterNetworkFile);
      }
    }
  }

  /**
   * Recursively deletes all files if the file is a directory.
   *
   * @param file or directory.
   */
  private void deleteAll(File file) {
    if (file.isDirectory())
      for (File subFile : file.listFiles())
        deleteAll(subFile);

    if (file.delete()) {
      Log.i(TAG, "File deleted in offline map geodatabase.");
    }
  }

  /**
   * Request write external storage permission at runtime from user for API 23+.
   */
  private void getWritePermission() {// define permission to request
    String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this, reqPermission[0]) != PackageManager.PERMISSION_GRANTED) {
      // request permission
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }
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
