# Surface Placement
Position graphics above a surface using different Surface Placements.

![Elevation Mode App](surface-placement.png)

## How to use the sample
Simply run the app.

## How it works
1. Create a GraphicsOverlay.
2. Set the surface placement mode `GraphicsOverlay.getSceneProperties().setSurfacePlacement(...)`
	- `DRAPED`, Z value of graphic has no affect and graphic is attached to surface
	- `RELATIVE`, position graphic using it's Z value plus the elevation of the surface
	- `ABSOLUTE`, position graphic using only it's Z value
3. Add graphics to the graphics overlay, GraphicsOverlay.getGraphics.add(Graphic).
4. Add the graphics overlay to the SceneView, SceneView.getGraphicsOverlays().add(GraphicsOverlay).

## Relevant API
* ArcGISScene
* Camera
* Graphic
* GraphicsOverlay
* LayerSceneProperties.SurfacePlacement
* SceneProperties
* SceneView
* Surface

#### Tags
Maps and Scenes
