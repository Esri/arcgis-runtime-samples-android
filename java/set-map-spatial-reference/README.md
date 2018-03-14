# Set Map Spatial Reference

![Set Map Spatial Reference App](set-map-spatial-reference.png)

The Set Map Spatial Reference sample app demonstrates how to create a `Map` with the **WORLD_BONNE** equal-area projection that has a true scale along the central meridian and all parallels.  The sample creates an `ArcGISMapImageLayer` that can reproject itself to the `Map`'s spatial reference.  Not all layer types can be reprojected, like `ArcGISTiledLayer`, and will fail to draw if their spatial reference is not the same as the `Map`'s spatial reference.

## Features

* ArcGISMap
* ArcGISMapImageLayer
* MapView
* SpatialReference