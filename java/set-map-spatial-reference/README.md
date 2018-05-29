# Set Map Spatial Reference
Create a map with a specific spatial reference.

![Set Map Spatial Reference App](set-map-spatial-reference.png)

## How to use the sample
Simply run the app.

## How it works
1. Create a `MapView`.
1. Create an `ArcGISMap` passing a specific `SpatialReference` as an argument.

NOTE: Not all layer types can be reprojected, like `ArcGISTiledLayer`, which will fail to draw if their spatial reference is not the same as the `ArcGISMap`'s spatial reference.

## Relevant API
* ArcGISMap
* ArcGISMapImageLayer
* MapView
* SpatialReference

#### Tags
Maps and Scenes