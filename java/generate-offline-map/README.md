# Generate Offline Map
Take a web map offline.

![Generate Offline Map App](generate-offline-map.png)

## How to use the sample
When the app starts, you will be prompted to sign in using a free ArcGIS Online account. Once the map loads, zoom to the extent you want to take offline. The red border shows the extent that will be downloaded. Click the "Take Map Offline" button to start the offline map job. The progress bar will show the job's progress. When complete, the offline map will replace the online map in the map view.

## How it works
To take a web map offline:

1. Create an `ArcGISMap` with a portal item pointing to the web map.
1. Create `GenerateOfflineMapParameters` specifying the download area geometry, min scale, and max scale.
1. Create an `OfflineMapTask` with the map.
1. Create the offline map job with `task.generateOfflineMap(params, downloadDirectoryPath)` and start it with `job.start()`.
1. When the job is done, get the offline map with job.getResult().getOfflineMap().

## Relevant API
* GenerateOfflineMapJob
* GenerateOfflineMapParameters
* GenerateOfflineMapResult
* OfflineMapTask

#### Tags
Edit and Manage Data