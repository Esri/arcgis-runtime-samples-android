# Query Task
The Query task sample illustrates how you can use the QueryTask class to find features that and display the results in a toast message. You can then click on individual results to see more details about the feature in a customized callout. 

It is important to note that the query task executes in a separate thread from the main thread of the application. This allows the main thread to stay responsive while the query is being executed on the server and in the background. The Query is based on your current Map extent, with this in mind, zoom into an area to reduce the extent you will be quering.  The query returns counties with households of 3.5 or more people.  

## Features
* [QueryTask](https://developers.arcgis.com/en/android/api-reference/reference/com/esri/core/tasks/ags/query/QueryTask.html)
* [Query](https://developers.arcgis.com/en/android/api-reference/reference/com/esri/core/tasks/ags/query/Query.html)
* [AsyncTask](http://developer.android.com/reference/android/os/AsyncTask.html)
