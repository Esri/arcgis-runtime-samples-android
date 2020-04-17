# Surface placement

Position graphics relative to a surface using different surface placement modes.

![Image of surface placement](surface-placement.png)

## Use case

Depending on the use case, data might be displayed at an absolute height (e.g. flight data recorded with altitude information), at a relative height to the terrain (e.g. transmission lines positioned relative to the ground), at a relative height to objects in the scene (e.g. extruded polygons, integrated mesh scene layer), or draped directly onto the terrain (e.g. location markers, area boundaries).

## How to use the sample

The application loads a scene showing four points that use the individual surface placement rules (Absolute, Relative, Relative to Scene, and either Draped Billboarded or Draped Flat). Use the control to toggle the draped mode, then explore the scene by zooming in/out and by panning around to observe the effects of the surface placement rules.

## How it works

1. Create a `GraphicsOverlay` for each placement mode, setting `LayerSceneProperties.surfacePlacement` to:
    * `ABSOLUTE`, position graphic using only its Z value.
    * `RELATIVE`, position graphic using its Z value plus the elevation of the surface.
    * `DRAPED_BILLBOARDED`, position graphic upright on the surface and always facing the camera, not using its z value.
    * `DRAPED_FLAT`, position graphic flat on the surface, not using its z value.
    * `RELATIVE_TO_SCENE`, position graphic using the Z value of the scene layer.`
2. Add graphics to the graphics overlay, `GraphicsOverlay.graphics.add(graphic)`.
3. Add each graphics overlay to the scene view by calling `SceneView.graphicsOverlays.add(overlay)`.

## Relevant API

* Graphic
* GraphicsOverlay
* LayerSceneProperties.SurfacePlacement
* SceneProperties
* Surface

## About the data

The scene launches with a view of Brest, France. Four points are shown hovering with positions defined by each of the different surface placement modes.

## Additional information

This sample uses an elevation service to add elevation/terrain to the scene. Graphics are positioned relative to that surface for the `DRAPED_BILLBOARDED`, `DRAPED_FLAT`, and `RELATIVE` surface placement modes. It also uses a scene layer containing 3D models of buildings. Graphics are positioned relative to that scene layer for the `RELATIVE_TO_SCENE` surface placement mode.

## Tags

3D, absolute, altitude, draped, elevation, floating, relative, scenes, sea level, surface placement
