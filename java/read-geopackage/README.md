# Read GeoPackage

This sample demonstrates how to read rasters and feature tables from geoPackages and display them as layers in a map.

![Scene Layer App](scene-layer.png)

## Features

* FeatureLayer
* GeoPackage
* GeoPackageFeatureTable
* GeoPackageRaster
* Layer
* Raster Layer

## How to use the sample

The `Layer`s in the `GeoPackage`, are shown in a drawer on the left hand side of the screen. Click on a `Layer` to add or remove it from map.

## Developer Pattern

1. Create a `GeoPackage` using a path a the local GeoPackage file.
1. Load the `GeoPackage` with `.loadAsync()`.
1. Use `geoPackage.getGeoPackageRasters()` to get each `GeoPackageRaster` and use them to make `RasterLayer`s.
1. Use `geoPackage.getGeoPackageFeatureTables()` to get each `GeoPackageFeatureTable` and use them to make `FeatureLayer`s.
1. Based on user input, add or remove these `Layer`s from the `ArcGISMap`'s operationalLayers.

