# Change Sublayer Visibility
Toggle visibility of the map's sublayers.

![Change Sub Layer Visibility App](change-sublayer-visibility.png)

## How to use the sample
Use the options menu in the upper right to toggle visibility of the Cities, Continents and World sublayers on and off.

## How it works
Add multiple layers to an `ArcGISMap` including a `Basemap.Type` and an `ArcGISMapImageLayer` which has multiple sub-layers.  The app allows you to turn the sub-layers from the `ArcGISMapImageLayer` on or off.  You gain access to the sub-layers from the `ArcGISMapImageLayer.getSubLayers()` method which returns a `SubLayerList`.  The `SubLayerList` is a modifiable list of `ArcGISSubLayers` which gives you access to determine if the layer is visible or not and to turn on or off the layers visibility.

## Relevant API
* ArcGISMap
* ArcGISMapImageLayer
* SublayerList

#### Tags
Visualization
