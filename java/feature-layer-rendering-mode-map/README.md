# Feature Layer Rendering Mode (Map)
### Category: MapViews, SceneViews and UI
This sample demonstrates how to use load settings to set preferred rendering mode for feature layers, specifically static or dynamic rendering modes.

![Feature Layer Rendering Mode App](feature-layer-rendering-mode-map.png)

## Features
* FeatureLayer
* FeatureLayer.RenderingMode
* LoadSettings

## Developer Pattern

1. Create an `ArcGISMap` and call `.getLoadSettings()` and then `.setPreferred[Point/Polyline/Polygon]FeatureRenderingMode(...)`.
1. The `RenderingMode` can be set to `STATIC`, `DYNAMIC` or `AUTOMATIC`.
1. `RenderingMode.STATIC` generally has better performance, however `Point`s don't stay screen-aligned and `Point`s/`Polyline`s/`Polygon`s are only redrawn once `MapView` navigation is complete.
1. `RenderingMode.DYNAMIC` generally has worse performance, however `Point`s remain screen-aligned and `Point`s/`Polyline`s/`Polygon`s are continually redrawn while the `MapView`  is navigating.
1. When left to `RenderingMode.AUTOMATIC`, `Point`s are drawn dynamically and `Polyline`s and `Polygon`s statically.
