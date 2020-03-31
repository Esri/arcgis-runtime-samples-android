# Open mobile scene package

Opens and displays a scene from a Mobile Scene Package (.mspk).

![Image of open mobile scene package](open-mobile-scene-package.png)

## Use case

A mobile scene package is an archive containing the data (specifically, basemaps and features), used to display an offline 3D scene.

## How to use the sample

When the sample opens, it will automatically display the Scene in the Mobile Scene Package.

Since this sample works with a local .mspk, you will need to download the file to your device.

## How it works

1. Create a `MobileScenePackage` using the path to the local .mspk file.
2. Call `MobileScenePackage.loadAsync` and check for any errors.
3. When the `MobileScenePackage` is loaded, obtain the first `Scene` using `mobileScenePackage.getScenes().get(0)`
4. Create a `SceneView` and call `sceneView.setView` to display the scene from the package.

## Relevant API

* MobileScenePackage
* SceneView

## Offline Data

1. Download the data from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=7dd2f97bb007466ea939160d0de96a9d).
2. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
3. Push the data into the scoped storage of the sample app:
`adb push philadelphia.mspk /Android/data/com.esri.arcgisruntime.sample.openmobilescenepackage/files/philadelphia.mspk`


## Tags

offline, scene
