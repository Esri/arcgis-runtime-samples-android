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
The Routing sample depends on the [Andriod Support Library](https://developer.android.com/tools/support-library/index.html). Instructions for setting that it up prior to running the app is detailed below.

### Steps
 1. Right click the sample project and select **Android Tools > Add Support Library**
 2. Accept packages to install and click **Install**
 3. Under **Android Private Libraries** you should see the ```android-support-v4.jar``` file library
 4. Right click the sample project and select **Properties**
 5. Select the **Java Build Path** on the left hand side then select **Order and Export** in the Java Build Path tabs
 6. Make sure **Android Private Libraries** is checked
 7. Run the application

 **NOTE**: You can get the depedency from our build.gradle file as a compile time depedency

