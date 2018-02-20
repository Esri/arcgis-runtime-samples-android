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

package com.esri.arcgisruntime.sample.blendrenderer;

import java.io.File;
import java.util.Collections;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.BlendRenderer;
import com.esri.arcgisruntime.raster.ColorRamp;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.raster.SlopeType;

public class MainActivity extends AppCompatActivity implements ParametersDialogFragment.ParametersListener {

  private MapView mMapView;
  private File mImageFile;
  private File mElevationFile;

  private int mAltitude;
  private int mAzimuth;
  private double mZFactor;
  private SlopeType mSlopeType;
  private ColorRamp.PresetType mColorRampType;
  private double mPixelSizeFactor;
  private double mPixelSizePower;
  private int mOutputBitDepth;

  private FragmentManager mFragmentManager;

  @Override
  public void returnParameters(int altitude, int azimuth, SlopeType slopeType, ColorRamp.PresetType colorRampType) {
    //gets dialog box parameters and calls updateRenderer
    mAltitude = altitude;
    mAzimuth = azimuth;
    mSlopeType = slopeType;
    mColorRampType = colorRampType;
    updateRenderer();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    //set default values for blend parameters
    mAltitude = 45;
    mAzimuth = 315;
    mZFactor = 0.000016;
    mSlopeType = SlopeType.NONE;
    mColorRampType = ColorRamp.PresetType.NONE;
    mPixelSizeFactor = 1;
    mPixelSizePower = 1;
    mOutputBitDepth = 8;
    // retrieve the MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    mFragmentManager = getSupportFragmentManager();
    // define permission to request
    String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this,
        reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      blendRenderer();
    } else {
      // request permission
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }
  }

  /**
   * Using values stored in strings.xml, builds path to rasters.
   *
   * @return the path to raster file
   */
  private String buildRasterPath(String filename) {
    // get sdcard resource name
    File extStorDir = Environment.getExternalStorageDirectory();
    // get the directory
    String extSDCardDirName =
        this.getResources().getString(R.string.data_sdcard_offline_dir);
    // create the full path to the raster file
    return extStorDir.getAbsolutePath()
        + File.separator
        + extSDCardDirName
        + File.separator
        + filename
        + ".tif";
  }

  /**
   * Creates new imagery and elevation files based on a given path, creates an ArcGISMap, sets it to a MapView and
   * calls updateRenderer().
   */
  private void blendRenderer() {
    // create raster files
    mImageFile = new File(buildRasterPath(this.getString(R.string.imagery_raster_name)));
    mElevationFile = new File(buildRasterPath(this.getString(R.string.elevation_raster_name)));
    // create a map
    ArcGISMap map = new ArcGISMap();
    // add the map to a map view
    mMapView.setMap(map);
    updateRenderer();
  }

  /**
   * Creates ColorRamp and BlendRenderer according to the chosen property values.
   */
  private void updateRenderer() {
    // if color ramp type is not None, create a new ColorRamp
    ColorRamp colorRamp = mColorRampType != ColorRamp.PresetType.NONE ? new ColorRamp(mColorRampType, 800) : null;
    // create rasters
    Raster imageryRaster = new Raster(mImageFile.getAbsolutePath());
    Raster elevationRaster = new Raster(mElevationFile.getAbsolutePath());
    // if color ramp is not NONE, color the hillshade elevation raster instead of using satellite imagery raster color
    RasterLayer rasterLayer = colorRamp != null ? new RasterLayer(elevationRaster) : new RasterLayer(imageryRaster);
    mMapView.getMap().setBasemap(new Basemap(rasterLayer));
    // create blend renderer
    BlendRenderer blendRenderer = new BlendRenderer(
        elevationRaster,
        Collections.singletonList(9.0),
        Collections.singletonList(255.0),
        null,
        null,
        null,
        null,
        colorRamp,
        mAltitude,
        mAzimuth,
        mZFactor,
        mSlopeType,
        mPixelSizeFactor,
        mPixelSizePower,
        mOutputBitDepth);
    rasterLayer.setRasterRenderer(blendRenderer);
  }

  /**
   * Handle the permissions request response.
   */
  public void onRequestPermissionsResult(int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      blendRenderer();
    } else {
      // report to user that permission was denied
      Toast.makeText(MainActivity.this,
          getResources().getString(R.string.location_permission_denied),
          Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.blend_parameters, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    ParametersDialogFragment paramDialog = new ParametersDialogFragment();
    Bundle blendParameters = new Bundle();
    //send parameters to fragment
    blendParameters.putInt("altitude", mAltitude);
    blendParameters.putInt("azimuth", mAzimuth);
    blendParameters.putSerializable("slope_type", mSlopeType);
    blendParameters.putSerializable("color_ramp_type", mColorRampType);
    paramDialog.setArguments(blendParameters);
    paramDialog.show(mFragmentManager, "param_dialog");
    return super.onOptionsItemSelected(item);
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