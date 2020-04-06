# Feature layer extrusion

Extrude features based on their attributes.

![Image of feature layer extrusion](feature-layer-extrusion.png)

## Use case

Extrusion is the process of stretching a flat, 2D shape vertically to create a 3D object in a scene. For example, you can extrude building polygons by a height value to create three-dimensional building shapes.

## How to use the sample

Tap the toggle to switch between using population density and total population for extrusion. Higher extrusion directly corresponds to higher attribute values.

## How it works

1. Create a `ServiceFeatureTable` from a URL.
2. Create a feature layer from the service feature table.
   * Make sure to set the rendering mode to dynamic, `statesFeatureLayer.renderingMode = RenderingMode.DYNAMIC`.
3. Apply a `SimpleRenderer` to the feature layer.
4. Set the `ExtrusionMode` of the renderer with `renderer.sceneProperties.extrusionMode = SceneProperties.ExtrusionMode.BASE_HEIGHT`.
5. Set the extrusion expression of the renderer with `renderer.sceneProperties.extrusionExpression = "[POP2007]/ 10"`.

## Relevant API

* FeatureLayer
* Renderer.SceneProperties
* ServiceFeatureTable
* SimpleRenderer

## Tags

3D, extrude, extrusion, extrusion expression, height, renderer, scene
