# Find Address
Geocode an address and show it on a map view.

![Find Address App](find-address.png)

## How to use the sample
Type in an address in the search view at the top of the screen. Suggestions will appear as text is entered. Tap a suggestion or enter your own text to see the address marked with a pin. Tapping on the pin will show the address in a callout.

## How it works
1. Create an `ArcGISMap` using a `Basemap`.
1. Add the map to the `MapView`, `MapView.setMap()`.
1. Create a `LocatorTask` using the world geocode service and define the `GeocodeParameters` for  the `LocatorTask`.
1. To geocode an address, use `LocatorTask.geocodeAsync(geocodeParameters)`.
1. Show the retrieved result on the `MapView` by creating a `PictureMarkerSymbol` with attributes from the `GeocodeResult` and add that symbol to a `Graphic` in the `GraphicsOverlay`.
1. Tapping on the `Graphic` will trigger `MapView.identifyGraphicsOverlayAsync(...)` which will show a `Callout` which contains the attributes chosen in the `GeocodeParameters`.

## Relevant API
* ArcGISMap
* Callout
* GeocodeParameters
* GeocodeResult
* Graphic
* GraphicsOverlay
* LocatorTask
* MapView
* PictureMarkerSymbol
* Point

#### Tags
Search and Query