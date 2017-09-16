# Find address

This sample demonstrates how to geocode an address and show it on a map view.


## How to use the sample

Type in an address in the search view at the top of the screen. Suggestions will appear as text is entered. Tap a suggestion or enter your own text to see the address marked with a pin. Tapping on the pin will show the address in a callout.

![](image1.png)


## How it works

Create an `ArcGISMap` using a `Basemap`.

Add the map to the `MapView`, `MapView.setMap()`.

Create a `LocatorTask` using the world geocode service and define the `GeocodeParameters` for  the `LocatorTask`.

To geocode an address, use `LocatorTask.geocodeAsync(geocodeParameters)`.

Show the retrieved result on the `MapView` by creating a `PictureMarkerSymbol` with attributes from the `GeocodeResult` and add that symbol to a `Graphic` in the `GraphicsOverlay`.

Tapping on the `Graphic` will trigger `MapView.identifyGraphicsOverlayAsync(...)` which will show a `Callout` which contains the attributes chosen in the `GeocodeParameters`.

## Features

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