# Get elevation at point

Get the elevation for a given point on a surface.

![Get elevation at point app](get-elevation-at-point.png)

## Use case

Knowing the elevation at a given point in a landscape can aid in navigation, planning and survey in the field.

## How to use the sample

Tap anywhere on the surface to get the elevation at that point.

## How it works

1. Create a `SceneView` and `Scene` with an imagery base map.
1. Set an `ArcGISTiledElevationSource` as the elevation source of the scene's base surface.
1. Use the `screenToBaseSurface(screenPoint)` method on the scene view to convert the tapped screen point into a point on surface.
1. Use the `getElevationAsync(surfacePoint)` method on the base surface to asynchronously get the elevation.

## Relevant API

* ArcGISTiledElevationSource
* BaseSurface
* ElevationSourcesList
* SceneView

## Additional information

`getElevationAsync(surfacePoint)` retrieves the most accurate available elevation value at a given point which requires it to go to the server or local raster file and load the highest level of detail of data for the target location and return the elevation value.

If multiple elevation sources are present in the surface the top most visible elevation source with a valid elevation in the given location is used to determine the result.

#### Tags
MapViews, SceneViews and UI
