# Web Tiled Layer
Display map tiles from Stamen terrain as an online resource using the `WebTiledLayer`. `WebTiledLayer` provides a simple way to integrate non-ArcGIS Services as a layer in a map.

![Web Tiled Layer App](web-tiledlayer.png)

## How to use the sample
Simply run the app.

## How it works
In this case, map tiles from Stamen are added to the map. The template URL is specified by setting the subDomains, level, col, and row attributes. Additionally, copyright information is added to the layer so that the layer can be properly attributed. The layer is added to a `Basemap`, and a `Basemap` is added to a `ArcGISMap`. Finally, the Map is set on the `MapView`, and the tiled layer is displayed.

## Relevant API
* ArcGISMap
* Basemap
* MapView
* WebTiledLayer

## License
Map tile sets are provided For Terrain: Map tiles by Stamen Design, under CC BY 3.0. Data by OpenStreetMap, under ODbL.

#### Tags
Layers