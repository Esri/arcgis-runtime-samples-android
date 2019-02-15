# Min max scale

Restrict zooming to a specific scale range.

![Min max scale](set-min-max-scale.png)

## How it works

1. Create an ArcGIS map.
1. Set min and max scales of map, `ArcGISMap.maxScale = ...` and `ArcGISMap.minScale = ...`.
1. Set initial Viewpoint of map, `ArcGISMap.initialViewpoint = ...`
1. Set the ArcGIS map to the `MapView`.

## Relevant API

* ArcGISMap
* Basemap
* MapView
* Viewpoint

#### Tags

Maps and Scenes