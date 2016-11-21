# ArcGIS Tiled Layer URL

![Tiled Layer from URL App](tiledlayer-from-url.png)

The ArcGIS Tile Layer URL app is the most basic Map app for the [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/en/android/) using Tiled Layer basemap from an ArcGIS Online service URL.  It shows how to inflate a MapView in the layout XML of the activity, create a Tiled Layer from an ArcGIS Online service URL and bind that to a `Basemap`.  The `Basemap` is used to create a `Map` which is used inside of the `MapView`.  By default, this map supports basic zooming and panning operations.

## Features
* ArcGISMap
* MapView
* ArcGISTiledLayer
* Basemap

## Developer pattern
```java
// create new Tiled Layer from service url
ArcGISTiledLayer tiledLayerBaseMap = new ArcGISTiledLayer(getResources().getString(R.string.world_topo_service));
// set tiled layer as basemap
Basemap basemap = new Basemap(tiledLayerBaseMap);
// create a map with the basemap
ArcGISMap map = new ArcGISMap(basemap);
// set the map to be displayed in this view
mMapView.setMap(map);
```
