package com.esri.arcgisruntime.sample.customdictionarystyle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.symbology.DictionaryRenderer
import com.esri.arcgisruntime.symbology.DictionarySymbolStyle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create a feature layer from a service feature table
    val featureTable = ServiceFeatureTable(getString(R.string.restaurants_url))
    val featureLayer = FeatureLayer(featureTable).apply {
      // use a custom style to create a dictionary renderer and set it to the feature layer renderer
      renderer = DictionaryRenderer(
        DictionarySymbolStyle.createFromFile(
          getExternalFilesDir(null)?.path + getString(R.string.restaurant_stylx_path)
        )
      )
    }

    // set the map view's viewpoint to the feature layer extent when loaded
    featureLayer.addDoneLoadingListener {
      mapView.setViewpointAsync(Viewpoint(featureLayer.fullExtent))
    }

    // create a new map with a streets basemap and set it to the map view
    mapView.map = ArcGISMap(BasemapStyle.ARCGIS_STREETS).apply {
      // add the the feature layer to the map's operational layers
      operationalLayers.add(featureLayer)
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
