# Viewshed GeoElement

Analyze the viewshed for an object (GeoElement) in a scene.

![Image of viewshed geoelement](viewshed-geoelement.png)

## Use case

A viewshed analysis is a type of visual analysis you can perform on a scene. The viewshed aims to answer the question 'What can I see from a given location?'. The output is an overlay with two different colors - one representing the visible areas (green) and the other representing the obstructed areas (red).

## How to use the sample

Tap to set a destination for the vehicle (a GeoElement). The vehicle will 'drive' towards the tapped location. The viewshed analysis will update as the vehicle moves.

## How it works

1. Create and show the scene, with an elevation source and a buildings layer.
2. Add a model (the `GeoElement`) to represent the observer (in this case, a tank).
    * Use a `SimpleRenderer` which has a heading expression set in the `GraphicsOverlay`. This way you can relate the viewshed's heading to the `GeoElement` object's heading.
3. Create a `GeoElementViewshed` with configuration for the viewshed analysis.
4. Add the viewshed to an `AnalysisOverlay` and add the overlay to the scene.
5. Configure the SceneView `CameraController` to orbit the vehicle.

## About the data

This sample shows a [Johannesburg, South Africa Scene](https://www.arcgis.com/home/item.html?id=eb4dab9e61b24fe2919a0e6f7905321e) from ArcGIS Online. The sample uses a [Tank model scene symbol](http://www.arcgis.com/home/item.html?id=07d62a792ab6496d9b772a24efea45d0) hosted as an item on ArcGIS Online.

## Relevant API

* AnalysisOverlay
* GeodeticDistanceResult 
* GeoElementViewshed
* GeometryEngine.distanceGeodetic
* ModelSceneSymbol
* OrbitGeoElementCameraController

## Tags

3D, analysis, buildings, model, scene, viewshed, visibility analysis
