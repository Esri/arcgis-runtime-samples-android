# Elevation at point

Get the elevation from a given point on a surface

![Elevation at point app](elevation-at-point.png)

## Use case

Knowing the elevation at a given point in a landscape can aid in navigation, planning and survey in the field.

## How to use the sample

Tap anywhere on the surface to get the elevation at that point.

## How it works

1. Create a `SceneView` and `Scene` with an imagery base map.
1. Set an `ArcGISTiledElevationService` as the elevation source of the scene's base surface.
1. Use the `screenToBaseSurface(screenPoint)` method on the scene view to convert the tapped screen point into a point on surface.
1. Use `getElevationAsync(surfacePoint)` on the base surface to asynchronously get the elevation.

## Relevant API

* ArcGISTiledElevationSource
* BaseSurface
* ElevationSourcesList
* SceneView

#### Tags

MapViews SceneViews and UI
