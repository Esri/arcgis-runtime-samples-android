# Raster Layer GeoPackage

This sample demonstrates how to open a GeoPackage, obtain a raster from the package, and display the table as a RasterLayer.

![Raster Layer Geopackage App](raster-layer-geopackage.png)

## How it works

A GeoPackage is created by passing the path to a `.gpkg` file. Once the `Map` loads, `GeoPackage::load` is called. A signal handler is connected so that once the `GeoPackage` loads, the `RasterLayer` can be created. The `RasterLayer` is created by obtaining the first `Raster` in the list of `GeoPackageRasters`, and passing that `Raster` to the `RasterLayer`. Finally, the new layer is appended to the Map's operational layers.

## Features

* GeoPackage
* GeoPackageRaster
* RasterLayer

## Provision your device
1. Download the data from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=68ec42517cdd439e81b036210483e8e7).
1. Extract the contents of the downloaded zip file to disk.
1. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
1. Execute the following command: `adb push AuroraCO.gpkg /sdcard/ArcGIS/Samples/GeoPackage/AuroraCO.gpkg`


Link | Local Location
---------|-------|
|[Aurora CO GeoPackage](https://www.arcgis.com/home/item.html?id=68ec42517cdd439e81b036210483e8e7)| `<sdcard>`/ArcGIS/Samples/GeoPackage/AuroraCO.gpkg|