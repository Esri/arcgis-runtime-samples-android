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
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.tasks.offlinemap.DownloadPreplannedOfflineMapJob;
import com.esri.arcgisruntime.tasks.offlinemap.DownloadPreplannedOfflineMapResult;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask;
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedMapArea;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.OnItemClicked {

  String TAG = MainActivity.class.getSimpleName();
  private MapView mMapView;
  private ArrayList<PreplannedAreaPreview> mPreplannedAreaPreviews;
  private RecyclerView mRecyclerView;
  private OfflineMapTask mOfflineMapTask;
  private String mLocalPreplannedMapDir;
  private List<PreplannedMapArea> mPreplannedAreas;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    requestPermission(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE });

    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(Basemap.createLightGrayCanvas());
    mMapView.setMap(map);

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

    file.mkdirs();

    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default");
    notificationBuilder.setContentTitle(title).setContentText("Download in progress")
        .setSmallIcon(android.R.drawable.stat_sys_download);

    // create the job used download the preplanned map and start
    DownloadPreplannedOfflineMapJob downloadPreplannedOfflineMapJob = mOfflineMapTask.downloadPreplannedOfflineMap(mapArea, path);

    // update progress
    downloadPreplannedOfflineMapJob.addProgressChangedListener(() -> {
      notificationBuilder.setProgress(100, downloadPreplannedOfflineMapJob.getProgress(), false);
      notificationManager.notify(0, notificationBuilder.build());
    });

    downloadPreplannedOfflineMapJob.addJobDoneListener(() -> {
      notificationBuilder.setContentText("Download complete").setProgress(0, 0, false).setSmallIcon(android.R.drawable.stat_sys_download_done);
      notificationManager.notify(0, notificationBuilder.build());
    });

    downloadPreplannedOfflineMapJob.start();
    downloadPreplannedOfflineMapJob.addJobDoneListener(() -> {
      if (downloadPreplannedOfflineMapJob.getStatus() == Job.Status.FAILED) {
        Log.e(TAG, "Offline map job failed: " + downloadPreplannedOfflineMapJob.getError());
        return;
      }
      // get result of downloaded area
      DownloadPreplannedOfflineMapResult results = downloadPreplannedOfflineMapJob.getResult();
      Log.d(TAG, "job path :" + downloadPreplannedOfflineMapJob.getDownloadDirectoryPath());
      // Handle possible errors and show them to the user
      if (results.hasErrors()) {
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
        // Report error accessing a secured resource
        Log.e(TAG, String.valueOf(errorBuilder));
      }
      // Set the downloaded map to the view
      mMapView.setMap(results.getOfflineMap());
    });
  }

  @Override public void onItemClick(int position) {
    downloadMapArea(mPreplannedAreas.get(position));
    Log.d(TAG, String.valueOf(mPreplannedAreas.get(position).getLoadStatus()));
  }

  /**
   * Request permissions at runtime.
   */
  private void requestPermission(String[] reqPermission) {
    int requestCode = 2;
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

}
