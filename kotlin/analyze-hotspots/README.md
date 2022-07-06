nlyze hotspots

Use a geoprocessing service and a set of features to identify statistically significant hot spots and cold spots.

![Image of analyze hotspots](analyze-hotspots.png)

#ol identifies statistically significant spatial clusters of high values (hot spots) and low values (cold spots). For example, a hotspot analysis based on the frequency of 911 calls within a set region.

## How to use the sample

Select a date range (between 1998-01-01 and 1998-05-31) from the dialog and tap on Analyze. The results will be shown on the map upon successful completion of the `GeoprocessingJob`.

## How it works

1. Create a `GeoprocessingTask` with the URL set to the endpoint of a geoprocessing service.
2. Create a query string with the date range as an input of `GeoprocessingParameters`.
3oprocessingJob` and wait for it to complete and return a `GeoprocessingResult`.
5. Get the resulting `ArcGISMapImageLayer` using `GeoprocessingResult.mapImageLayer`.
6. Add the layer to the map's operational layers.

## Relevant API

* GeoprocessingJob
* GeoprocessingParameters
* GeoprocessingResult
* GeoprocessingTask

## Tags

analysis, density, geoprocessing, hot spots, hotspots
