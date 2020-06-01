# Animate 3D graphic

A camera follows a graphic while the graphic's position and rotation are animated.

![Image of animate 3D graphic](animate-3d-graphic.png)

## How to use the sample

Animation Controls (Top Left Corner):

* Select a mission -- selects a location with a route for plane to fly.
* Mission progress -- shows how far along the route the plane is. Slide to change keyframe in animation.
* Play/Stop -- toggles playing and stopping the animation.
* Follow/Free Cam -- toggles camera following plane.

Speed Slider (Top Right Corner):

* Controls speed of animation.

2D Map Controls (Bottom Left Corner):

* Plus and Minus -- controls distance of 2D view from ground level.

## How it works

1. Create a `ModelSceneSymbol` object.
2. Create a `Graphic` object and set its geometry to a `Point`.
3. Set the `ModelSceneSymbol` object to the graphic.
4. Add heading, pitch, and roll attributes to the graphic. Get the attributes from the graphic with `Graphic.attributes`.
5. Create a `SimpleRenderer` object and set its expression properties.
6. Add graphic and a renderer to the graphics overlay.
7. Create a `OrbitGeoElementCameraController` which is set to target the graphic.
8. Assign the camera controller to the `SceneView`.
9. Update the graphic's location, heading, pitch, and roll.

## Relevant API

* ArcGISScene
* Camera
* GlobeCameraController
* Graphic
* GraphicsOverlay
* ModelSceneSymbol
* OrbitGeoElementCameraController
* Renderer
* SceneProperties
* SceneView
* SurfacePlacement

## Tags

animation, camera, heading, pitch, roll, rotation, visualize
