# Generate Geodatabase
Generate a geodatabase from a feature service.

![Generate Geodatabase App](generate-geodatabase.png)

## How to use the sample
Zoom to any extent and click the generate button to generate a geodatabase of features from a feature service filtered to the current extent. A red bounding box graphic will display showing the extent used. A progress bar will show the job's progress. Once the geodatabase has been generated it is stored in the app's cache directory (shown in the log). The layers in the geodatabase are then added to the map.

## How it works
1. Create a `GeodatabaseSyncTask` with the URL of a feature service and load it.
1. Create `GenerateGeodatabaseParameters` specifying the extent and whether to include attachments.
1. Create a `GenerateGeodatabaseJob` with `GenerateGeodatabaseJob generateGeodatabaseJob = syncTask.generateGeodatabaseAsync(parameters, filePath)`. Start the job with `generateGeodatabaseJob.start()`.
1. When the job is done, `generateGeodatabaseJob.getResult()` will return a `Geodatabase`. Inside the `Geodatabase` are `FeatureTable`s that can be used to add `FeatureLayer`s to the map.
1. Lastly, it is good practice to call `syncTask.unregisterGeodatabaseAsync(geodatabase)` when not planning on syncing changes to the service.

## Relevant API
* ArcGISMap
* FeatureLayer
* Geodatabase
* GenerateGeodatabaseJob 
* GenerateGeodatabaseParameters
* MapView
* ServiceFeatureTable

## Offline data
1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=72e703cd01654e7796eb1ae75af1cb53).  
2. Extract the contents of the downloaded zip file to disk.  
3. Create an ArcGIS/samples/MapPackage folder on your device. You can use the [Android Debug Bridge (adb)](https://developer.android.com/guide/developing/tools/adb.html) tool found in **<sdk-dir>/platform-tools**.
4. Open up a command prompt and execute the ```adb shell``` command to start a remote shell on your target device.
5. Navigate to your sdcard directory, e.g. ```cd /sdcard/```.  
6. Create the ArcGIS/samples/TileCache directory, ```mkdir ArcGIS/samples/TileCache```.
7. You should now have the following directory on your target device, ```/sdcard/ArcGIS/samples/TileCache```. We will copy the contents of the downloaded data into this directory. Note:  Directory may be slightly different on your device.
8. Exit the shell with the, ```exit``` command.
9. While still in your command prompt, navigate to the folder where you extracted the contents of the data from step 1 and execute the following command: 
	* ```adb push SanFrancisco.tpk /sdcard/ArcGIS/samples/TileCache```


Link | Local Location
---------|-------|
|[San Francisco Tile Cache](https://arcgisruntime.maps.arcgis.com/home/item.html?id=72e703cd01654e7796eb1ae75af1cb53)| `<sdcard>`/ArcGIS/samples/TileCache/SanFrancisco.tpk |

#### Tags
Edit and Manage Data