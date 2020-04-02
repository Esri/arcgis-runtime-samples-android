/*
 * Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.hillshaderenderer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.HillshadeRenderer;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.raster.SlopeType;

/**
 * A sample class which demonstrates how to use a hillshade renderer on a raster layer.
 */
public class MainActivity extends AppCompatActivity
    implements ParametersDialogFragment.ParametersListener {

  private MapView mMapView;
  private RasterLayer mRasterLayer;
  private int mAltitude;
  private int mAzimuth;
  private double mZFactor;
  private SlopeType mSlopeType;
  private double mPixelSizeFactor;
  private double mPixelSizePower;
  private int mOutputBitDepth;
  private FragmentManager mFragmentManager;

  @Override
  public void returnParameters(int altitude, int azimuth, SlopeType slopeType) {
    // gets dialog box parameters and calls updateRenderer
    mAltitude = altitude;
    mAzimuth = azimuth;
    mSlopeType = slopeType;
    updateRenderer();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // set default values for HillshadeRenderer parameters
    mAltitude = 45;
    mAzimuth = 315;
    mZFactor = 0.000016;
    mSlopeType = SlopeType.NONE;
    mPixelSizeFactor = 1;
    mPixelSizePower = 1;
    mOutputBitDepth = 8;
    // retrieve the MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    mFragmentManager = getSupportFragmentManager();

    // create raster
    Raster raster = new Raster(getExternalFilesDir(null) + getString(R.string.hillshade_raster_name));

    // create a raster layer
    mRasterLayer = new RasterLayer(raster);
    // create a basemap from the raster layer
    ArcGISMap map = new ArcGISMap();
    // add the map to a map view
    mMapView.setMap(map);
    // add the raster as an operational layer
    map.getOperationalLayers().add(mRasterLayer);
    updateRenderer();
  }

  /**
   * Creates a new HillshadeRenderer according to the chosen property values.
   */
  private void updateRenderer() {
    // create blend renderer
    HillshadeRenderer hillshadeRenderer = new HillshadeRenderer(mAltitude, mAzimuth,
        mZFactor, mSlopeType, mPixelSizeFactor, mPixelSizePower, mOutputBitDepth);
    mRasterLayer.setRasterRenderer(hillshadeRenderer);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.hillshade_parameters, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    ParametersDialogFragment paramDialog = new ParametersDialogFragment();
    Bundle hillshadeParameters = new Bundle();
    //send parameters to fragment
    hillshadeParameters.putInt("altitude", mAltitude);
    hillshadeParameters.putInt("azimuth", mAzimuth);
    hillshadeParameters.putSerializable("slope_type", mSlopeType);
    paramDialog.setArguments(hillshadeParameters);
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
