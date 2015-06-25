# Routing
This sample demonstrates a simple user experience for finding a route between two points by long pressing on a street to calculate a route from your location to the geocoded location resulting from your long press.  Additionally, the sample demonstrates a dialog to input start and destination locations to calculate a route.  

The sample contains four files:
1.RoutingSample
  When the user long clicks on the map, the activity will do the Routing Task and create the Routing List Fragment
2.Routing List Fragment
  This fragment populates the Listview (Navigation Drawer) using a custom adapter
3. RoutingDialogFragment
  When the user clicks on the Get Direction button on the bottom layout, a dialog box appears in which the user can enter the source and destination addresses to get the route. It uses Geocoding to convert the addresses to the points on the map which then the Routing sample uses for routing. 
4. MyAdapter
	Custom ```Adapter``` for the ```ListView```.

## Features
* Calculate route from your location to specified location
* Route broken into segments
* Support for credentials to support ArcGIS Online Network Analysis Service
* Geocode the addresses

## Sample Requirements
The Routing sample depends on the [Andriod Support Library](https://developer.android.com/tools/support-library/index.html). 

 **NOTE**: The Routing sample depends on the Android Support Library and is included in the sample as a compile time dependency in the modules build.grade file. 

