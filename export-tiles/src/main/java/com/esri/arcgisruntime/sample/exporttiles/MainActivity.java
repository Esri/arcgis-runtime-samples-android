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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheJob;
import com.esri.arcgisruntime.tasks.tilecache.ExportTileCacheTask;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private MapView mPrivateMapView;
  private ArcGISTiledLayer mTiledLayer;
  private GraphicsOverlay mGraphicsOverlay;

  private ExportTileCacheJob mTileCacheJob;
  private ExportTileCacheTask mTileCacheTask;

  private boolean mDownloading = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mTiledLayer = new ArcGISTiledLayer(
        "https://sampleserver6.arcgisonline.com/arcgis/rest/services/World_Street_Map/MapServer");
    ArcGISMap map = new ArcGISMap();
    map.setBasemap(Basemap.createStreets());

    //add the graphics overlay to the map view
    mGraphicsOverlay = new GraphicsOverlay();
    mMapView = new MapView(MainActivity.this);
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    //setup export tiles button
    Button exportTiles = new Button(MainActivity.this);

    //setupExportTilesButton();
    //setup export tiles button
    final Button exportTilesButton = new Button(MainActivity.this);
    exportTilesButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d("onClick", "Button touched!");
        if (mDownloading) {
          cancelDownload();
          exportTilesButton.setText(getResources().getString(R.string.export_tiles_text));
        } else {
          //download()
          exportTilesButton.setText(getResources().getString(R.string.downloading_text));
          mDownloading = true;
        }
      }
    });

    //setupExtentView();

    //mPrivateMapView.layer.borderColor = UIColor.white.cgColor
    //self.previewMapView.layer.borderWidth = 8
  }

  private void setupExportTilesButton() {
    //setup export tiles button
    final Button exportTilesButton = new Button(MainActivity.this);
    exportTilesButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d("onClick", "Button touched!");
        if (mDownloading) {
          cancelDownload();
          exportTilesButton.setText(getResources().getString(R.string.export_tiles_text));
        } else {
          //download()
          exportTilesButton.setText(getResources().getString(R.string.downloading_text));
          mDownloading = true;
        }
      }
    });
  }

  private void cancelDownload() {
    if (mTileCacheJob != null) {
      //SVProgressHUD.dismiss()
      mTileCacheJob.cancel();
      mDownloading = false;
      //self.visualEffectView.isHidden = true
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
