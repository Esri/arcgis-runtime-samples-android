# WMTS Layer
This sample demonstrates showing UI components responding to attribution view.

![WMTS Layer App](wmts-layer.png)

## Features
* Basemap
* ArcGISMap
* WmtsService
* WmtsLayer

## Developer Pattern
Create a `WmtsService` from a `String` url and load the service.  Once loaded use the service info to get the LayerInfo and use to create the `WmtsLayer`.  Add the layer to the map as a `Basemap`. 

```kotlin
 // create wmts service from url string
 val wmtsService = WmtsService(getString(R.string.wmts_url))
 wmtsService.addDoneLoadingListener({
     if(wmtsService.loadStatus == LoadStatus.LOADED){
         // get service info
         val wmtsServiceInfo = wmtsService.serviceInfo
         // get the first layers id
         val layerInfos = wmtsServiceInfo.layerInfos
         // create WMTS layer from layer info
         val wmtsLayer = WmtsLayer(layerInfos[0])
         // set the basemap of the map with WMTS layer
         map.basemap = Basemap(wmtsLayer)
     }
 })
 wmtsService.loadAsync()
```
