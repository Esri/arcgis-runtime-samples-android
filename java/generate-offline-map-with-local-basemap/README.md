# Generate offline map with local basemap

Take a web map offline, but instead of downloading an online basemap, use one which is already on the device.

![Generate offline map with local basemap App](generate-offline-map-with-local-basemap.png)

## Use case

There are a number of use-cases where you may wish to use a basemap which is already on the device, rather than downloading:

* You want to limit the total download size.
* You want to be able to share a single set of basemap files between many offline maps.
* You want to use a custom basemap (for example authored in ArcGIS Pro) which is not available online.
* You do not wish to sign into ArcGIS.com in order to download Esri basemaps.

The author of a web map can support the use of basemaps which are already on a device by configuring the web map to specify the name of a suitable basemap file. This could be a basemap which:

* Has been authored in ArcGIS Pro to make use of your organizations custom data.
* Is available as a PortalItem which can be downloaded once and re-used many times.

## How to use the sample

1. Click on the "Generate offline map" button.
1. You will be prompted to choose whether you wish to use the "naperville_imagery.tpk" basemap which is already on the device or download the online basemap.
1. If you choose to download the online basemap, the offline map will be generated with the same (topographic) basemap as the online web map. To download the Esri basemap, you may be prompted to sign in to ArcGIS.com.
1. If you choose to use the basemap from the device, the offline map will be generated with the local imagery basemap. The download will be quicker since no tiles are exported or downloaded. Since the application is not exporting online ArcGIS Online basemaps you will not need to log-in.

## How it works

1. Create an `ArcGISMap` with a portal item pointing to the web map.
1. Create `GenerateOfflineMapParameters` specifying the download area geometry, min scale, and max scale.
1. Once the generate offline map parameters are created, check the `getReferenceBasemapFilename()` property. The author of an online web map can configure this setting to indicate the name of a suitable basemap. In this sample, the app checks the local device for the suggested "naperville_imagery.tpk" file.
1. If the user chooses to use the basemap on the device, use `setReferenceBasemapDirectory()` on the generate offline map parameters to set the absolute path of the directory which contains the .tpk file.
1. Create an `OfflineMapTask` the generate offline map parameters.
1. Create the offline map job and start it.
1. When the job is done, use `getOfflineMap` on the `GenerateOfflineMapResult` to get the map.

## Relevant API

* OfflineMapTask
* GenerateOfflineMapParameters
* GenerateOfflineMapParameterOverrides
* GenerateOfflineMapJob
* GenerateOfflineMapResult

## Offline Data
1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=628e8e3521cf45e9a28a12fe10c02c4d).
1. Extract the contents of the downloaded zip file to disk.
1. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
1. Execute the following command:
`adb push naperville_imagery.tpk /sdcard/ArcGIS/Samples/TileCache/naperville_imagery.tpk`

Link | Local Location
---------|-------|
|[Naperville Imagery](https://arcgisruntime.maps.arcgis.com/home/item.html?id=628e8e3521cf45e9a28a12fe10c02c4d)| `<sdcard>`/ArcGIS/Samples/TileCache/naperville_imagery.tpk|

#### Tags
Edit and Manage Data
