# Feature layer rendering mode (map)

Render features statically or dynamically by setting the feature layer rendering mode.

![Image of feature layer rendering mode map](feature-layer-rendering-mode-map.png)

## Use case

In dynamic rendering mode, features and graphics are stored on the GPU. As a result, dynamic rendering mode is good for moving objects and for maintaining graphical fidelity during extent changes, since individual graphic changes can be efficiently applied directly to the GPU state. This gives the map or scene a seamless look and feel when interacting with it. The number of features and graphics has a direct impact on GPU resources, so large numbers of features or graphics can affect the responsiveness of maps or scenes to user interaction. Ultimately, the number and complexity of features and graphics that can be rendered in dynamic rendering mode is dependent on the power and memory of the device's GPU.

In static rendering mode, features and graphics are rendered only when needed (for example, after an extent change) and offloads a significant portion of the graphical processing onto the CPU. As a result, less work is required by the GPU to draw the graphics, and the GPU can spend its resources on keeping the UI interactive. Use this mode for stationary graphics, complex geometries, and very large numbers of features or graphics. The number of features and graphics has little impact on frame render time, meaning it scales well, and pushes a constant GPU payload. However, rendering updates is CPU and system memory intensive, which can have an impact on device battery life.

## How to use the sample

Use the 'Animated Zoom' button to trigger the same zoom animation on both static and dynamic maps.

## How it works

1. Create an `ArcGISMap` and call `getLoadSettings()` and then `setPreferred[Point/Polyline/Polygon]FeatureRenderingMode(...)`.
2. The `RenderingMode` can be set to `STATIC`, `DYNAMIC` or `AUTOMATIC`.
   * In Static rendering mode, the number of features and graphics has little impact on frame render time, meaning it scales well, however points don't stay screen-aligned and point/polyline/polygon objects are only redrawn once map view navigation is complete.
   * In Dynamic rendering mode, large numbers of features or graphics can affect the responsiveness of maps or scenes to user interaction, however points remain screen-aligned and point/polyline/polygon objects are continually redrawn while the map view is navigating.
3. When left to automatic rendering, points are drawn dynamically and polylines and polygons statically.

## Relevant API

* ArcGISMap
* FeatureLayer
* FeatureLayer.RenderingMode
* LoadSettings
* MapView

## Tags

dynamic, feature layer, features, rendering, static