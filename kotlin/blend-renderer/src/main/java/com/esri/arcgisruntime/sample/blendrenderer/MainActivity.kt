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

package com.esri.arcgisruntime.sample.blendrenderer

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.raster.BlendRenderer
import com.esri.arcgisruntime.raster.ColorRamp
import com.esri.arcgisruntime.raster.Raster
import com.esri.arcgisruntime.raster.SlopeType
import com.esri.arcgisruntime.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ParametersDialogFragment.ParametersListener {

  private var mImageRaster: Raster? = null
  private var mElevationRaster: Raster? = null

  private var mAltitude: Double = 0.toDouble()
  private var mAzimuth: Double = 0.toDouble()
  private var mZFactor: Double = 0.toDouble()
  private var mSlopeType: SlopeType? = null
  private var mColorRampType: ColorRamp.PresetType? = null
  private var mPixelSizeFactor: Double = 0.toDouble()
  private var mPixelSizePower: Double = 0.toDouble()
  private var mOutputBitDepth: Int = 0

  override fun returnParameters(altitude: Double, azimuth: Double, slopeType: SlopeType?,
                                colorRampType: ColorRamp.PresetType?) {
    //gets dialog box parameters and calls updateRenderer
    mAltitude = altitude
    mAzimuth = azimuth
    mSlopeType = slopeType
    mColorRampType = colorRampType
    updateRenderer()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    //set default values for blend parameters
    mAltitude = 45.0
    mAzimuth = 315.0
    mZFactor = 0.000016
    mSlopeType = SlopeType.NONE
    mColorRampType = ColorRamp.PresetType.NONE
    mPixelSizeFactor = 1.0
    mPixelSizePower = 1.0
    mOutputBitDepth = 8

    // request permission
    PermissionUtils.requestPermission(this@MainActivity, ::blendRenderer,
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
  }

  /**
   * Creates new imagery and elevation files based on a given path, creates an ArcGISMap, sets it to a MapView and
   * calls updateRenderer().
   */
  private fun blendRenderer() {
    // create raster files
    mImageRaster = Raster(
        Environment.getExternalStorageDirectory().absolutePath + this.getString(R.string.shasta_path))
    mElevationRaster = Raster(
        Environment.getExternalStorageDirectory().absolutePath + this.getString(R.string.shasta_elevation_path))
    // create a map and it to a map view
    mapView.map = ArcGISMap()
    updateRenderer()
  }

  /**
   * Creates ColorRamp and BlendRenderer according to the chosen property values.
   */
  private fun updateRenderer() {

    // when color ramp type is not NONE, create a new ColorRamp
    val colorRamp = when {
      mColorRampType != ColorRamp.PresetType.NONE -> ColorRamp(mColorRampType, 800)
      else -> null
    }

    // if color ramp is not null, color the hillshade elevation raster instead of the satellite imagery raster
    when {
      colorRamp != null -> RasterLayer(mElevationRaster)
      else -> RasterLayer(mImageRaster)
    }.let {
      mapView.map.basemap = Basemap(it)
      // create blend renderer
      it.rasterRenderer = BlendRenderer(
          mElevationRaster,
          listOf(9.0),
          listOf(255.0), null, null, null, null,
          colorRamp,
          mAltitude,
          mAzimuth,
          mZFactor,
          mSlopeType,
          mPixelSizeFactor,
          mPixelSizePower,
          mOutputBitDepth)
    }
  }

  /**
   * Handle the permissions request response.
   */
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    PermissionUtils.onRequestPermissionResult(this@MainActivity, ::blendRenderer, permissions, grantResults)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.blend_parameters, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    ParametersDialogFragment().apply {
      arguments = Bundle().apply {
        // send parameters to fragment
        putDouble("altitude", mAltitude)
        putDouble("azimuth", mAzimuth)
        putSerializable("slope_type", mSlopeType)
        putSerializable("color_ramp_type", mColorRampType)
      }
      show(supportFragmentManager, "param_dialog")
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onPause() {
    super.onPause()
    mapView.pause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.dispose()
  }
}