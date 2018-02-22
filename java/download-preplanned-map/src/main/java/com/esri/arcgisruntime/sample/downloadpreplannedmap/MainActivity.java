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

package com.esri.arcgisruntime.sample.downloadpreplannedmap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask;
import com.esri.arcgisruntime.tasks.offlinemap.DownloadPreplannedOfflineMapJob;
import com.esri.arcgisruntime.tasks.offlinemap.DownloadPreplannedOfflineMapResult;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask;
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedMapArea;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.OnItemClicked {

  private static final String TAG = MainActivity.class.getSimpleName();

  private static final String[] reqPermission = { Manifest.permission.WRITE_EXTERNAL_STORAGE };

  private MapView mMapView;
  private OfflineMapTask mOfflineMapTask;

  private ArrayList<PreplannedAreaPreview> mPreplannedAreaPreviews;
  private RecyclerView mRecyclerView;
  private Button mDeleteAreasButton;

  private String mLocalPreplannedMapDir;
  private List<PreplannedMapArea> mPreplannedAreas;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    requestPermissions();

    mMapView = findViewById(R.id.mapView);

    mDeleteAreasButton = findViewById(R.id.deleteAreasButton);
    mDeleteAreasButton.setOnClickListener(view -> deleteAllMapAreas());
  }

  public void populateDrawerWithThumbnailPreviews() {

    // setup recycler view
    mRecyclerView = findViewById(R.id.drawerRecyclerView);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    mRecyclerView.setLayoutManager(layoutManager);
    mPreplannedAreaPreviews = new ArrayList<>();
    RecyclerViewAdapter adapter = new RecyclerViewAdapter(mPreplannedAreaPreviews);
    mRecyclerView.setAdapter(adapter);

    adapter.setOnClick(this);

    // define the local path where the preplanned map will be stored
    mLocalPreplannedMapDir = getCacheDir().toString() + File.separator + getString(R.string.file_name);
    Log.d(TAG, mLocalPreplannedMapDir);

    // create portal that contains the portal item
    Portal portal = new Portal(getString(R.string.portal_url));

    // create a portal item based on a the portal item id and load it
    PortalItem portalItem = new PortalItem(portal, getString(R.string.portal_item_ID));
    portalItem.loadAsync();
    portalItem.addDoneLoadingListener(() -> {
      if (portalItem.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        Log.e(TAG, "Portal item failed to load: " + portalItem.getLoadError().getMessage());
        return;
      }

      // create an offline map task and load it
      mOfflineMapTask = new OfflineMapTask(portalItem);
      mOfflineMapTask.loadAsync();
      mOfflineMapTask.addDoneLoadingListener(() -> {
        if (mOfflineMapTask.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
          Log.e(TAG, "Error loading OfflineMapTask: " + mOfflineMapTask.getLoadError().getMessage());
          return;
        }

        // get related preplanned areas
        ListenableFuture<List<PreplannedMapArea>> preplannedAreasFuture = mOfflineMapTask.getPreplannedMapAreasAsync();
        preplannedAreasFuture.addDoneListener(() -> {
          try {
            // get the list of preplanned map areas
            mPreplannedAreas = preplannedAreasFuture.get();
            // load each area
            for (PreplannedMapArea preplannedMapArea : mPreplannedAreas) {
              preplannedMapArea.loadAsync();
              preplannedMapArea.addDoneLoadingListener(() -> {
                if (preplannedMapArea.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                  Log.e(TAG, "Map area failed to load: " + preplannedMapArea.getLoadError().getMessage());
                  return;
                }
                PreplannedAreaPreview preview = new PreplannedAreaPreview();
                preview.setTitle(preplannedMapArea.getPortalItem().getTitle());
                ListenableFuture<byte[]> thumbnailFuture = preplannedMapArea.getPortalItem().fetchThumbnailAsync();
                thumbnailFuture.addDoneListener(() -> {
                  try {
                    byte[] byteStream = thumbnailFuture.get();
                    Bitmap thumbnail = BitmapFactory.decodeByteArray(byteStream, 0, byteStream.length);
                    preview.setBitmapThumbnail(Bitmap.createScaledBitmap(thumbnail, 640, 480, true));
                    mPreplannedAreaPreviews.add(preview);
                    adapter.notifyDataSetChanged();
                  } catch (InterruptedException | ExecutionException e) {
                    Log.e(TAG, "Error fetching thumbnail: " + e.getMessage());
                  }
                });
              });
            }
          } catch (InterruptedException | ExecutionException e) {
            Toast.makeText(MainActivity.this, "Error loading preplanned areas: " + e.getMessage(), Toast.LENGTH_LONG)
                .show();
            Log.e(TAG, "Error loading preplanned areas: " + e.getMessage());
          }
        });
      });
    });
  }

  private void downloadMapArea(PreplannedMapArea mapArea) {
    String title = mapArea.getPortalItem().getTitle();

    // create folder path where the map area will be downloaded
    String path = mLocalPreplannedMapDir + File.separator + title;
    File file = new File(path);

    // if area has already been downloaded locally to the device, open it
    if (file.exists()) {
      MobileMapPackage localMapArea = new MobileMapPackage(path);
      localMapArea.loadAsync();
      localMapArea.addDoneLoadingListener(() -> mMapView.setMap(localMapArea.getMaps().get(0)));
      return;
    }

    // make the file directory
    file.mkdirs();

    // create the job used download the preplanned map and start
    DownloadPreplannedOfflineMapJob downloadPreplannedOfflineMapJob = mOfflineMapTask
        .downloadPreplannedOfflineMap(mapArea, path);

    // set up notification manager to show download progress
    startNotificationManager(downloadPreplannedOfflineMapJob, title);

    downloadPreplannedOfflineMapJob.start();
    downloadPreplannedOfflineMapJob.addJobDoneListener(() -> {
      if (downloadPreplannedOfflineMapJob.getStatus() == Job.Status.FAILED) {
        Log.e(TAG, "Offline map job failed: " + downloadPreplannedOfflineMapJob.getError());
        return;
      }

      // get result of downloaded area
      DownloadPreplannedOfflineMapResult results = downloadPreplannedOfflineMapJob.getResult();
      Log.d(TAG, "job path :" + downloadPreplannedOfflineMapJob.getDownloadDirectoryPath());
      // handle possible errors and show them to the user
      if (results.hasErrors()) {
        handleErrors(results);
      } else {
        // set the downloaded map to the view
        mMapView.setMap(results.getOfflineMap());
      }
    });
  }

  /**
   * Set up notification manager to show download progress.
   *
   * @param job the given DownloadPreplannedOfflineMapJob
   * @param title of the map area
   */
  private void startNotificationManager(DownloadPreplannedOfflineMapJob job, String title) {
    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default");
    notificationBuilder.setContentTitle(title).setContentText("Download in progress")
        .setSmallIcon(android.R.drawable.stat_sys_download);

    // update progress
    job.addProgressChangedListener(() -> {
      notificationBuilder.setProgress(100, job.getProgress(), false);
      notificationManager.notify(0, notificationBuilder.build());
    });

    //
    job.addJobDoneListener(() -> {
      if (job.getStatus() == Job.Status.SUCCEEDED) {
        notificationBuilder.setContentText("Download complete").setProgress(0, 0, false)
            .setSmallIcon(android.R.drawable.stat_sys_download_done);
        notificationManager.notify(0, notificationBuilder.build());
      } else {
        notificationBuilder.setContentText("Download failed").setProgress(0, 0, false)
            .setSmallIcon(android.R.drawable.stat_sys_warning);
        notificationManager.notify(0, notificationBuilder.build());
      }
    });
  }

  /**
   * Builds errors from results and displays them in the Log.
   *
   * @param results DownloadPreplannedOfflineMapResult
   */
  private void handleErrors(DownloadPreplannedOfflineMapResult results) {
    StringBuilder errorBuilder = new StringBuilder();
    for (Map.Entry<Layer, ArcGISRuntimeException> layerError : results.getLayerErrors().entrySet()) {
      errorBuilder.append(
          layerError.getKey() + " " + layerError.getValue().getMessage() + "\n" + layerError.getValue()
              .getCause() + "\n");
    }
    for (Map.Entry<FeatureTable, ArcGISRuntimeException> tableError : results.getTableErrors().entrySet()) {
      errorBuilder.append(
          tableError.getKey() + " " + tableError.getValue().getMessage() + "\n" + tableError.getValue()
              .getCause() + "\n");
    }
    // report error accessing a secured resource
    Log.e(TAG, String.valueOf(errorBuilder));
  }

  private void deleteAllMapAreas() {
    // Setup UI for downloading
    //downloadNotificationText.Visibility = Visibility.Collapsed;
    //progressBar.IsIndeterminate = true;
    //busyText.Text = "Deleting downloaded map area...";
    //busyIndicator.Visibility = Visibility.Visible;

    // If there is a map loaded to the MapView, remove it
    if (mMapView.getMap() != null) {
      mMapView.setMap(null);
    }

    unregisterAndDeleteAllAreas();
  }

  /**
   * Find all geodatabases from all the downloaded map areas, unregister them then recreate temporary data folder.
   * <p>
   * When area is downloaded, geodatabases gets registered with the original service to support syncronization. When the
   * area is deleted from the device, it is important first to unregister all geodatabases that are used in the map so
   * the service doesn't have a stray geodatabases registered.
   */
  private void unregisterAndDeleteAllAreas() {
    // find all geodatabase files from the map areas by extension

    File localPreplannedMapDirFile = new File(mLocalPreplannedMapDir);

    List<String> geodatabaseToUnregister = findGeodatabases(localPreplannedMapDirFile.listFiles());

    // unregister all geodatabases
    for (String geodatabasePath : geodatabaseToUnregister) {
      Geodatabase geodatabase = new Geodatabase(geodatabasePath);
      geodatabase.addDoneLoadingListener(() -> {
        GeodatabaseSyncTask geodatabaseSyncTask = new GeodatabaseSyncTask(geodatabase.getServiceUrl());
        geodatabaseSyncTask.addDoneLoadingListener(() -> {
          ListenableFuture<Void> unregisterGeodatabaseAsync = geodatabaseSyncTask.unregisterGeodatabaseAsync(geodatabase);
          unregisterGeodatabaseAsync.addDoneListener(() -> geodatabaseSyncTask.addDoneLoadingListener(geodatabase::close));
        });
      });
    }
    // delete all data from the temporary data folder
    deleteAll(localPreplannedMapDirFile);
  }

  /**
   * Find all files of type *.geodatabase.
   *
   * @param files as array
   * @return list of strings which are paths to .geodatabase files on the device
   */
  private List<String> findGeodatabases(File[] files) {
    List<String> geodatabasesToUnregister = new ArrayList<>();
    for (File f : files) {
      if (f.isDirectory()) {
        findGeodatabases(f.listFiles());
      } else if (f.isFile() && f.getAbsolutePath().endsWith(".geodatabase")) {
        geodatabasesToUnregister.add(f.getAbsolutePath());
      }
    }
    return geodatabasesToUnregister;
  }

  /**
   * Delete all files recursively.
   *
   * @param fileOrDirectory to delete
   */
  private void deleteAll(File fileOrDirectory) {
    if (fileOrDirectory.isDirectory()) {
      for (File child : fileOrDirectory.listFiles()) {
        deleteAll(child);
      }
      fileOrDirectory.delete();
    }
  }

  @Override public void onItemClick(int position) {
    downloadMapArea(mPreplannedAreas.get(position));
  }

  /**
   * Request permissions at runtime.
   */
  private void requestPermissions() {
    int requestCode = 1;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this,
        reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      populateDrawerWithThumbnailPreviews();
    } else {
      // request permission
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      populateDrawerWithThumbnailPreviews();
    } else {
      // report to user that permission was denied
      Toast.makeText(MainActivity.this, getResources().getString(R.string.write_permission_denied), Toast.LENGTH_SHORT)
          .show();
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

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }
}
