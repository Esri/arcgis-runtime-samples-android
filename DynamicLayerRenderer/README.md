# Dynamic Layer Renderer
The ```DynamicLayerMapService``` sample illustrates how you can use the Dynamic Layer to render on the client side using the Dynamic Map Server. It is important to note that the ```DynamicLayerMapService``` executes in a separate thread from the main thread of the application. This allows the main thread to stay responsive while the query is being executed on the server and in the background. 

## Features
* [ArcGISDynamicLayerMapService](https://developers.arcgis.com/android/api-reference/reference/com/esri/android/map/ags/ArcGISDynamicMapServiceLayer.html)
* [ClassBreak Rendering](https://developers.arcgis.com/android/api-reference/reference/com/esri/core/renderer/ClassBreaksRenderer.html)
* [Class Break](https://developers.arcgis.com/android/api-reference/reference/com/esri/core/renderer/ClassBreak.html)

## App usage
1. Select the ```ActionBar``` overlay button to see a list of attribute field names for the layer being displayed on the map.
2. When the selected field changes, a new map image is requested using the new field as the basis for the layer's renderer.  