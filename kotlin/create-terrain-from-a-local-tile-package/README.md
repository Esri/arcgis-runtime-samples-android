# Create terrain from a local tile package

Set the terrain surface with elevation described by a local tile package.

![Image of create terrain from a local tile package](create-terrain-from-a-local-tile-package.png)

## Use case

In a scene view, the terrain surface is what the basemap, operational layers, and graphics are draped onto. For example, when viewing a scene in a mountainous region, applying a terrain surface to the scene will help in recognizing the slopes, valleys, and elevated areas.

## How to use the sample

When loaded, the sample will show a scene with a terrain surface applied. Pan and zoom to explore the scene and observe how the terrain surface allows visualizing elevation differences.

## How it works

1. Create an `ArcGISScene` and add it to a `SceneView`.
1. Create an `ArcGISTiledElevationSource` with the path to the local tile package.
1. Add this source to the scene's base surface: `ArcGISScene.baseSurface.elevationSources.add(ArcGISTiledElevationSource)`.

## Relevant API

* ArcGISTiledElevationSource
* Surface

## Offline data

1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=cce37043eb0440c7a5c109cf8aad5500).
1. Extract the contents of the downloaded zip file to disk.
1. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
1. Execute the following command:

`adb push MontereyElevation.tpkx /Android/data/com.esri.arcgisruntime.sample.createterrainfromalocaltilepackage/files/MontereyElevation.tpkx`

Link | Local Location
---------|-------|
|[Monterey Elevation TPKX](https://arcgisruntime.maps.arcgis.com/home/item.html?id=52ca74b4ba8042b78b3c653696f34a9c)| /Android/data/com.esri.arcgisruntime.sample.createterrainfromalocaltilepackage/files/MontereyElevation.tpkx |

## Additional information

The tile package must be a LERC (limited error raster compression) encoded TPKX. Details on can be found in the topic [Share a tile package](https://pro.arcgis.com/en/pro-app/help/sharing/overview/tile-package.htm) in the *ArcGIS Pro* documentation.

## Tags

3D, elevation, LERC, surface, terrain, tile cache
