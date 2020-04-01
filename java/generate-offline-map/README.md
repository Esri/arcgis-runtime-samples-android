# Generate offline map

Take a web map offline.

![Image of generate offline map](generate-offline-map.png)

## Use case

Taking a web map offline allows users continued productivity when their network connectivity is poor or nonexistent. For example, by taking a map offline, a field worker inspecting utility lines in remote areas could still access a feature's location and attribute information.

## How to use the sample

Once the map loads, zoom to the extent you want to take offline. The red border shows the extent that will be downloaded. Tap the "Take Map Offline" button to start the offline map job. You will be prompted to sign in using a free ArcGIS Online account. The progress bar will show the job's progress. When complete, the offline map will replace the online map in the map view.

## How it works

1. Create an `ArcGISMap` with a `Portal` item pointing to the web map.
2. Create `GenerateOfflineMapParameters` specifying the download area geometry, minimum scale, and maximum scale.
3. Create an `OfflineMapTask` with the map.
4. Create the `OfflineMapJob` with `OfflineMapTask.generateOfflineMap(params, downloadDirectoryPath)` and start it with `OfflineMapJob.start()`.
5. When the job is done, get the offline map with `OfflineMapJob.getResult().getOfflineMap()`.

## Relevant API

* GenerateOfflineMapJob
* GenerateOfflineMapParameters
* GenerateOfflineMapResult
* OfflineMapTask
* Portal

## About the data

The map used in this sample shows the [stormwater network](https://arcgisruntime.maps.arcgis.com/home/item.html?id=acc027394bc84c2fb04d1ed317aac674) within Naperville, IL, USA, with cartography designed for web and mobile devices with offline support.

## Additional information

The creation of the offline map can be fine-tuned using [parameter overrides for feature layers](https://github.com/Esri/arcgis-runtime-samples-android/tree/master/java/generate-offline-map-overrides), or by using [local basemaps](https://github.com/Esri/arcgis-runtime-samples-android/tree/master/java/generate-offline-map-with-local-basemap)
 to achieve more customised results.

## Tags

download, offline, save, web map
