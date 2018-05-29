# ArcGIS Tiled Layer URL
Load an ArcGIS Tiled Layer from a URL.

![Tiled Layer from URL App](arcgis-tiledlayer-url.png)

## How to use the sample
Simply run the app.

## How it works
An `ArcGISTiledLayer` from an ArcGIS Online service URL is added to the `ArcGISMap` as an operational layer.

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

## Relevant API
* ArcGISMap
* ArcGISTiledLayer
* Basemap
* MapView

#### Tags
Layers