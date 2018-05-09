# Display Layer View State
View the status of the layers on the map. 

![Display Layer View State App](display-layer-view-state.png)

## How to use the sample
Pan and zoom on the map to view changes in status.

## How it works

1. The layer view state is obtained from the enum value of `LayerViewStatus`.
1. To access `LayerViewStatus`, add a `LayerViewStateChangedListener` to the `MapView`.
1. Call `LayerViewStateChangedEvent.getLayerViewStatus()` to get the status.

## Relevant API
* ArcGISMap
* LayerViewStateChangedEvent
* LayerViewStateChangedListener
* MapView

#### Tags
MapViews, SceneViews and UI
