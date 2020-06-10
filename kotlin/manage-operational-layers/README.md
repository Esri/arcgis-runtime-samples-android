# Manage operational layers

Add, remove, and reorder operational layers in a map.

![Image of manage operational layers](manage-operational-layers.png)

## Use case

Operational layers display the primary content of the map and usually provide dynamic content for the user to interact with (as opposed to basemap layers that provide context).

The order of operational layers in a map determines the visual hierarchy of layers in the view. You can bring attention to a specific layer by rendering it above other layers.

## How to use the sample

When the app starts, tap the floating action button to display lists of operational layers and any removed layers. Long press and drag an active layer to change its position, or swipe it to the side to remove it from the map. Tap removed layers to add them back to the map. The map will be updated automatically.

## How it works

1. Get the operational layers `LayerList` from the map using `map.operationalLayers`.
2. Add or remove layers using `layerList.add(layer)` and `layerList.remove(layer)` respectively. The last layer in the list will be rendered on top.

## Relevant API

* ArcGISMap
* ArcGISMapImageLayer
* LayerList

## Additional information

You cannot add the same layer to the map multiple times or add the same layer to multiple maps. Instead, clone the layer with `layer.clone()` before duplicating.

## Tags

add, delete, layer, map, remove
