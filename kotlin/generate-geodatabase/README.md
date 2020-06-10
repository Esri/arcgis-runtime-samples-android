# Generate geodatabase

Generate a local geodatabase from an online feature service.

![Image of generate geodatabase](generate-geodatabase.png)

## Use case

Generating geodatabases is the first step toward taking a feature service offline. It allows you to save features locally for offline display.

## How to use the sample

Zoom to any extent. Then tap the generate button to generate a geodatabase of features from a feature service filtered to the current extent. A red outline will show the extent used. The job's progress is shown while the geodatabase is generated. When complete, the map will reload with only the layers in the geodatabase, clipped to the extent.

## How it works

1. Create a `GeodatabaseSyncTask` with the URL of the feature service and load it.
2. Create `GenerateGeodatabaseParameters` specifying the extent and whether to include attachments.
3. Create a `GenerateGeodatabaseJob` with `geodatabaseSyncTask.generateGeodatabaseAsync(parameters, downloadPath)`. Start the job with `job.start()`.
4. When the job is done, get the geodatabase from `job.result`. Inside the geodatabase are feature tables which can be used to add feature layers to the map.
5. Call `syncTask.unregisterGeodatabaseAsync(geodatabase)` after generation when you're not planning on syncing changes to the service.

## Relevant API

* GenerateGeodatabaseJob
* GenerateGeodatabaseParameters
* Geodatabase
* GeodatabaseSyncTask

## Offline Data

1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=72e703cd01654e7796eb1ae75af1cb53).
2. Extract the contents of the downloaded zip file to disk.
3. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
4. Push the data into the scoped storage of the sample app:
`adb push SanFrancisco.tpk /Android/data/com.esri.arcgisruntime.sample.generategeodatabase/files/SanFrancisco.tpk`

## Tags

disconnected, local geodatabase, offline, sync
