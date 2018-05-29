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

package com.esri.arcgisruntime.sample.colormaprenderer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.ColormapRenderer;
import com.esri.arcgisruntime.raster.Raster;

/**
 * A sample class which demonstrates the ColorMapRenderer.
 */
public class MainActivity extends AppCompatActivity {

  private final static String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // retrieve the MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // define permission to request
    String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this,
        reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      loadRaster();
    } else {
      // request permission
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }
  }

  /**
   * Using values stored in strings.xml, builds path to ShastaBW.tif.
   *
   * @return the path to raster file
   */
  private String buildRasterPath() {
    // get sdcard resource name
    File extStorDir = Environment.getExternalStorageDirectory();
    // get the directory
    String extSDCardDirName = this.getResources().getString(R.string.raster_folder);
    // get raster filename
    String filename = this.getString(R.string.shasta_b_w);
    // create the full path to the raster file
    return extStorDir.getAbsolutePath()
        + File.separator
        + extSDCardDirName
        + File.separator
        + filename
        + ".tif";
  }

  /**
   * Loads ShastaBW.tif as a Raster and adds it to a new RasterLayer. RasterLayer is then added to the map as an
   * operational layer. A List of color values is created (0-149: red) (150-250: yellow). The List is passed to a new
   * ColorMapRenderer, which is then set to the RasterLayer Rendererer. Map viewpoint is then set based on Raster
   * geometry.
   */
  private void loadRaster() {
    // create a raster from a local raster file
    Raster raster = new Raster(buildRasterPath());
    // create a raster layer
    final RasterLayer rasterLayer = new RasterLayer(raster);
    // create a Map with imagery basemap
    ArcGISMap map = new ArcGISMap(Basemap.createImagery());
    // add the map to a map view
    mMapView.setMap(map);
    // add the raster as an operational layer
    map.getOperationalLayers().add(rasterLayer);
    // create a color map where values 0-149 are red (Color.RED) and 150-250 are yellow (Color.Yellow)
    List<Integer> colors = new ArrayList<>();
    for (int i = 0; i <= 250; i++) {
      if (i < 150) {
        colors.add(i, Color.RED);
      } else {
        colors.add(i, Color.YELLOW);
      }
    }
    // create a colormap renderer
    ColormapRenderer colormapRenderer = new ColormapRenderer(colors);
    // set the ColormapRenderer on the RasterLayer
    rasterLayer.setRasterRenderer(colormapRenderer);
    // set Viewpoint on the Raster
    rasterLayer.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        if (rasterLayer.getLoadStatus() == LoadStatus.LOADED) {
          mMapView.setViewpointGeometryAsync(rasterLayer.getFullExtent(), 50);
        } else {
          String error = "RasterLayer failed to load: " + rasterLayer.getLoadError().getMessage();
          Log.e(TAG, error);
          Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
        }
      }
    });
  }

  /**
   * Handle the permissions request response.
   */
  public void onRequestPermissionsResult(int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      loadRaster();
    } else {
      // report to user that permission was denied
      Toast.makeText(MainActivity.this,
          getResources().getString(R.string.colormap_write_permission_denied),
          Toast.LENGTH_SHORT).show();
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
