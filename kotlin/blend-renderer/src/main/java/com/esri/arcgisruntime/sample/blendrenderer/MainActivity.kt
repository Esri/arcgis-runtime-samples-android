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
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.File

class MainActivity : AppCompatActivity(), ParametersDialogFragment.ParametersListener {

    private var mImageFile: File? = null
    private var mElevationFile: File? = null

    private var mAltitude: Int = 0
    private var mAzimuth: Int = 0
    private var mZFactor: Double = 0.toDouble()
    private var mSlopeType: SlopeType? = null
    private var mColorRampType: ColorRamp.PresetType? = null
    private var mPixelSizeFactor: Double = 0.toDouble()
    private var mPixelSizePower: Double = 0.toDouble()
    private var mOutputBitDepth: Int = 0

    override fun returnParameters(altitude: Int, azimuth: Int, slopeType: SlopeType?, colorRampType: ColorRamp.PresetType?) {
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
        mAltitude = 45
        mAzimuth = 315
        mZFactor = 0.000016
        mSlopeType = SlopeType.NONE
        mColorRampType = ColorRamp.PresetType.NONE
        mPixelSizeFactor = 1.0
        mPixelSizePower = 1.0
        mOutputBitDepth = 8
        // define permission to request
        val reqPermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val requestCode = 2
        // For API level 23+ request permission at runtime
        when {
            ContextCompat.checkSelfPermission(this@MainActivity,
                    reqPermission[0]) == PackageManager.PERMISSION_GRANTED -> blendRenderer()
            else -> // request permission
                ActivityCompat.requestPermissions(this@MainActivity, reqPermission, requestCode)
        }
    }

    /**
     * Using values stored in strings.xml, builds path to rasters.
     *
     * @return the path to raster file
     */
    // create the full path to the raster file
    private fun buildRasterPath(filename: String): String =
            (Environment.getExternalStorageDirectory().absolutePath + File.separator
                    + resources.getString(R.string.data_sdcard_offline_dir) + File.separator + filename + ".tif")

    /**
     * Creates new imagery and elevation files based on a given path, creates an ArcGISMap, sets it to a MapView and
     * calls updateRenderer().
     */
    private fun blendRenderer() {
        // create raster files
        mImageFile = File(buildRasterPath(this.getString(R.string.imagery_raster_name)))
        mElevationFile = File(buildRasterPath(this.getString(R.string.elevation_raster_name)))
        // create a map and it to a map view
        mapView.map = ArcGISMap()
        updateRenderer()
    }

    /**
     * Creates ColorRamp and BlendRenderer according to the chosen property values.
     */
    private fun updateRenderer() {
        // if color ramp type is not None, create a new ColorRamp
        val colorRamp = when {
            mColorRampType != ColorRamp.PresetType.NONE -> ColorRamp(mColorRampType!!, 800)
            else -> null
        }
        // create rasters
        val imageryRaster = Raster(mImageFile!!.absolutePath)
        val elevationRaster = Raster(mElevationFile!!.absolutePath)
        // if color ramp is not NONE, color the hillshade elevation raster instead of using satellite imagery raster color
        val rasterLayer = colorRamp?.run {  RasterLayer(elevationRaster)  } ?: RasterLayer(imageryRaster)
        mapView.map.basemap = Basemap(rasterLayer)
        // create blend renderer
        val blendRenderer = BlendRenderer(
                elevationRaster,
                listOf(9.0),
                listOf(255.0), null, null, null, null,
                colorRamp,
                mAltitude.toDouble(),
                mAzimuth.toDouble(),
                mZFactor,
                mSlopeType!!,
                mPixelSizeFactor,
                mPixelSizePower,
                mOutputBitDepth)
        rasterLayer.rasterRenderer = blendRenderer
    }

    /**
     * Handle the permissions request response.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when {
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> blendRenderer()
            else -> toast(R.string.location_permission_denied) // report to user that permission was denied
        }
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
                putInt("altitude", mAltitude)
                putInt("azimuth", mAzimuth)
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