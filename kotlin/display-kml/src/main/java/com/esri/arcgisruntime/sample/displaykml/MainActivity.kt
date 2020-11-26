/*
 * Copyright 2020 Esri
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

package com.esri.arcgisruntime.sample.displaykml

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.layers.KmlLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.loadable.Loadable
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.ogc.kml.KmlDataset
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val TAG: String = MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // authentication with an API key or named user is required to access basemaps and other 
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // show progress indicator when app starts
    progressIndicator.visibility = View.VISIBLE

    // create a map with the dark gray canvas basemap
    val map = ArcGISMap(BasemapStyle.ARCGIS_DARK_GRAY).apply {
      // set initial view point
      initialViewpoint = Viewpoint(39.0, -98.0, 100000000.0)
    }
    // set the map to the map view
    mapView.map = map
    // prompt user to make a KML source selection when the app has loaded
    map.addDoneLoadingListener {
      if (map.loadStatus == LoadStatus.LOADED) {
        Toast.makeText(applicationContext, R.string.user_prompt, Toast.LENGTH_LONG).show()
        // hide progress indicator once map has loaded
        progressIndicator.visibility = View.GONE
      }
    }
  }

  /**
   * Shows progress indicator if the KML Layer is loading, clears all operational layers and adds
   * the KML layer to the map as an operational layer.
   *
   * @param kmlLayer to add to the map
   */
  private fun display(kmlLayer: KmlLayer) {
    // show progress indicator if kml dataset is loading
    progressIndicator.visibility = View.VISIBLE
    // hide progress indicator when kml dataset has loaded
    kmlLayer.addDoneLoadingListener {
      if (kmlLayer.loadStatus == LoadStatus.LOADED) {
        progressIndicator.visibility = View.GONE
      }
    }
    // clear operational layers before adding the KML layer to the map
    val operationalLayers = mapView.map.operationalLayers
    operationalLayers.clear()
    operationalLayers.add(kmlLayer)
  }

  /**
   * Display a KML layer from a URL.
   */
  private fun changeSourceToURL() {
    // create a KML data set from a URL
    val kmlDataset = KmlDataset(getString(R.string.noaa_weather_kml_url))
    // create a KML layer created from the KML data set and display it on the map
    val kmlLayer = KmlLayer(kmlDataset)
    display(kmlLayer)
    // report layers if failed to load
    reportErrors(kmlLayer, "Failed to load KML layer from URL")
  }

  /**
   * Display a KML layer from a portal item.
   */
  private fun changeSourceToPortalItem() {
    // create a portal to ArcGIS Online
    val portal = Portal(getString(R.string.arcgis_online_url))
    // create a portal item from a KML item id
    val portalItem = PortalItem(portal, getString(R.string.kml_item_id))

    // a KML layer created from an ArcGIS Online portal item
    val kmlLayer = KmlLayer(portalItem)
    display(kmlLayer)
    // report layers if failed to load
    reportErrors(kmlLayer, "Failed to load KML layer from portal item")
  }

  /**
   * Display a kml layer from external storage.
   */
  private fun changeSourceToFileExternalStorage() {
    // get the data set stored locally in device external storage
    val file = getExternalFilesDir(null)?.path + getString(R.string.kml_path)
    val kmlDataset = KmlDataset(file)
    // create a KML layer from the locally stored data set
    val kmlLayer = KmlLayer(kmlDataset)
    display(kmlLayer)
    // report layers if failed to load
    reportErrors(kmlLayer, "Failed to load kml data set from external storage: $file ")
  }

  /**
   * Reports any loading errors of the KML datasets
   *
   * @param kmlData any loadable containing a kml data set e.g. KmlDataset or KmlLayer
   * @param string the error message to display
   */
  private fun reportErrors(kmlData: Loadable, string: String) {
    // report errors if failed to load
    kmlData.addDoneLoadingListener {
      if (kmlData.loadStatus != LoadStatus.LOADED) {
        // remove the progress indicator
        progressIndicator.visibility = View.GONE
        // report the error
        val error = string + kmlData.loadError.message
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.kml_sources, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.kmlFromUrl -> changeSourceToURL()
      R.id.kmlFromPortal -> changeSourceToPortalItem()
      R.id.kmlFromExternalStorage -> changeSourceToFileExternalStorage()
      else -> return super.onOptionsItemSelected(item)
    }
    return true
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
