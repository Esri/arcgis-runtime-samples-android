# Analyze Hotpots
This sample demonstrates how to execute a geoprocessing task to calculate a hotspot analysis based on the frequency of 911 calls. It calculates the frequency of these calls within a given study area during a specified constrained time period set between 1998-01-01 and 1998-05-31.

![Analyze Hotspots App](analyze-hotspots.png)

## Features
* GeoprocessingJob
* GeoprocessingParameters
* GeoprocessingResult
* GeoprocessingTask

## How to use the sample
Select a From: and To: date from the dialog and tap on the `Analyze`. The results will be shown on the map on successful completion of the Geoprocessing Job

## How it works
A `GeoprocessingTask` is created by setting the URL to the REST endpoint of a geoprocessing service.
`GeoprocessingParameters` are created asynchronously from the `GeoprocessingTask`.

Once the date ranges are selected, a query string is created with the 'to' and 'from' dates. The query string is then added as a key/value parameter input to the `GeoprocessingParameters`.

A `GeoprocessingJob` is then obtained by calling  `.createJob(geoprocessingParameters)` method of the `GeoprocessingTask`. The job is started, and once complete, the `ArcGISMapImageLayer` is obtained from the result, and added to the `ArcGISMap`.
