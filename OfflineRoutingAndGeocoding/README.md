# Offline Routing	
This sample demonstrates offline routing by finding a route between multiple points.  Single tap on the map to set stops for a route.  You must have at least 2 stops to calculate a route. Double tapping on a street will calculate a route along all stops.  You can continue to add stops after a route has been calculated and double tap to calculate a new route.  When you single click on the directions label a ```ListActivity``` appears to show the segments of the route.  Clicking on a segment in the directions label will bring you back to the map zoomed into the selected segment.  

## Features
* Calculate route from your location to specified location
* Route broken into segments
* Support for credentials to support ArcGIS Online Network Analysis Service

## Data
The OfflineRoutingAndGeocoding sample is an example of working with data in an offline setting.  The sample depends on basemap data to be located on the device. This includes installing a local tile map cache (tpk) to device as described below:

1. Download Basemap & Routing/Geocoding data from [ArcGIS Online](http://www.arcgis.com/home/item.html?id=bd441813cd2f4c8891aee671a65feb54).
2. Create the the sample data folder at the root <storage> folder on your device, /{device-externalstoragepath}/ArcGIS/samples/OfflineRouting.  
3. Push the downloaded data contents from step 1 to your device:
    * /{device-externalstoragepath}/ArcGIS/samples/OfflineRouting/SanDiego.tpk
    * /{device-externalstoragepath}/ArcGIS/samples/OfflineRouting/Routing/RuntimeSanDiego.geodatabase
    * /{device-externalstoragepath}/ArcGIS/samples/OfflineRouting/Routing/RuntimeSanDiego.tn/*
    * /{device-externalstoragepath}/ArcGIS/samples/OfflineRouting/Geocoding/SanDiego_StreetAddress.loc
    * /{device-externalstoragepath}/ArcGIS/samples/OfflineRouting/Geocoding/SanDiego_StreetAddress.locb
    * /{device-externalstoragepath}/ArcGIS/samples/OfflineRouting/Geocoding/SanDiego_StreetAddress.lox
