# Feature Layer Shapefile

This sample demonstrates how to open a shapefile stored on the device and display it as a feature layer with default symbology.

![Feature Layer App](feature-layer-shapefile.png)

## Features

* FeatureLayer
* ShapefileFeatureTable

## How it works

1. Create a `ShapefileFeatureTable a path to a shapefile (.shp) on loaded onto the device.
1. Use the `ShapefileFeatureTable` to create a `FeatureLayer`
1. Add the `FeatureLayer` to the `ArcGISMap` as an operational layer, using default symbology and rendering.
