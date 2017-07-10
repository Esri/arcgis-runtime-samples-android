# Scene Layer
This sample demonstrates how to add a scene layer to a scene.

![Scene Layer App](scene-layer.png)

## Features
* ArcGISScene
* SceneView
* SceneLayer

## Developer Pattern
Create an `ArcGISScene` and set a `Basemap` with `ArcGISScene.setBasemap()`.
Create a `SceneView` and set the scene to the view, `SceneView.setScene(scene)`. 
Create a `SceneLayer` and add it to the scene as an operational layer, `scene.getOperationalLayers().add(sceneLayer)`;

```java
// create a scene and add a basemap to it
ArcGISScene scene = new ArcGISScene();
scene.setBasemap(Basemap.createImagery());

mSceneView = (SceneView) findViewById(R.id.sceneView);
mSceneView.setScene(scene);

// add a scene service to the scene for viewing buildings
ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(
    "http://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/Buildings_Brest/SceneServer");
scene.getOperationalLayers().add(sceneLayer);

// add a camera and initial camera position
Camera camera = new Camera(48.378, -4.494, 200, 345, 65, 0);
mSceneView.setViewpointCamera(camera);
```
