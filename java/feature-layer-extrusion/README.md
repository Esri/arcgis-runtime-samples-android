# Feature Layer Extrusion
This sample demonstrates how to apply extrusion to a renderer on a feature layer.

![Feature Layer Extrusion App](feature-layer-extrusion.png)

## Features
* FeatureLayer
* ExtrusionMode
* Renderer
* SceneProperties

## Developer Pattern

1. Create a `ServiceFeatureTable` from a web service and load all fields with `.queryFeaturesAsync(...)`.
1. Set the `ServiceFeatureTable` to a `FeatureLayer` and `.setFeatureRenderingMode(FeatureLayer.RenderingMode.DYNAMIC)`.
1. When definining the `FeatureLayer`'s `Renderer`, remember to `.setExtrusionMode(...)` on the `SceneProperties`.
1. Finally, also on `SceneProperties`, use `setExtrusionExpression("[SOME_FIELD]")` to a `Field` from the `ServiceFeatureTable`.
