# Display a Scene
This sample demonstrates how to display a scene with elevation data.

![Display a Scene App](display-scene.png)

## Features
* ArcGISScene
* ArcGISTiledElevationSource
* SceneView

## Developer Pattern
Create an `ArcGISScene` and set the `Basemap` with `ArcGISScene.setBasemap()`. Create a `SceneView` and set the scene to the view, `SceneView.setScene(scene)`.  Create a `Surface` and add an `ArcGISTiledElevationSource`, `Surface.getElevationSources().add()`. Set the surface as the scene's base surface `ArcGIScene.setBaseSurface(surface)`.

```java
// inflate SceneView from layout
mSceneView = (SceneView) findViewById(sceneView);
// create a scene and add a basemap to it
ArcGISScene agsScene = new ArcGISScene();
agsScene.setBasemap(Basemap.createImagery());
mSceneView.setScene(agsScene);

// add base surface for elevation data
ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getResources().getString(R.string.elevation_image_service));
agsScene.getBaseSurface().getElevationSources().add(elevationSource);

// add a camera and initial camera position
Camera camera = new Camera(28.4, 83.9, 10010.0, 10.0, 80.0, 300.0);
mSceneView.setViewpointCamera(camera);
```
