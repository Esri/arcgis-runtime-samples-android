package com.esri.arcgisruntime.sample.offlinegeocode

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.esri.arcgisruntime.tasks.geocode.ReverseGeocodeParameters
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // set the map to the map view
    mapView.map = ArcGISMap()
    // add a graphics overlay to the map view
    val graphicsOverlay = GraphicsOverlay()
    mapView.graphicsOverlays.add(graphicsOverlay)
    // create a point symbol for showing the address location
    val pointSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 20.0f)
    // add a touch listener to the map view
    //TODO

    // load the tile cache from local storage
    val tileCache =
      TileCache(getExternalFilesDir(null)?.path + getString(R.string.san_diego_tpk))
    // use the tile cache extent to set the view point
    tileCache.addDoneLoadingListener { mapView.setViewpoint(Viewpoint(tileCache.fullExtent)) }
    // create a tiled layer and add it to as the base map
    val tiledLayer = ArcGISTiledLayer(tileCache)
    mapView.map.basemap = Basemap(tiledLayer)
    // create geocode parameters
    val geocodeParameters = GeocodeParameters().apply {
      resultAttributeNames.add("*")
      maxResults = 1
    }
    // create reverse geocode parameters
    val reverseGeocodeParameters = ReverseGeocodeParameters().apply {
      resultAttributeNames.add("*")
      outputSpatialReference = mapView.map.spatialReference
      maxResults = 1
    }
    // load the locator task from external storage
    val locatorTask = LocatorTask(
      getExternalFilesDir(null)?.path + resources.getString(R.string.san_diego_loc)
    )
    locatorTask.loadAsync()
  }
}
