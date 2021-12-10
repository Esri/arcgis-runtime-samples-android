package com.esri.arcgisruntime.sample.tokenauthentication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.tokenauthentication.databinding.ActivityMainBinding
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler

class MainActivity : AppCompatActivity() {

  private val activityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  private val mapView: MapView by lazy {
    activityMainBinding.mapView
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activityMainBinding.root)

    // set up an authentication handler to take credentials for access to the protected map service
    AuthenticationManager.setAuthenticationChallengeHandler(
      DefaultAuthenticationChallengeHandler(
        this
      )
    )

    // create a portal to ArcGIS Online
    Portal(getString(R.string.arcgis_online_portal_url)).let {
      // create a portal item using the portal and the item id of a protected map service
      PortalItem(it, getString(R.string.map_service_world_traffic_id))
    }.let {
      // create a map with the portal item
      ArcGISMap(it)
    }.let {
      // set the map to be displayed in the map view
      mapView.map = it
    }
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
