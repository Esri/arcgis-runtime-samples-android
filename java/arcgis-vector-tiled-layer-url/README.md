# ArcGIS vector tiled layer URL

Load an ArcGIS vector tiled layer from a URL.

![Image of ArcGIS vector tiled layer url](arcgis-vector-tiled-layer-url.png)

## Use case

Vector tile basemaps can be created in ArcGIS Pro and published as offline packages or online services. An ArcGIS vector tiled layer has many advantages over traditional raster based basemaps (ArcGIS tiled layer), including smooth scaling between different screen DPIs, smaller package sizes, and the ability to rotate symbols and labels dynamically.

## How to use the sample

Tap the menu icon at the top left of the screen and select different vector tile basemaps.

## How it works

1. Construct an `ArcGISVectorTiledLayer` with an ArcGIS Online service URL.
2. Instantiate a new `Basemap` passing in the vector tiled layer as a parameter.
3. Create a new `ArcGISMap` object by passing in the basemap as a parameter.

## Relevant API

* ArcGISVectorTiledLayer
* BasemapStyle

## Tags

tiles, vector, vector basemap, vector tiled layer, vector tiles
