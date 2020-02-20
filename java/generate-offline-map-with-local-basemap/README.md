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

1. Tap on "Generate Offline Map".
2. You will be prompted to choose whether you wish to download the online basemap or use the "naperville_imagery.tpk" basemap which is already on the device.
3. If you choose to download the online basemap, the offline map will be generated with the same (topographic) basemap as the online web map.
4. To download the Esri basemap, you may be prompted to sign in to ArcGIS.com.
5. If you choose to use the basemap from the device, the offline map will be generated with the local imagery basemap. The download will be quicker since no tiles are exported or downloaded.
6. Since the application is not exporting online ArcGIS Online basemaps you will not need to log-in.

## How it works

The sample creates a `PortalItem` object using a web map's ID. This portal item is used to initialize an `OfflineMapTask` object. When the button is tapped, the sample requests the default parameters for the task, with the selected extent, by calling `OfflineMapTask.createDefaultGenerateOfflineMapParameters`. 

Once the parameters are created, the application checks the `GenerateOfflineMapParameters.referenceBasemapFilename` property. The author of an online web map can configure this setting to indicate the name of a suitable basemap. In this example, the application checks the local file-system for the suggested "naperville_imagery.tpk" file - and if found, asks the user whether they wish to use this instead of downloading.

If the user chooses to use the basemap on the device, the `GenerateOfflineMapParameters.referenceBasemapDirectory` is set to the absolute path of the directory which contains the .tpk file.

A `GenerateOfflineMapJob` is created by calling `OfflineMapTask.generateOfflineMap` passing the parameters and the download location for the offline map.

When the `GenerateOfflineMapJob` is started it will check whether `GenerateOfflineMapParameters.referenceBasemapDirectory` has been set. If this property is set, no online basemap will be downloaded and instead, the mobile map will be created with a reference to the .tpk on the device.

## Relevant API

* GenerateOfflineMapJob
* GenerateOfflineMapParameters
* GenerateOfflineMapResult
* OfflineMapTask

## Offline Data
1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=628e8e3521cf45e9a28a12fe10c02c4d).
1. Extract the contents of the downloaded zip file to disk.
1. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
1. Execute the following command:
`adb push naperville_imagery.tpk /sdcard/ArcGIS/Samples/TileCache/naperville_imagery.tpk`

Link | Local Location
---------|-------|
|[Naperville Imagery](https://arcgisruntime.maps.arcgis.com/home/item.html?id=628e8e3521cf45e9a28a12fe10c02c4d)| `<sdcard>`/ArcGIS/Samples/TileCache/naperville_imagery.tpk|

## Tags

basemap, download, local, offline, save, web map