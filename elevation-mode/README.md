# Elevation Mode

Demonstrates how to position graphics using different elevation modes.

![Elevation Mode App](elevation-mode.png)


## How it works

To position `Graphics` using `SurfacePlacement` (DRAPED, RELATIVE, ABSOLUTE):

1. Create a GraphicsOverlay.
2. Set the surface placement mode `GraphicsOverlay.getSceneProperties().setSurfacePlacement(SurfacePlacement.      )`
- Draped, Z value of graphic has no affect and graphic is attached to surface
- Absolute, position graphic using only it's Z value
- Relative, position graphic using it's Z value plus the elevation of the surface
3. Add graphics to the graphics overlay, GraphicsOverlay.getGraphics.add(Graphic).
4. Add the graphics overlay to the SceneView, SceneView.getGraphicsOverlays().add(GraphicsOverlay).

## Features
* ArcGISScene
* Camera
* Graphic
* GraphicsOverlay
* LayerSceneProperties.SurfacePlacement
* SceneProperties
* SceneView
* Surface