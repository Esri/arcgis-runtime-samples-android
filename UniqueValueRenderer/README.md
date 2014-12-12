# UniqueValueRenderer Sample
The ```UniqueValueRenderer``` sample shows how the features are rederered based on a user defined unique value. On execution you will notice specific regions to be rendered representing in different colors which are classified based on the Unique Attributes as provided for the Unique Values. When you tap the colored region which are collective of states you will get the interactive callout results which displays the Name of the state and Population and the total land area. You can customize the callouts to view the results.  

It is important to note that these require the URL to render and also the query task executes in a separate thread from the main thread of the application. This allows the main thread to stay responsive while the query is being executed on the server and in the background. 

## Features
* [UniqueValueRenderer](https://developers.arcgis.com/android/api-reference/reference/com/esri/core/renderer/UniqueValueRenderer.html)
* [UniqueValue](https://developers.arcgis.com/android/api-reference/reference/com/esri/core/renderer/UniqueValue.html)
* [UniqueValueDefinition](https://developers.arcgis.com/android/api-reference/reference/com/esri/core/renderer/UniqueValueDefinition.html)
* [SimpleFillSymbol](https://developers.arcgis.com/android/api-reference/reference/com/esri/core/symbol/SimpleFillSymbol.html)
* [Query](https://developers.arcgis.com/en/android/api-reference/reference/com/esri/core/tasks/ags/query/Query.html)
* [AsyncTask](http://developer.android.com/reference/android/os/AsyncTask.html)
