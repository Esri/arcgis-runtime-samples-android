/* Copyright 2020 Esri
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
package com.esri.arcgisruntime.sample.rasterlayerfile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.raster.Raster
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // define permission to request
    val reqPermission =
      arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val requestCode = 2
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(
        this@MainActivity,
        reqPermission[0]
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      loadRaster()
    } else { // request permission
      ActivityCompat.requestPermissions(this@MainActivity, reqPermission, requestCode)
    }
  }

//  /**
//   * Using values stored in strings.xml, builds path to Shasta.tif.
//   *
//   * @return the path to raster file
//   */
//  private fun buildRasterPath(): String { // get sdcard resource name
//    val extStorDir = getExternalFilesDir(null)?.path + getString(R.string.raster_folder)
//    // get the directory
//    val extSDCardDirName = this.resources.getString(R.string.raster_folder)
//    // get raster filename
//    val filename = this.getString(R.string.shasta)
//    // create the full path to the raster file
//    return (extStorDir.absolutePath
//        + File.separator
//        + extSDCardDirName
//        + File.separator
//        + filename
//        + ".tif")
//  }

  /**
   * Loads Shasta.tif as a Raster and adds it to a new RasterLayer. The RasterLayer is then added
   * to the map as an operational layer. Map viewpoint is then set based on the Raster's geometry.
   */
  private fun loadRaster() { // create a raster from a local raster file
    val raster =
      Raster(getExternalFilesDir(null)?.path + getString(R.string.raster_folder) + getString(R.string.shasta) + ".tif")
    // create a raster layer
//    val rasterLayer = RasterLayer(raster)
    // create a Map with imagery basemap
    val map = ArcGISMap(Basemap.createImagery())
    // add the map to a map view
    mapView.map = map
    // add the raster as an operational layer
//    map.operationalLayers.add(rasterLayer)
    // set viewpoint on the raster
//    rasterLayer.addDoneLoadingListener {
//      mapView.setViewpointGeometryAsync(
//        rasterLayer.fullExtent,
//        50.0
//      )
//    }
  }

  /**
   * Handle the permissions request response.
   */
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      loadRaster()
    } else { // report to user that permission was denied
      Toast.makeText(
        this@MainActivity,
        resources.getString(R.string.raster_write_permission_denied),
        Toast.LENGTH_SHORT
      ).show()
    }
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