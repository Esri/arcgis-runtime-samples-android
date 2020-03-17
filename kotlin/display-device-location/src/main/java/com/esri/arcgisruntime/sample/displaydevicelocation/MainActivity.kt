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
package com.esri.arcgisruntime.sample.displaydevicelocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
  private val locationDisplay: LocationDisplay by lazy { mapView.locationDisplay }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    mapView.map = ArcGISMap(Basemap.createImagery())
    // Listen to changes in the status of the location data source.
    locationDisplay.addDataSourceStatusChangedListener { checkDataSource(it) }
    // Populate the list for the Location display options for the spinner's Adapter
    val list = arrayListOf(
      ItemData("Stop", R.drawable.locationdisplaydisabled),
      ItemData("On", R.drawable.locationdisplayon),
      ItemData("Re-Center", R.drawable.locationdisplayrecenter),
      ItemData("Navigation", R.drawable.locationdisplaynavigation),
      ItemData("Compass", R.drawable.locationdisplayheading)
    )

    spinner.apply {
      adapter = SpinnerAdapter(this@MainActivity, R.layout.spinner_layout, R.id.txt, list)
      onItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(
          parent: AdapterView<*>?,
          view: View,
          position: Int,
          id: Long
        ) {
          when (position) {
            0 ->  // Stop Location Display
              if (locationDisplay.isStarted) locationDisplay.stop()
            1 ->  // Start Location Display
              if (!locationDisplay.isStarted) locationDisplay.startAsync()
            2 -> {
              // Re-Center MapView on Location
              locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
              if (!locationDisplay.isStarted) locationDisplay.startAsync()
            }
            3 -> {
              // Start navigation mode
              locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
              if (!locationDisplay.isStarted) locationDisplay.startAsync()
            }
            4 -> {
              // Start compass navigation mode
              locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.COMPASS_NAVIGATION
              if (!locationDisplay.isStarted) locationDisplay.startAsync()
            }
          }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
      }
    }

    // move the spinner above the attribution bar
    mapView.addAttributionViewLayoutChangeListener { view, _, _, _, _, _, oldTop, _, oldBottom ->
      spinner.y -= view.height - (oldBottom - oldTop)
    }
  }

  private fun checkDataSource(dataSourceStatusChangedEvent: LocationDisplay.DataSourceStatusChangedEvent){
    // If LocationDisplay started OK, then continue.
    if (dataSourceStatusChangedEvent.isStarted) return
    // No error is reported, then continue.
    if (dataSourceStatusChangedEvent.error == null) return
    // If an error is found, handle the failure to start.
    // Check permissions to see if failure may be due to lack of permissions.
    requestPermissions(dataSourceStatusChangedEvent)
  }

  /**
   * Request fine and coarse location permissions for API level 23+.
   */
  private fun requestPermissions(dataSourceStatusChangedEvent: LocationDisplay.DataSourceStatusChangedEvent) {
    val requestCode = 2
    val reqPermissions = arrayOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
    )
    // fine location permission
    val permissionCheckFineLocation =
      ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[0]) ==
          PackageManager.PERMISSION_GRANTED
    // coarse location permission
    val permissionCheckCoarseLocation =
      ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[1]) ==
          PackageManager.PERMISSION_GRANTED
    if (!(permissionCheckFineLocation && permissionCheckCoarseLocation)) { // If permissions are not already granted, request permission from the user.
      ActivityCompat.requestPermissions(this@MainActivity, reqPermissions, requestCode)
    } else { // Report other unknown failure types to the user - for example, location services may not
      // be enabled on the device.
      val message = String.format(
        "Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent
          .source.locationDataSource.error.message
      )
      Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
      // Update UI to reflect that the location display did not actually start
      spinner.setSelection(0, true)
    }
  }

  /**
   * Handle the permissions request response.
   */
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) { // If request is cancelled, the result arrays are empty.
    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Location permission was granted. This would have been triggered in response to failing to start the
// LocationDisplay, so try starting this again.
      locationDisplay.startAsync()
    } else { // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
// request permission UX will be shown again, option should be shown to allow never showing the UX again.
// Alternative would be to disable functionality so request is not shown again.
      Toast.makeText(
        this@MainActivity,
        resources.getString(R.string.location_permission_denied),
        Toast.LENGTH_SHORT
      ).show()
      // Update UI to reflect that the location display did not actually start
      spinner.setSelection(0, true)
    }
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
