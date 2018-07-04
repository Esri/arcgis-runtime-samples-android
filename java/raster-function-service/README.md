# Raster Function Service
Raster functions are operations performed on a raster to apply on-the-fly processing. This sample demonstrates how to create an image service raster and apply a hillshade raster function to it.

![Raster Function Service App](raster-function-service.png)

## How to use the sample
Click on the button to apply the Raster service.

## How it works
* Create `ImageServiceRaster` using a `URL` and load it.
* Create `RasterFunction` using a `JSON` string.
* Get raster function's arguments with `RasterFunctionArguments`.
* Set image service raster in the raster function arguments using `setRaster(rasterName, raster)`.
* Create `Raster` using the `RasterFunction`.
* Create `RasterLayer` using `Raster`.
* Add `RasterLayer`  to the map operational layers with `map.getOperationalLayers()`.

## Relevant API
* ImageServiceRaster
* RasterFunction
* RasterLayer

#### Tags
Layers
