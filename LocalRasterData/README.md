# LocalRasterData

The LocalRasterData app showcases the raster capabilities in the [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/en/android/). It aims to be a starting off point for developers looking to extend the raster features with the ArcGIS Android. It demonstrates how to read raster data stored on your device and apply a renderer to a raster layer.


## Features
* FileRasterSource
* RasterLayer
* RasterRenderer
* StretchRenderer
* RGBRenderer
* HillshadeRenderer
* BlendRenderer

## Sample Design
A RasterLayer is constructed using a '''FileRasterSource''' which represents the raw raster file on a device. It can be added to the map to allow the raw raster file to be rendered. The '''RasterLayer''' can be added to a '''MapView''' as basemap layer or operational layer. On-the-fly reprojection on '''FileRasterSource''' is performed before instantiating a '''RasterLayer''' from the '''FileRasterSource''' to handle different spatial references when adding the layer as operational layer.  ArcGIS Runtime Layers support changing it's renderer.  '''RasterRenderer''' is the interface that all the renderers must implement. You apply the renderer through the '''RasterLayer.setRenderer()''' method.