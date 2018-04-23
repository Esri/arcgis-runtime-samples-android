# ArcGIS Tiled Layer URL

![Tiled Layer from URL App](arcgis-tiledlayer-from-url.png)

The ArcGIS Tile Layer URL app is the most basic Map app for the [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/en/android/) using Tiled Layer basemap from an ArcGIS Online service URL.  It shows how to inflate a MapView in the layout XML of the activity, create a Tiled Layer from an ArcGIS Online service URL and bind that to a `Basemap`.  The `Basemap` is used to create a `Map` which is used inside of the `MapView`.  By default, this map supports basic zooming and panning operations.

## Features
* ArcGISMap
* MapView
* ArcGISTiledLayer
* Basemap

## Developer pattern
```kotlin
// set the map to be displayed in the map view
mapView.map = ArcGISMap().apply {
  // create a basemap with a tiled layer from service url
  basemap = Basemap(ArcGISTiledLayer(resources.getString(R.string.world_topo_service)))
}
```
