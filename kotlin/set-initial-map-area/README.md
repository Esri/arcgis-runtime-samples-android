# Set initial map area

Display the map at an initial viewpoint representing a bounding geometry.

![Image of map initial extent](set-initial-map-area.png)

## Use case

Setting the initial viewpoint is useful when a user wishes to first load the map at a particular area of interest.

## How to use the sample

As the application is loading, the initial view point is set and the map view opens at the given location.

## How it works

1. Instantiate an `ArcGISMap` object.
2. Instantiate a `Viewpoint` object using an `Envelope` object.
3. Set the starting location of the map with `map.initialViewpoint = viewpoint`.
4. Set the map to a `MapView` object.

## Relevant API

* ArcGISMap
* Envelope
* MapView
* Point
* Viewpoint


## Tags

extent, zoom
