# Raster function service

Load a raster from a service, then apply a function to it.

![Image of raster function service](raster-function-service.png)

## Use case

Raster functions allow processing operations that can be applied to one or more rasters on the fly. Functions can be applied to rasters that come from a service. A land survey agency may apply hillshade and aspect functions to rasters with elevation data in order to better determine the topography of a landscape and to make further planning decisions.

## How to use the sample

Load the sample to see a raster function applied to a raster.

## How it works

1. Create the `ImageServiceRaster` referring to the image server URL.
2. Create the `RasterFunction` from a JSON string.
3. Get the arguments of the raster function with `rasterFunction.arguments`.
4. Get the names of the raster arguments with `rasterFunctionArguments.rasterNames`.
5. Set the raster argument with `rasterFunction.setRaster(rasterName, raster)`.
6. Create a new `Raster` referring to the raster function.
7. Create a `RasterLayer` to visualize the computed raster.
8. Add the raster layer to the map.

## Relevant API

* ImageServiceRaster
* Raster
* RasterFunction
* RasterFunctionArguments
* RasterLayer

## About the data

The sample applies a hillshade function to a raster produced from the National Land Cover Database, [NLCDLandCover2001](https://sampleserver6.arcgisonline.com/arcgis/rest/services/NLCDLandCover2001/ImageServer). You can learn more about the [hillshade function](http://desktop.arcgis.com/en/arcmap/latest/manage-data/raster-and-images/hillshade-function.htm) in the *ArcMap* documentation.

## Additional information

The raster function computation happens locally on the client device. 

## Tags

function, layer, raster, raster function, service
