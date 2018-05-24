# Display Drawing Status
Use the `DrawStatus` value representing drawing state of the `MapView` to display an Android `ProgressBar` while the map is loading.

![Display Drawing Status App](display-drawing-status.png)

## How to use the sample
Simply run the app.

## How it works
1. Create a `MapView` and set a `DrawStatusChangedListener`.
1. Use `getDrawStatus` on the `DrawStatusChangedEvent` to determine draw status.

## Relevant API
* ArcGISMap
* MapView
* ServiceFeatureTable
* DrawStatus
* DrawStatusChangedEvent
* DrawStatusChangedListener

#### Tags
MapViews, SceneViews and UI