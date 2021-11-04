# Generate offline map with local basemap

Use the `OfflineMapTask` to take a web map offline, but instead of downloading an online basemap, use one which is already on the device.

![Image of generate offline map with local basemap](generate-offline-map-with-local-basemap.png)

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

Tap on "Generate Offline Map". You will be prompted to choose whether you wish to download the online basemap or use the "naperville_imagery.tpkx" basemap (see Offline Data section). If you choose to download the online basemap, the offline map will be generated with the same (topographic) basemap as the online web map. To download the Esri basemap, an organizational account is required.

If you choose to use the basemap from the device, the offline map will be generated with the local imagery basemap. The download will be quicker since no tiles are exported or downloaded. Since the application is not exporting online ArcGIS Online basemaps you will not need to log-in.

## How it works

1. Create a `PortalItem` object using a web map's ID and create an `ArcGISMap` from it.
2. Initialize an `OfflineMapTask` object using the map.
3. Request the default parameters for the task with `OfflineMapTask.createDefaultGenerateOfflineMapParametersAsync()`.
4. A `GenerateOfflineMapJob` is created by calling `OfflineMapTask.generateOfflineMap`.  
    * If desired, set the `GenerateOfflineMapParameters.referenceBasemapDirectory` to the absolute path of the directory which contains the .tpkx file.
    * Otherwise a basemap will be downloaded.
5. Run the `GenerateOfflineMapJob` with basemap settings from step 4.

## Relevant API

* GenerateOfflineMapJob
* GenerateOfflineMapParameters
* GenerateOfflineMapResult
* OfflineMapTask

## Offline Data

1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=85282f2aaa2844d8935cdb8722e22a93).
2. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
3. Push the data into the scoped storage of the sample app:
`adb push naperville_imagery.tpk /Android/data/com.esri.arcgisruntime.sample.generateofflinemapwithlocalbasemap/files/naperville_imagery.tpkx`

## Tags

basemap, download, local, offline, save, web map
