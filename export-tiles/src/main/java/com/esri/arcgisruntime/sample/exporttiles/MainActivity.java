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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheJob;
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheParameters;
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheTask;

public class MainActivity extends AppCompatActivity {

  private String TAG = MainActivity.class.getSimpleName();

  private Button mExportTilesButton;
  private RelativeLayout mProgressLayout;
  private TextView mProgressTextView;
  private ProgressBar mProgressBar;
  private RelativeLayout mMapPreviewLayout;

  private MapView mMapView;
  private MapView mPreviewMapView;
  private ArcGISTiledLayer mTiledLayer;
  private ExportTileCacheJob mExportTileCacheJob;
  private ExportTileCacheTask mExportTileCacheTask;

  private boolean mDownloading = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // define permission to request
    String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this,
        reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
    } else {
      // request permission
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }

    mProgressLayout = (RelativeLayout) findViewById(R.id.progressLayout);
    mProgressTextView = (TextView) findViewById(R.id.progress_text_view);
    mProgressBar = (ProgressBar) findViewById(R.id.taskProgressBar);

    mTiledLayer = new ArcGISTiledLayer(getString(R.string.world_street_map));
    ArcGISMap map = new ArcGISMap();
    map.setBasemap(new Basemap(mTiledLayer));

    mMapPreviewLayout = (RelativeLayout) findViewById(R.id.mapPreviewLayout);

    mMapView = (MapView) findViewById(R.id.mapView);
    mMapView.setMap(map);

    mPreviewMapView = (MapView) findViewById(R.id.previewMapView);

    createExportTilesButton();
    createPreviewCloseButton();

    // run cancel download once to clear map preview and progress bar
    cancelDownload();
  }

  /**
   * Uses the MapView View to define an Envelope larger on all sides by one MapView.
   *
   * @return extent
   */
  private Envelope viewToExtent() {
    // get the upper left corner of the map view
    android.graphics.Point minScreenPoint = new android.graphics.Point(mMapView.getLeft() - mMapView.getWidth(), mMapView.getTop() - mMapView.getHeight());
    // get the lower right corner of the map view
    android.graphics.Point maxScreenPoint = new android.graphics.Point(minScreenPoint.x + (mMapView.getWidth() * 3), minScreenPoint.y + (mMapView.getHeight() * 3 ));
    // convert screen points to map points
    Point minPoint = mMapView.screenToLocation(minScreenPoint);
    Point maxPoint = mMapView.screenToLocation(maxScreenPoint);
    // use the points to define and return an envelope
    return new Envelope(minPoint, maxPoint);
  }

  private void cancelDownload() {
    Log.d(TAG, "cancelDownload");
    mDownloading = false;
    mExportTilesButton.setVisibility(View.VISIBLE);
    mProgressLayout.setVisibility(View.INVISIBLE);
    mProgressTextView.setVisibility(View.INVISIBLE);
    mProgressBar.setVisibility(View.INVISIBLE);
    mProgressBar.setProgress(0);
    mPreviewMapView.getChildAt(0).setVisibility(View.INVISIBLE);
    mMapView.bringToFront();
    mExportTilesButton.setText(getResources().getString(R.string.export_tiles_text));
    if (mExportTileCacheJob != null) {
      mExportTileCacheJob.cancel();
    }
  }

  private void initiateDownload() {
    Log.d(TAG, "initiateDownload");
    mExportTilesButton.setText(getResources().getString(R.string.cancel_export_tiles_text));
    mProgressLayout.setVisibility(View.VISIBLE);
    mProgressLayout.bringToFront();
    mProgressTextView.setText(getString(R.string.progress_starting));
    //mProgressTextView.bringToFront();
    mProgressTextView.setVisibility(View.VISIBLE);
    //mProgressBar.bringToFront();
    mProgressBar.setVisibility(View.VISIBLE);
    //mPreviewMapView.getChildAt(1).setVisibility(View.VISIBLE);
    //get the parameters by specifying the selected area,
    //mapview's current scale as the minScale and tiled layer's max scale as maxScale
    double minScale = mMapView.getMapScale();
    double maxScale = mTiledLayer.getMaxScale();

    // minScale must always be larger than maxScale
    if (minScale == maxScale) {
      minScale = maxScale + 1;
    }

    Log.d("minScale", String.valueOf(minScale));
    Log.d("maxScale", String.valueOf(maxScale));

    //set the state
    mDownloading = true;

    //initialize the export task
    Log.d("tilecacheurl", mTiledLayer.getUri());
    mExportTileCacheTask = new ExportTileCacheTask(mTiledLayer.getUri());
    final ListenableFuture<ExportTileCacheParameters> parametersFuture = mExportTileCacheTask
        .createDefaultExportTileCacheParametersAsync(viewToExtent(), minScale, maxScale);
    parametersFuture.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          ExportTileCacheParameters parameters = parametersFuture.get();
          mExportTileCacheJob = mExportTileCacheTask.exportTileCacheAsync(parameters,
              Environment.getExternalStorageDirectory() + getString(R.string.config_data_sdcard_offline_dir));
        } catch (InterruptedException e) {
          Log.e(TAG, "TileCacheParameters interrupted: " + e.getMessage());
        } catch (ExecutionException e) {
          Log.e(TAG, "Error generating parameters: " + e.getMessage());
        }
        mExportTileCacheJob.start();
        mProgressTextView.setText(getString(R.string.progress_starting));
        Log.e("Job", String.valueOf(mExportTileCacheJob.getError()));
        mExportTileCacheJob.addProgressChangedListener(new Runnable() {
          @Override public void run() {
            mProgressTextView.setText(getString(R.string.progress_fetching));
            mProgressBar.setProgress(mExportTileCacheJob.getProgress());
            Log.d("Progress", String.valueOf(mExportTileCacheJob.getProgress()));
          }
        });
        mExportTileCacheJob.addJobDoneListener(new Runnable() {
          @Override public void run() {
            if (mExportTileCacheJob.getResult() != null) {
              TileCache exportedTileCacheResult = mExportTileCacheJob.getResult();
              showMapPreview(exportedTileCacheResult);
            } else {
              Log.e(TAG, "Tile cache job result null. File size may be too big.");
              cancelDownload();
            }
            mProgressTextView.setText(getString(R.string.progress_done));
          }
        });
      }
    });
  }

  private void showMapPreview(TileCache result) {
    ArcGISTiledLayer newTiledLayer = new ArcGISTiledLayer(result);
    ArcGISMap map = new ArcGISMap(new Basemap(newTiledLayer));
    mPreviewMapView.setMap(map);
    mPreviewMapView.setViewpoint(mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE));
    mMapPreviewLayout.bringToFront();
    mPreviewMapView.getChildAt(0).setVisibility(View.VISIBLE);
    mExportTilesButton.setVisibility(View.GONE);
  }

  private void createExportTilesButton() {
    //setup export tiles button
    mExportTilesButton = (Button) findViewById(R.id.exportTilesButton);
    mExportTilesButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d("onClick", "Button touched!");
        if (mDownloading) {
          cancelDownload();
        } else {
          initiateDownload();
        }
      }
    });
  }

  private void createPreviewCloseButton() {
    Button previewCloseButton = (Button) findViewById(R.id.closeButton);
    previewCloseButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        cancelDownload();
      }
    });
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
