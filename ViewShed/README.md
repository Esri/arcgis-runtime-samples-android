# Viewshed
This sample demonstrates the use of the ```Geoprocessor``` (GP) task to calculate a viewshed within a five mile distance of an area of interest. It also illustrates how to use an Android ```AsyncTask``` to execute asynchronous GP tasks. The application prompts the user to add a point to the map with a single tap on the device, click the **Play** button to run a geoprocessing task that calculates the viewshed from the point location perspective. 

## Features
* Geoprocessing
* Creating symbols on a Graphics Layer.
* Single Tap event listener.

## Sample Design 
A viewshed is an area visible from a specific location.

The ```onCreate()``` method is first called when the activity is created. It sets the content view to a ```MapView``` defined in the layout ```main.xml``` file. The ```mapType``` is set in the layout along with the zoom level and its center. The ```setOnSingleTapListener``` is set on the map such that when a user taps on the map it displays a point at the tapped location. It also removes any previous point if present.

The ```ActionBar``` displays the two icons **Start** and **Delete**. When **Start** is tapped, the viewshed analysis takes place using the point on the map as an ```AsyncTask``` running on the background thread. The results are displayed on a ```GraphicsLayer```. When the **Delete** icon is tapped, any graphics are removed from the ```GraphicsLayer```.

