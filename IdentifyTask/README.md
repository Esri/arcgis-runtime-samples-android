# Identify Task
The purpose of this sample is to show the user how to use the Identify task to query features on the map and show these results in a callout. The sample adds a tiled map service as the basemap and a dynamic map service of recent earthquakes. The dynamic map service is the same as that used for the Identify task.

## Features
* [IdentifyTask](https://developers.arcgis.com/en/android/api-reference/reference/com/esri/core/tasks/ags/identify/IdentifyTask.html)
* [IdentifyResultSpinner](https://developers.arcgis.com/en/android/api-reference/reference/com/esri/android/action/IdentifyResultSpinner.html)
* Single Tap Listener

## Sample Design
The maps layers are added to the map via code in the activities ```onCreate()``` method and the parameters for the ```IdentifyTask``` are also created (map service uniform resource locator [URL] and layers to query). An ```OnSingleTapListener``` is set on the map and within this class, the ```IdentifyTask``` is created and invoked via an ```Executor``` (as the Task abstract super class implements the ```Callable<T>``` interface), which allows the task to run in a different thread from the main thread. The geometry is obtained from the ```onSingleTap``` event and is passed to the task. The results from the ```IdentifyTask``` are processed in the ```onCompletion()``` method and placed in a map ```Callout```.

In the ```Callout```, a spinner is customized by overriding the ```IdentifyResultSpinnerAdapter``` class. In the new adapter within the ```getView()``` method, create a ```ListView``` that is populated with ```IdentifyResult```.
