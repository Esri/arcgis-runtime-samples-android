# WMTS Layer
 Display a layer from a Web Map Tile Service. 
 
 ![WMTS Layer App](wmts-layer.png)

## How to use the sample
Simply run the app
 
## How it works
To display a `WmtsLayer` from a `WmtsService`:
 
1. Create a `WmtsService` using the URL of the WMTS Service.
1. After loading the WmtsService, get the list of `WmtsLayerInfos` from the service info: `service.getServiceInfo().getLayerInfos()`
1. For the layer you want to display, get the layer ID using `getLayerInfos().get(0).getId()`
1. Use the LayerInfo to create the WMTSLayer: `new WmtsLayer(layerInfos.get(0))`
1. Set it as the maps' basemap with `map.setBasemap(new Basemap(wmtsLayer))`

## Relevant API
* ArcGISMap
* Basemap
* MapView
* WmtsLayer
* WmtsService

#### Tags
Layers
