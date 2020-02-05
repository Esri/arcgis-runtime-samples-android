# Scene layer

Add a scene layer to a scene.

![](scene-layer.png)

## Use case

Each scene layer added to a scene can assist in performing helpful visual analysis. For example, if presenting the results of a shadow analysis of a major metropolitan downtown area in 3D, adding a scene layer of 3D buildings to the scene that could be toggled on/off would help to better contextualize the source of the shadows.

## How to use the sample

When launched, this sample displays a scene service with an `ArcGISSceneLayer`. Pan and zoom to explore the scene.

## How it works

1. Create an `ArcGISScene` and set its `Basemap` with `ArcGISScene.setBasemap()`.
1. Create a `Surface` and add an elevation source to it: `surface.getElevationSources()add.(arcGISTiledElevationSource)`.
1. Add the created surface to the scene: `ArcGISScene.setBaseSurface(surface)`.
1. Create a `SceneView` and set the scene to the view with `SceneView.setArcGISScene(scene)`.
1. Create an `ArcGISSceneLayer` using a data source URI: `new ArcGISSceneLayer(Uri)`.
1. Add the new scene layer to the scene as an operational layer with `ArcGISScene.getOperationalLayers().add(sceneLayer)`.

## About the data

The scene launches with a northward view of the city of Brest, France. A 3D scene layer representing buildings (some textured) is used as an example.

## Relevant API

* ArcGISScene
* ArcGISSceneLayer
* ArcGISTiledElevationSource
* SceneView
* Surface

## Tags

layer, scene, 3D
