# Animate 3D Graphic

Animate a graphic's position and orientation and follow it with the camera.

![Animate 3D Graphic App](animate-3d-graphic.png)

## How to use the sample

Animation Controls (Top Left Corner):
* Select a mission -- selects a location with a route for plane to fly.
* Mission progress -- shows how far along the route the plane is. Slide to change keyframe in animation.
* Play -- toggles playing and stopping the animation.
* Follow -- toggles camera following plane.

Speed Slider (Top Right Corner):
* Controls speed of animation.

2D Map Controls (Bottom Left Corner):
* Plus and Minus -- controls distance of 2D view from ground level.

Moving the Camera: 
* Simply use regular zoom and pan interactions with the mouse. When in follow mode, the `OrbitGeoElementCameraController` will keep the camera locked to the plane.

## How it works

To animate a `Graphic` by updating it's `Geometry` object's, heading, pitch, and roll:

1. Create a `GraphicsOverlay` and attach it to the `SceneView`.
1. Create a `ModelSceneSymbol` with `AnchorPosition.CENTER`.
1. Create a `Graphic(Geometry, Symbol)`.
   * set the `Geometry` to a `Point` where the `Graphic` will be located in the `SceneView`.
   * set the `Symbol` to the one we made above.
1. Add Attributes to graphic.
   * Get attributes from graphic, `Graphic.getAttributes()`.
   * Add heading, pitch, and roll attribute, `attributes.put("[HEADING]", heading)`.
1. Create a `SimpleRenderer` to access and set it's expression properties.
   * Access properties with `Renderer.getSceneProperties()`.
   * Set heading, pitch, and roll expressions, `SceneProperties.setHeadingExpression("[HEADING]")`.
1. Add `Graphic` to the `GraphicsOverlay`.
1. Set `Renderer` to the `GraphicsOverlay`, `GraphicsOverlay.setRenderer(Renderer)`.
1. Update the `Graphic` object's location, `Graphic.setGeometry(Point)`.
1. Update `Graphic` object's heading, pitch, and roll, `attributes.replace("[HEADING]", heading)`.

## Relevant API

* Camera
* GlobeCameraController
* Graphic
* GraphicsOverlay
* LayerSceneProperties.SurfacePlacement
* ModelSceneSymbol
* OrbitGeoElementCameraController
* Renderer
* SceneView
* Viewpoint

#### Tags
Visualization