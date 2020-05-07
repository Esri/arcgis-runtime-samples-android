# Mobile map (search and route)

Display maps and use locators to enable search and routing offline using a Mobile Map Package.

![Image of mobile map search and route](mobile-map-search-and-route.png)

## Use case

Mobile map packages make it easy to transmit and store the necessary components for an offline map experience including: transportation networks (for routing/navigation), locators (address search, forward and reverse geocoding), and maps. 

A field worker might download a mobile map package to support their operations while working offline.

## How to use the sample

A list of maps from a mobile map package will be displayed. If the map contains transportation networks, the list item will have a navigation icon. Tap on a map in the list to open it. If a locator task is available, tap on the map to reverse geocode the location's address. If transportation networks are available, a route will be calculated between geocode locations.

## How it works

1.  Create a `MobileMapPackage` using `MobileMapPackage(path).loadAsync()`.
2.  Get a list of maps inside the package using the `mobileMapPackage.getMaps()`.
3.  If the package has a locator, access it using `mobileMapPackage.getLocatorTask()`.
4.  To see if a map contains transportation networks, check `map.getTransportationNetworks()`.

## Relevant API

* GeocodeResult
* MobileMapPackage
* ReverseGeocodeParameters
* Route
* RouteParameters
* RouteResult
* RouteTask

## Offline Data

1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=260eb6535c824209964cf281766ebe43).
2. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
3. Push the data into the scoped storage of the sample app:
`adb push SanFrancisco.mmpk /Android/data/com.esri.arcgisruntime.sample.mobilemapsearchandroute/files/SanFrancisco.mmpk`

## Tags

disconnected, field mobility, geocode, network, network analysis, offline, routing, search, transportation
