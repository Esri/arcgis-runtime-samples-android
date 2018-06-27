# Style WMS Layer
Shows how to change the style of a WMS layer.

![Style WMS Layer App](style-wms-layer.png)

## How to use the sample
Click the toggle button at the bottom of the screen to switch between styles.

## How it works
1. Create a `WmsLayer` specifying the URL of the service and the layer names you want with `new WmsLayer(url, names)`.
1. When the layer is done loading, get its list of style strings using `wmsLayer.getSublayers().get(0).getSublayerInfo().getStyles()`.
1. Set one of the styles using `wmsLayer.getSublayers().get(0).setCurrentStyle(styleString)`.

## Relevant API
* WmsLayer

#### Tags
Visualization
