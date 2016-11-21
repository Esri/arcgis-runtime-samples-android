# Change Sub Layer Visibility

![Sub Layer Visibility App](sub-layer-viz.png)

The **Change Sub Layer Visibility** sample demonstrates how to add multiple layers to your ```Map``` using a ```BasemapType``` and an ```ArcGISMapImageLayer``` which as multiple sub-layers.  The app allows you to turn on or off the sub-layers from the ```ArcGISMapImageLayer```.  You gain access to the sub-layers from the ```ArcGISMapImageLayer.getSubLayers()``` method which returns a ```SubLayerList```.  The ```SubLayerList``` is a modifiable list of ```ArcGISSubLayers``` which gives you access to determine if the layer is visible or not and to turn on or off the layers visibility.

## Features
* ArcGISMap
* ArcGISMapImageLayer
* SubLayerList
