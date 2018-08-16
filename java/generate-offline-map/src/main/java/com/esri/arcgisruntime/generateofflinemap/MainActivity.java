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

package com.esri.arcgisruntime.generateofflinemap;

import java.io.File;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.Job;
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = findViewById(R.id.mapView);

    // define permission to request
    String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // for API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      generateOfflineMap();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Use generate offline map job to generate an offline map.
   */
  private void generateOfflineMap() {

    // create a button to take the map offline
    final Button takeMapOfflineButton = findViewById(R.id.takeMapOfflineButton);
    takeMapOfflineButton.setEnabled(false);

    // handle authentication with the portal
    AuthenticationManager.setAuthenticationChallengeHandler(new DefaultAuthenticationChallengeHandler(this));

    // create a portal item with the itemId of the web map
    Portal portal = new Portal(getString(R.string.portal_url), true);
    PortalItem portalItem = new PortalItem(portal, getString(R.string.item_id));

    // create a map with the portal item
    ArcGISMap map = new ArcGISMap(portalItem);
    map.addDoneLoadingListener(() -> {
      // enable the button when the map is loaded
      if (map.getLoadStatus() == LoadStatus.LOADED) {
        takeMapOfflineButton.setEnabled(true);
      }
    });

    // set the map to the map view
    mMapView.setMap(map);

    // create a graphics overlay for the map view
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // create a graphic to show a box around the extent we want to download
    Graphic downloadArea = new Graphic();
    graphicsOverlay.getGraphics().add(downloadArea);
    SimpleLineSymbol simpleLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2);
    downloadArea.setSymbol(simpleLineSymbol);

    // update the box whenever the viewpoint changes
    mMapView.addViewpointChangedListener(viewpointChangedEvent -> {
      if (map.getLoadStatus() == LoadStatus.LOADED) {
        // upper left corner of the area to take offline
        android.graphics.Point minScreenPoint = new android.graphics.Point(200, 200);
        // lower right corner of the downloaded area
        android.graphics.Point maxScreenPoint = new android.graphics.Point(mMapView.getWidth() - 200, mMapView.getHeight() - 200);
        // convert screen points to map points
        Point minPoint = mMapView.screenToLocation(minScreenPoint);
        Point maxPoint = mMapView.screenToLocation(maxScreenPoint);
        // use the points to define and return an envelope
        if (minPoint != null && maxPoint != null) {
          Envelope envelope = new Envelope(minPoint, maxPoint);
          downloadArea.setGeometry(envelope);
        }
      }
    });

    // create a progress dialog to show download progress
    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Generate Offline Map");
    progressDialog.setMessage("Taking map offline...");
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setIndeterminate(false);
    progressDialog.setProgress(0);

    // when the button is clicked, start the offline map task job
    takeMapOfflineButton.setOnClickListener(v -> {
      progressDialog.show();

      // delete any offline map already in the cache
      String tempDirectoryPath = getCacheDir() + File.separator + "offlineMap";
      deleteDirectory(new File(tempDirectoryPath));

      // specify the extent, min scale, and max scale as parameters
      double minScale = mMapView.getMapScale();
      double maxScale = map.getMaxScale();
      // minScale must always be larger than maxScale
      if (minScale <= maxScale) {
        minScale = maxScale + 1;
      }
      GenerateOfflineMapParameters params = new GenerateOfflineMapParameters(downloadArea.getGeometry(), minScale,
          maxScale);

      // create an offline map task with the map
      OfflineMapTask task = new OfflineMapTask(map);

      // create an offline map job with the download directory path and parameters and start the job
      GenerateOfflineMapJob job = task.generateOfflineMap(params, tempDirectoryPath);

      // replace the current map with the result offline map when the job finishes
      job.addJobDoneListener(() -> {
        if (job.getStatus() == Job.Status.SUCCEEDED) {
          GenerateOfflineMapResult result = job.getResult();
          mMapView.setMap(result.getOfflineMap());
          graphicsOverlay.getGraphics().clear();
          takeMapOfflineButton.setEnabled(false);
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
    });

  }

  /**
   * Recursively deletes all files in the given directory.
   *
   * @param file to delete
   */
  void deleteDirectory(File file) {
    if (file.isDirectory())
      for (File subFile : file.listFiles())
        deleteDirectory(subFile);
    file.delete();
  }

  /**
   * Handle the permissions request response.
   */
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      generateOfflineMap();
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
}

