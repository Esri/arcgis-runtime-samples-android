# Open mobile scene package

Opens and displays a scene from a Mobile Scene Package (.mspk).

![Open Mobile Scene Package App](open-mobile-scene-package.png)

## How to use the sample

Run the app and allow read permissions.

## How it works

1. Create a `MobileScenePackage` using a path to a local .mspk file.
1. Use `isDirectReadSupportedAsync()` to check whether the mobile scene package can be read in the archived form (.mspk) or whether it needs to be unpacked.
1. If the mobile scene package requires unpacking, call `unpackAsync()` and wait for unpacking to complete.
1. Call `loadAsync()` on the mobile scene package and check for any errors.
1. When the mobile scene package is loaded, get the first `Scene`and set it to the `SceneView`.

## Relevant API

* MobileScenePackage
* SceneView

## Offline data
1. Download the data from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=7dd2f97bb007466ea939160d0de96a9d).
1. Extract the contents of the downloaded zip file to disk.
1. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
1. Execute the following command:
`adb push philadelphia.mspk /sdcard/ArcGIS/Samples/ScenePackage/philadelphia.mspk`

Link | Local Location
---------|-------|
|[Philadelphia mobile scene package](https://www.arcgis.com/home/item.html?id=7dd2f97bb007466ea939160d0de96a9d)| `<sdcard>`/ArcGIS/Samples/ScenePackage/philadelphia.mspk|

## About the data

An .mspk file is an archive containing the data (specifically, basemaps and features), used to display an offline 3D scene. A mobile scene package can also be unpacked to a directory to allow read support for certain data types.

#### Tags
Edit and Manage Data
