package com.esri.arcgisruntime.sample.customdictionarystyle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.symbology.DictionaryRenderer
import com.esri.arcgisruntime.symbology.DictionarySymbolStyle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a new map with a streets basemap and set it to the map view
    mapView.map = ArcGISMap(Basemap.createStreetsVector()).apply {
      // create the restaurants feature layer from a service feature table
      FeatureLayer(ServiceFeatureTable(getString(R.string.restaurants_url))).let { featureLayer ->
        // set a dictionary renderer to the restaurant layer made from a custom style file
        featureLayer.renderer =
          DictionaryRenderer(DictionarySymbolStyle.createFromFile(
            getExternalFilesDir(getString(R.string.restaurant_stylx_path))?.path))
        // once the feature layer is loaded
        featureLayer.addDoneLoadingListener {
          // set the map view's viewpoint to the feature layer extent
          mapView.setViewpointAsync(Viewpoint(featureLayer.fullExtent))
        }
        // add the the restaurant feature layer to the map's operational layers
        operationalLayers.add(featureLayer)
      }
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
