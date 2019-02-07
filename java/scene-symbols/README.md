# Scene symbols

Show various kinds of 3D symbols in a scene.

![Scene symbols app](scene-symbols.png)

## How to use the sample

Various symbols will be shown in the scene when it loads.

## How it works

1. Create a `GraphicsOverlay`.
1. Create `SimpleMarkerSceneSymbol(Styler, color, width, height, depth, AnchorPosition)` objects.
1. Pass the scene symbol object to `Graphic(geometry, symbol).`
1. Add the graphics to the graphics overlay.
1. Add the graphics overlay to the scene view.

## Relevant API

* SimpleMarkerSceneSymbol
* SimpleMarkerSceneSymbol.Style
* SceneSymbol.AnchorPosition

## Tags
Visualization
