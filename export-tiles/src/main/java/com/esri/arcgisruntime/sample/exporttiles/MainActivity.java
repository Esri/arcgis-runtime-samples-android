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

package com.esri.arcgisruntime.sample.exporttiles;

import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheJob;
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheParameters;
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheTask;

public class MainActivity extends AppCompatActivity {

  private String TAG = MainActivity.class.getSimpleName();

  private ProgressBar mProgressBar;

  private MapView mMapView;
  private MapView mPreviewMapView;
  private ArcGISTiledLayer mTiledLayer;
  private GraphicsOverlay mGraphicsOverlay;
  private ExportTileCacheJob mExportTileCacheJob;
  private ExportTileCacheTask mExportTileCacheTask;

  private boolean mDownloading = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    handleWritePermissions();

    mProgressBar = (ProgressBar) findViewById(R.id.taskProgressBar);

    mTiledLayer = new ArcGISTiledLayer(getString(R.string.world_street_map));
    mExportTileCacheTask = new ExportTileCacheTask(getString(R.string.world_street_map));
    ArcGISMap map = new ArcGISMap();
    map.setBasemap(Basemap.createStreets());

    //add the graphics overlay to the map view
    mGraphicsOverlay = new GraphicsOverlay();
    mMapView = (MapView) findViewById(R.id.mapView);
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
    mMapView.setMap(map);
    mPreviewMapView = (MapView) findViewById(R.id.previewMapView);

    setupExportTilesButton();

  }

  private void setupExportTilesButton() {
    //setup export tiles button
    final Button exportTilesButton = (Button) findViewById(R.id.exportTilesButton);
    exportTilesButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d("onClick", "Button touched!");
        if (mDownloading) {
          cancelDownload();
          exportTilesButton.setText(getResources().getString(R.string.export_tiles_text));
        } else {
          initiateDownload();
          exportTilesButton.setText(getResources().getString(R.string.cancel_export_tiles_text));
        }
      }
    });
  }

  private Envelope viewToExtent() {
    int[] outLocation = new int[2];
    mPreviewMapView.getLocationOnScreen(outLocation);
    for (int location : outLocation) {
      Log.d("coor", String.valueOf(location));
    }

    android.graphics.Point minScreenPoint = new android.graphics.Point(outLocation[0], outLocation[1]);
    android.graphics.Point maxScreenPoint = new android.graphics.Point(minScreenPoint.x + mPreviewMapView.getWidth(),
        minScreenPoint.y + mPreviewMapView.getHeight());

    Log.d("minScreen", "x: " + minScreenPoint.x + " y: " + minScreenPoint.y);
    Log.d("maxScreen", "x: " + maxScreenPoint.x + " y: " + maxScreenPoint.y);

    Point minPoint = mMapView.screenToLocation(minScreenPoint);
    Point maxPoint = mMapView.screenToLocation(maxScreenPoint);

    Log.d("minMap", "x: " + minPoint.getX() + " y: " + minPoint.getY());
    Log.d("maxMap", "x: " + maxPoint.getX() + " y: " + maxPoint.getY());

    Envelope extent = new Envelope(new Point(0,0), new Point(10,10));
    return extent;
  }

  private void cancelDownload() {
    Log.d(TAG, "cancelDownload");
    mDownloading = false;
    if (mExportTileCacheJob != null) {
      mProgressBar.setVisibility(View.INVISIBLE);
      mExportTileCacheJob.cancel();

      //self.visualEffectView.isHidden = true
    }
  }

  private void initiateDownload() {
    Log.d(TAG, "initiateDownload");

    //get the parameters by specifying the selected area,
    //mapview's current scale as the minScale and tiled layer's max scale as maxScale
    double minScale = mMapView.getMapScale();
    double maxScale = 8.0;

    //set the state
    mDownloading = true;

    //delete previous existing tpks
    //deleteAllTpks();

    //initialize the export task
    Log.d("tilecacheurl", mTiledLayer.getUri());
    mExportTileCacheTask = new ExportTileCacheTask(mTiledLayer.getUri());
    final ListenableFuture<ExportTileCacheParameters> parametersFuture = mExportTileCacheTask
        .createDefaultExportTileCacheParametersAsync(viewToExtent(), minScale, maxScale);
    parametersFuture.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          ExportTileCacheParameters parameters = parametersFuture.get();
          Log.d(TAG, "parameters loaded");
          mExportTileCacheJob = mExportTileCacheTask.exportTileCacheAsync(parameters, Environment.getExternalStorageDirectory() + getString(R.string.config_data_sdcard_offline_dir));
          mExportTileCacheJob.start();
          mExportTileCacheJob.addProgressChangedListener(new Runnable() {
            @Override public void run() {
              Log.d("Progress", String.valueOf(mExportTileCacheJob.getProgress()));
            }
          });
          mExportTileCacheJob.addJobDoneListener(new Runnable() {
            @Override public void run() {
              Log.e(TAG, mExportTileCacheJob.getError().getMessage());
              TileCache exportedTileCacheResult = mExportTileCacheJob.getResult();
              ArcGISTiledLayer newTiledLayer = new ArcGISTiledLayer(exportedTileCacheResult);
              mPreviewMapView.getMap().setBasemap(new Basemap(newTiledLayer));
            }
          });
        } catch (InterruptedException | ExecutionException e) {
          Log.e(TAG, "Error exporting tile cache: " + e.getMessage());
        }
      }
    });
  }

  private void handleWritePermissions() {
    //for API level 23+ request permission at runtime
    final String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    if (ContextCompat.checkSelfPermission(getApplicationContext(), reqPermission[0])
        != PackageManager.PERMISSION_GRANTED) {
      //request permission
      int requestCode = 2;
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
