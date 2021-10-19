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

package com.esri.arcgisruntime.sample.generateofflinemap;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
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
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapJob;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapParameters;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapResult;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask;

import java.io.File;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private Button mTakeMapOfflineButton;
  private GraphicsOverlay mGraphicsOverlay;
  private Graphic mDownloadArea;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // access button to take the map offline and disable it until map is loaded
    mTakeMapOfflineButton = findViewById(R.id.takeMapOfflineButton);
    mTakeMapOfflineButton.setEnabled(false);

    // authentication with an API key or named user is required
    // to access basemaps and other location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // create a portal item with the itemId of the web map
    Portal portal = new Portal(getString(R.string.portal_url), false);
    PortalItem portalItem = new PortalItem(portal, getString(R.string.item_id));

    // create a map with the portal item
    ArcGISMap map = new ArcGISMap(portalItem);
    map.addDoneLoadingListener(() -> {
      if (map.getLoadStatus() == LoadStatus.LOADED) {
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
          // enable the take map offline button only after the map is loaded
          mTakeMapOfflineButton.setEnabled(true);
        }
      }
    });

    // create a progress dialog to show download progress
    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Generate offline map job");
    progressDialog.setMessage("Taking map offline...");
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setIndeterminate(false);
    progressDialog.setProgress(0);

    // when the button is clicked, start the offline map task job
    mTakeMapOfflineButton.setOnClickListener(v -> {
      progressDialog.show();

      // delete any offline map already in the cache
      String tempDirectoryPath = getExternalCacheDir() + File.separator + "offlineMap";
      deleteDirectory(new File(tempDirectoryPath));

      // specify the extent, min scale, and max scale as parameters
      double minScale = mMapView.getMapScale();
      double maxScale = mMapView.getMap().getMaxScale();
      // minScale must always be larger than maxScale
      if (minScale <= maxScale) {
        minScale = maxScale + 1;
      }
      GenerateOfflineMapParameters generateOfflineMapParameters = new GenerateOfflineMapParameters(
          mDownloadArea.getGeometry(), minScale, maxScale);
      // set job to cancel on any errors
      generateOfflineMapParameters.setContinueOnErrors(false);

      // create an offline map offlineMapTask with the map
      OfflineMapTask offlineMapTask = new OfflineMapTask(mMapView.getMap());

      // create an offline map job with the download directory path and parameters and start the job
      GenerateOfflineMapJob job = offlineMapTask.generateOfflineMap(generateOfflineMapParameters, tempDirectoryPath);

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
    });

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

