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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

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

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a map view
    mapView = new MapView();

    // create progress indicator
    ProgressIndicator progressIndicator = new ProgressIndicator();
    progressIndicator.setVisible(false);

    // create a button to update the offline map
    Button applyUpdatesButton = new Button("Apply Scheduled Updates");
    applyUpdatesButton.setDisable(true);

    // create labels to show update availability and size
    Label updateAvailableLabel = new Label("Updates: ");
    updateAvailableLabel.setTextFill(Color.WHITE);
    Label updateSizeLabel = new Label("Update size: ");
    updateSizeLabel.setTextFill(Color.WHITE);

    // create a control panel for the UI elements
    VBox controlsVBox = new VBox(6);
    controlsVBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("rgba(0,0,0,0.3)"), CornerRadii.EMPTY,
        Insets.EMPTY)));
    controlsVBox.setPadding(new Insets(10.0));
    controlsVBox.setMaxSize(180, 110);
    controlsVBox.getChildren().addAll(applyUpdatesButton, updateAvailableLabel, updateSizeLabel);

    // create a temporary copy of the local offline map files, so that updating does not overwrite them permanently
    Path tempMobileMapPackageDirectory = Files.createTempDirectory("canyonlands_offline_map");
    tempMobileMapPackageDirectory.toFile().deleteOnExit();
    Path sourceDirectory = Paths.get("./samples-data/canyonlands/");
    FileUtils.copyDirectory(sourceDirectory.toFile(), tempMobileMapPackageDirectory.toFile());

    // load the offline map as a mobile map package
    MobileMapPackage mobileMapPackage = new MobileMapPackage(tempMobileMapPackageDirectory.toString());
    mobileMapPackage.loadAsync();
    mobileMapPackage.addDoneLoadingListener(() -> {
      if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED && !mobileMapPackage.getMaps().isEmpty()) {

        // add the map from the mobile map package to the map view
        ArcGISMap offlineMap = mobileMapPackage.getMaps().get(0);
        mMapView.setMap(offlineMap);

        // show progress indicator
        progressIndicator.setVisible(true);

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
              updateAvailableLabel.setText("Updates: AVAILABLE");

              // check and show update size
              long updateSize = offlineMapUpdatesInfo.getScheduledUpdatesDownloadSize();
              updateSizeLabel.setText("Update size: " + updateSize + " bytes.");

              // hide the progress indicator
              progressIndicator.setVisible(false);

              // enable the 'Apply Scheduled Updates' button
              applyUpdatesButton.setDisable(false);
              // when the button is clicked, synchronize the mobile map package
              applyUpdatesButton.setOnAction(e -> {

                // show progress indicator
                progressIndicator.setVisible(true);

                // create default parameters for the sync task
                ListenableFuture<OfflineMapSyncParameters> offlineMapSyncParametersFuture = offlineMapSyncTask.createDefaultOfflineMapSyncParametersAsync();
                offlineMapSyncParametersFuture.addDoneListener(() -> {
                  try {
                    OfflineMapSyncParameters offlineMapSyncParameters = offlineMapSyncParametersFuture.get();

                    // set the sync direction to none, since we only want to update
                    offlineMapSyncParameters.setSyncDirection(SyncGeodatabaseParameters.SyncDirection.NONE);
                    // set the parameters to download all updates for the mobile map packages
                    offlineMapSyncParameters.setPreplannedScheduledUpdatesOption(PreplannedScheduledUpdatesOption.DOWNLOAD_ALL_UPDATES);
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
                            if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED && !mobileMapPackage.getMaps().isEmpty()) {

                              // add the map from the mobile map package to the map view
                              mapView.setMap(mobileMapPackage.getMaps().get(0));

                            } else {
                              new Alert(Alert.AlertType.ERROR, "Failed to load the mobile map package.").show();
                            }
                          });
                        }

                        // update labels
                        updateAvailableLabel.setText("Updates: Up to date");
                        updateSizeLabel.setText("Update size: N/A");

                      } else {
                        new Alert(Alert.AlertType.ERROR, "Error syncing the offline map: " + offlineMapSyncJob.getError().getMessage()).show();
                      }

                      // disable the 'Apply Scheduled Updates' button
                      applyUpdatesButton.setDisable(true);
                      // hide progress indicator
                      progressIndicator.setVisible(false);

                    });
                  } catch (InterruptedException | ExecutionException ex) {
                    new Alert(Alert.AlertType.ERROR, "Error creating DefaultOfflineMapSyncParameters" + ex.getMessage()).show();
                  }
                });
              });

            } else {
              updateAvailableLabel.setText("Updates: NOT AVAILABLE");
            }
          } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error checking for Scheduled Updates Availability: " + e.getMessage()).show();
          }
        });
      } else {
        new Alert(Alert.AlertType.ERROR, "Failed to load the mobile map package.").show();
      }
    });
  }
}
