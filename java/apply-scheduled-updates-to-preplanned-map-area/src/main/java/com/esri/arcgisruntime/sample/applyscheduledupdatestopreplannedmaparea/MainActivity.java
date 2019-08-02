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

package com.esri.arcgisruntime.sample.applyscheduledupdatestopreplannedmaparea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.geodatabase.SyncGeodatabaseParameters;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapSyncJob;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapSyncParameters;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapSyncResult;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapSyncTask;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapUpdatesInfo;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineUpdateAvailability;
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedScheduledUpdatesOption;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private File mCopyOfMmpk;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a map view
    mMapView = findViewById(R.id.mapView);

    // this is the original mmpk, not updated by the scheduled update
    File originalMmpk = new File(Environment.getExternalStorageDirectory() + "/ArcGIS/Samples/MapPackage/canyonlands");
    // copy of the mmpk file which will have the update applied to it
    mCopyOfMmpk = new File(getCacheDir() + "/canyonlands");

    try {
      // copy the original mmpk into the cache, overwriting there's already a copy of the mmpk there
      copyDirectory(originalMmpk, mCopyOfMmpk);
    } catch (IOException e) {
      Log.e(TAG, "Error copying MMPK file: " + e.getMessage());
    }

    requestReadPermission();
  }

  private void applyScheduledUpdate() {
    // get a reference to the UI views
    TextView updateAvailableTextView = findViewById(R.id.updateAvailableTextView);
    TextView updateSizeTextView = findViewById(R.id.updateSizeTextView);
    Button applyScheduledUpdatesButton = findViewById(R.id.applyScheduledUpdatesButton);

    // load the offline map as a mobile map package
    MobileMapPackage mobileMapPackage = new MobileMapPackage(mCopyOfMmpk.getPath());
    mobileMapPackage.loadAsync();
    mobileMapPackage.addDoneLoadingListener(() -> {
      if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED && !mobileMapPackage.getMaps().isEmpty()) {
        // add the map from the mobile map package to the map view
        ArcGISMap offlineMap = mobileMapPackage.getMaps().get(0);
        mMapView.setMap(offlineMap);
        // create an offline map sync task with the preplanned area
        OfflineMapSyncTask offlineMapSyncTask = new OfflineMapSyncTask(offlineMap);
        // check for updates to the offline map
        ListenableFuture<OfflineMapUpdatesInfo> offlineMapUpdatesInfoFuture = offlineMapSyncTask.checkForUpdatesAsync();
        offlineMapUpdatesInfoFuture.addDoneListener(() -> {
          try {
            // get and check the results
            OfflineMapUpdatesInfo offlineMapUpdatesInfo = offlineMapUpdatesInfoFuture.get();

            // update UI for available updates
            if (offlineMapUpdatesInfo.getDownloadAvailability() == OfflineUpdateAvailability.AVAILABLE) {
              updateAvailableTextView.setText("Updates: " + OfflineUpdateAvailability.AVAILABLE.name());
              // check and show update size
              updateSizeTextView.setText("Update size: " + offlineMapUpdatesInfo.getScheduledUpdatesDownloadSize() + " bytes.");
              // enable the 'Apply Scheduled Updates' button
              applyScheduledUpdatesButton.setEnabled(true);
              // when the button is clicked, synchronize the mobile map package
              applyScheduledUpdatesButton.setOnClickListener(v -> {
                // create default parameters for the sync task
                ListenableFuture<OfflineMapSyncParameters> offlineMapSyncParametersFuture = offlineMapSyncTask
                    .createDefaultOfflineMapSyncParametersAsync();
                offlineMapSyncParametersFuture.addDoneListener(() -> {
                  try {
                    OfflineMapSyncParameters offlineMapSyncParameters = offlineMapSyncParametersFuture.get();
                    // set the sync direction to none, since we only want to update
                    offlineMapSyncParameters.setSyncDirection(SyncGeodatabaseParameters.SyncDirection.NONE);
                    // set the parameters to download all updates for the mobile map packages
                    offlineMapSyncParameters
                        .setPreplannedScheduledUpdatesOption(PreplannedScheduledUpdatesOption.DOWNLOAD_ALL_UPDATES);
                    // set the map package to rollback to the old state should the sync job fail
                    offlineMapSyncParameters.setRollbackOnFailure(true);
                    // create a sync job using the parameters
                    OfflineMapSyncJob offlineMapSyncJob = offlineMapSyncTask.syncOfflineMap(offlineMapSyncParameters);
                    // start the job and get the results
                    offlineMapSyncJob.start();
                    offlineMapSyncJob.addJobDoneListener(() -> {
                      if (offlineMapSyncJob.getStatus() == Job.Status.SUCCEEDED) {
                        OfflineMapSyncResult offlineMapSyncResult = offlineMapSyncJob.getResult();
                        // if mobile map package reopen is required, close the existing mobile map package and load it again
                        if (offlineMapSyncResult.isMobileMapPackageReopenRequired()) {
                          mobileMapPackage.close();
                          mobileMapPackage.loadAsync();
                          mobileMapPackage.addDoneLoadingListener(() -> {
                            if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED && !mobileMapPackage.getMaps()
                                .isEmpty()) {
                              // add the map from the mobile map package to the map view
                              mMapView.setMap(mobileMapPackage.getMaps().get(0));
                            } else {
                              String error =
                                  "Failed to load mobile map package: " + mobileMapPackage.getLoadError().getMessage();
                              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                              Log.e(TAG, error);
                            }
                          });
                        }
                        // check if map is up to date against the server. This is not required, since in most cases,
                        // you'll be confident the update was applied by virtue of the offline map sync job completing
                        // successfully
                        ListenableFuture<OfflineMapUpdatesInfo> offlineMapsUpdateInfoAfterUpdateFuture = offlineMapSyncTask.checkForUpdatesAsync();
                        offlineMapsUpdateInfoAfterUpdateFuture.addDoneListener(() -> {
                          try {
                            OfflineMapUpdatesInfo offlineMapsUpdatesInfoAfterUpdate = offlineMapsUpdateInfoAfterUpdateFuture.get();
                            updateAvailableTextView.setText("Updates: " + offlineMapsUpdatesInfoAfterUpdate.getDownloadAvailability().name());
                            if (offlineMapsUpdatesInfoAfterUpdate.getDownloadAvailability() != OfflineUpdateAvailability.NONE) {
                              // server still reports that updates are available
                              updateSizeTextView.setText("Update size: " + offlineMapsUpdatesInfoAfterUpdate.getScheduledUpdatesDownloadSize() + " bytes.");
                              applyScheduledUpdatesButton.setEnabled(true);
                            } else {
                              // no updates available
                              updateSizeTextView.setText("Update size: N/A");
                              applyScheduledUpdatesButton.setEnabled(false);
                            }
                          } catch (Exception e) {
                            String error = "Error checking for Scheduled Updates Availability: " + e.getMessage();
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                            Log.e(TAG, error);
                          }
                        });
                      } else {
                        String error = "Error syncing the offline map: " + offlineMapSyncJob.getError().getMessage();
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        Log.e(TAG, error);
                      }
                      // disable the 'Apply Scheduled Updates' button
                      applyScheduledUpdatesButton.setEnabled(false);
                    });
                  } catch (InterruptedException | ExecutionException ex) {
                    String error = "Error creating DefaultOfflineMapSyncParameters" + ex.getMessage();
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, error);
                  }
                });
              });

            } else {
              updateAvailableTextView.setText("Updates: NOT AVAILABLE");
            }
          } catch (Exception e) {
            String error = "Error checking for Scheduled Updates Availability: " + e.getMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
      } else {
        String error = "Failed to load the mobile map package: " + mobileMapPackage.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      applyScheduledUpdate();
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
      applyScheduledUpdate();
    } else {
      Toast.makeText(this, getString(R.string.canyonlands_mmpk_read_permission_denied), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

  public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
    if (sourceLocation.isDirectory()) {
      if (!targetLocation.exists()) {
        targetLocation.mkdirs();
      }
      String[] children = sourceLocation.list();
      for (int i = 0; i < children.length; i++) {
        copyDirectory(new File(sourceLocation, children[i]), new File(
            targetLocation, children[i]));
      }
    } else {
      InputStream in = new FileInputStream(sourceLocation);
      OutputStream out = new FileOutputStream(targetLocation);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
    }
  }
}
