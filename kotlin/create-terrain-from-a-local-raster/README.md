# Create terrain from a local raster

Set the terrain surface with elevation described by a raster file.

![Image of create terrain from a local raster](create-terrain-from-a-local-raster.png)

## Use case

In a scene view, the terrain surface is what the basemap, operational layers, and graphics are draped onto. For example, when viewing a scene in a mountainous region, applying a terrain surface to the scene will help in recognizing the slopes, valleys, and elevated areas.

## How it works

1. Create an `ArcGISScene` and add it to a `SceneView`.
2. Create a `RasterElevationSource` with a list of raster file paths.
3. Add this source to the scene's base surface: `ArcGISScene.baseSurface.elevationSources.add(RasterElevationSource)`.

## Relevant API

* RasterElevationSource
* Surface

## Additional information

 Supported raster formats include:

* ASRP/USRP
* CIB1, 5, 10
* DTED0, 1, 2
* GeoTIFF
* HFA
* HRE
* IMG
* JPEG
* JPEG 2000
* NITF
* PNG
* RPF
* SRTM1, 2

## Offline data

1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=98092369c4ae4d549bbbd45dba993ebc).
1. Extract the contents of the downloaded zip file to disk.
1. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
1. Execute the following command:

`adb push MontereyElevation.dt2 /sdcard/ArcGIS/Raster/dt2/MontereyElevation.dt2`

Link | Local Location
---------|-------|
|[Monterey Elevation Raster File](https://arcgisruntime.maps.arcgis.com/home/item.html?id=98092369c4ae4d549bbbd45dba993ebc)| `<sdcard>`/ArcGIS/Raster/dt2/MontereyElevation.dt2 |

## Tags

3D, elevation, raster, surface, terrain
