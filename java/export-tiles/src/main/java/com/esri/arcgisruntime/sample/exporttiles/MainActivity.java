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

import java.io.File;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

  private final String TAG = MainActivity.class.getSimpleName();

  private Button mExportTilesButton;
  private ConstraintLayout mTileCachePreviewLayout;
  private View mPreviewMask;

  private MapView mMapView;
  private MapView mTileCachePreview;
  private ArcGISTiledLayer mTiledLayer;
  private ExportTileCacheJob mExportTileCacheJob;
  private ExportTileCacheTask mExportTileCacheTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // define permission to request
    String[] reqPermission = { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) != PackageManager.PERMISSION_GRANTED) {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }

    // get references to ui elements
    mTileCachePreviewLayout = findViewById(R.id.mapPreviewLayout);
    mPreviewMask = findViewById(R.id.previewMask);
    mTileCachePreview = findViewById(R.id.previewMapView);
    mMapView = findViewById(R.id.mapView);

    mTiledLayer = new ArcGISTiledLayer(getString(R.string.world_street_map));
    ArcGISMap map = new ArcGISMap();
    map.setBasemap(new Basemap(mTiledLayer));
    // set a min scale to avoid instance of downloading a tile cache that is too big
    map.setMinScale(10000000);
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint(51.5, 0.0, 10000000));

    mExportTilesButton = findViewById(R.id.exportTilesButton);
    mExportTilesButton.setOnClickListener(v -> initiateDownload());

    Button previewCloseButton = findViewById(R.id.closeButton);
    previewCloseButton.setOnClickListener(v -> clearPreview());

    clearPreview();
  }

  /**
   * Uses the current MapView to define an Envelope larger on all sides by one MapView in the relevant dimension.
   * ____________________
   * |                  |
   * |      downloaded  |
   * |     ________     |
   * |     |      |  t  |
   * |     | tile |  i  |
   * |     |cache |  l  |
   * |     |preview  e  |
   * |     | View |     |
   * |     --------     |
   * |       cache area |
   * |                  |
   * |                  |
   * --------------------
   *
   * @return an Envelope three times as high and three times as wide as the main MapView.
   */
  private Envelope viewToExtent() {
    // upper left corner of the downloaded tile cache area
    android.graphics.Point minScreenPoint = new android.graphics.Point(mMapView.getLeft() - mMapView.getWidth(),
        mMapView.getTop() - mMapView.getHeight());
    // lower right corner of the downloaded tile cache area
    android.graphics.Point maxScreenPoint = new android.graphics.Point(minScreenPoint.x + mMapView.getWidth() * 3,
        minScreenPoint.y + mMapView.getHeight() * 3);
    // convert screen points to map points
    Point minPoint = mMapView.screenToLocation(minScreenPoint);
    Point maxPoint = mMapView.screenToLocation(maxScreenPoint);
    // use the points to define and return an envelope
    return new Envelope(minPoint, maxPoint);
  }

  /**
   * Clear preview window.
   */
  private void clearPreview() {
    // make map preview invisible
    mTileCachePreview.getChildAt(0).setVisibility(View.INVISIBLE);
    mMapView.bringToFront();
    // show red preview mask
    mPreviewMask.bringToFront();
    mExportTilesButton.setVisibility(View.VISIBLE);
  }

  /**
   * Using scale defined by the main MapView and the TiledLayer and an extent defined by viewToExtent() as parameters,
   * downloads a TileCache locally to the device.
   */
  private void initiateDownload() {
    // map view's current scale as the minScale and tiled layer's max scale as maxScale
    double minScale = mMapView.getMapScale();
    double maxScale = mTiledLayer.getMaxScale();
    // minScale must always be larger than maxScale
    if (minScale <= maxScale) {
      minScale = maxScale + 1;
    }
    // initialize the export task
    mExportTileCacheTask = new ExportTileCacheTask(mTiledLayer.getUri());
    final ListenableFuture<ExportTileCacheParameters> parametersFuture = mExportTileCacheTask
        .createDefaultExportTileCacheParametersAsync(viewToExtent(), minScale, maxScale);
    parametersFuture.addDoneListener(() -> {
      // create directory for file
      File file = new File(Environment.getExternalStorageDirectory(), getString(R.string.tile_cache_folder));
      if (!file.exists()) {
        boolean dirCreated = file.mkdirs();
        if (dirCreated) {
          Log.i(TAG, "Local TileCache directory created.");
        } else {
          Log.e(TAG, "Error creating local TileCache directory.");
        }
      } else {
        Log.i(TAG, "No local TileCache directory created, one already exists.");
      }
      try {
        // export tile cache to directory
        ExportTileCacheParameters parameters = parametersFuture.get();
        mExportTileCacheJob = mExportTileCacheTask.exportTileCache(parameters,
            Environment.getExternalStorageDirectory() + getString(R.string.tile_cache_folder)
                + getString(R.string.world_street_map_tpk));
      } catch (InterruptedException e) {
        Log.e(TAG, "TileCacheParameters interrupted: " + e.getMessage());
      } catch (ExecutionException e) {
        Log.e(TAG, "Error generating parameters: " + e.getMessage());
      }
      mExportTileCacheJob.start();

      createProgressDialog(mExportTileCacheJob);

      mExportTileCacheJob.addJobDoneListener(() -> {
        if (mExportTileCacheJob.getResult() != null) {
          TileCache exportedTileCacheResult = mExportTileCacheJob.getResult();
          showMapPreview(exportedTileCacheResult);
        } else {
          Log.e(TAG, "Tile cache job result null. File size may be too big.");
          Toast.makeText(this,
              "Tile cache job result null. File size may be too big. Try zooming in before exporting tiles",
              Toast.LENGTH_LONG).show();
        }
      });
    });
  }

  /**
   * Show progress UI elements.
   *
   * @param exportTileCacheJob used to track progress and cancel when required
   */
  private void createProgressDialog(ExportTileCacheJob exportTileCacheJob) {

    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Export Tile Cache Job");
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setCanceledOnTouchOutside(false);
    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
        (dialogInterface, i) -> exportTileCacheJob.cancel());
    progressDialog.show();

    exportTileCacheJob.addProgressChangedListener(() -> progressDialog.setProgress(exportTileCacheJob.getProgress()));
    exportTileCacheJob.addJobDoneListener(progressDialog::dismiss);
  }

  /**
   * Show tile cache preview window including MapView.
   *
   * @param result Takes the TileCache from the ExportTileCacheJob.
   */
  private void showMapPreview(TileCache result) {
    ArcGISTiledLayer newTiledLayer = new ArcGISTiledLayer(result);
    ArcGISMap map = new ArcGISMap(new Basemap(newTiledLayer));
    mTileCachePreview.setMap(map);
    mTileCachePreview.setViewpoint(mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE));
    mTileCachePreview.setVisibility(View.VISIBLE);
    mTileCachePreviewLayout.bringToFront();
    mTileCachePreview.getChildAt(0).setVisibility(View.VISIBLE);
    mExportTilesButton.setVisibility(View.GONE);
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    mTileCachePreview.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
    mTileCachePreview.resume();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    mTileCachePreview.dispose();
    super.onDestroy();
  }
}
