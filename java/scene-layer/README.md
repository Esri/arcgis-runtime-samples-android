# Scene Layer

This sample demonstrates how to add a scene layer to a scene.

![Scene Layer App](scene-layer.png)

## Features

* ArcGISScene
* SceneView
* SceneLayer

## Developer Pattern

1. Create an `ArcGISScene` and set a `Basemap` with `ArcGISScene.setBasemap()`.
1. Create a `SceneView` and set the scene to the view with `SceneView.setScene(scene)`. 
1. Create a `SceneLayer` and add it to the scene as an operational layer with `scene.getOperationalLayers().add(sceneLayer)`;

```java
// create a scene and add a basemap to it
ArcGISScene scene = new ArcGISScene();
scene.setBasemap(Basemap.createImagery());

mSceneView = (SceneView) findViewById(R.id.sceneView);
mSceneView.setScene(scene);

// add a scene service to the scene for viewing buildings
ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(getResources().getString(R.string.brest_buildings));
scene.getOperationalLayers().add(sceneLayer);

// add a camera and initial camera position
Camera camera = new Camera(48.378, -4.494, 200, 345, 65, 0);
mSceneView.setViewpointCamera(camera);
```
