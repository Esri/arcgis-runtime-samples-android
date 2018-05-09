# Set Initial Map Area
Start a `Map` app with a defined initial area using a `Viewpoint` created with an `Envelope` which defines the initial area.

![Set Initial Map Area App](set-initial-map-area.png)

## How to use the sample
Simply run the app.

## How it works
1. Create a `MapView`.
1. Create an `ArcGISMap`.
1. Create a `Viewpoint` from an `Envelope`.
1. Use `ArcGISMap.setInitialViewpoint(...)` and pass the `Viewpoint` as an argument.
1. Set the `ArcGISMap` to the `MapView`.

## Relevant API
* ArcGISMap
* MapView
* SpatialReference
* Envelope
* Viewpoint

#### Tags
Maps and Scenes
