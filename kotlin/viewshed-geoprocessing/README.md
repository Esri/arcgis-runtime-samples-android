# Viewshed geoprocessing

Calculate a viewshed using a geoprocessing service, in this case showing what parts of a landscape are visible from points on mountainous terrain.

![Image of viewshed geoprocessing](viewshed-geoprocessing.png)

## Use case

A viewshed is used to highlight what is visible from a given point. A viewshed could be created to show what a hiker might be able to see from a given point at the top of a mountain. Equally, a viewshed could also be created from a point representing the maximum height of a proposed wind turbine to see from what areas the turbine would be visible.

## How to use the sample

Tap the map to see all areas visible from that point within a 15km radius. Tapping on an elevated area will highlight a larger part of the surrounding landscape. It may take a few seconds for the task to run and send back the results.

## How it works

1. Create a `GeoprocessingTask` object with the URL set to a geoprocessing service endpoint.
2. Create a `FeatureCollectionTable` object and add a new `Feature` object whose geometry is the viewshed's observer `Point`.
3. Make a `GeoprocessingParameters` object passing in the observer point.
4. Use the geoprocessing task to create a `GeoprocessingJob` object with the parameters.
5. Start the job and wait for it to complete and return a `GeoprocessingResult` object.
6. Get the resulting `GeoprocessingFeatures` object.
7. Iterate through the viewshed features to use their geometry or display the geometry in a new `Graphic` object.

## Relevant API

* FeatureCollectionTable
* GeoprocessingFeatures
* GeoprocessingJob
* GeoprocessingParameters
* GeoprocessingResult
* GeoprocessingTask

## Tags

analysis, geoprocessing, heat map, viewshed
