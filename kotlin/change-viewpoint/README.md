# Change viewpoint

Set the map view to a new viewpoint.

![Image of change viewpoint](change-viewpoint.png)

## Use case

Programmatically navigate to a specified location in the map or scene. Use this to focus on a particular point or area of interest.

## How to use the sample

The map view has several methods for setting its current viewpoint. Select a viewpoint from the UI to see the viewpoint changed using that method.

## How it works

1. Create a new `ArcGISMap` object and set it to the `MapView` object.
2. Change the map's `Viewpoint` using one of the available methods:
   * Use `MapView.setViewpointWithDurationAsync()` to pan to a viewpoint over the specified length of time.
   * Use `MapView.setViewpointCenterAsync()` to center the viewpoint on a `Point` and set a distance from the ground using a scale.
   * Use `MapView.setViewpointGeometryAsync()` to set the viewpoint to a given `Geometry`.

## Relevant API

* ArcGISMap
* Geometry
* MapView
* Point
* Viewpoint

## Additional information

Below are some other ways to set a viewpoint:

* setViewpoint
* setViewpointAsync
* setViewpointCenterAsync
* setViewpointGeometryAsync
* setViewpointRotationAsync
* setViewpointScaleAsync

## Tags

animate, extent, pan, rotate, scale, view, zoom
