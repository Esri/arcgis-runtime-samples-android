# Identify KML features

Show a balloon popup with formatted content for tapped KML features. KML supports defining HTML for popups.

![Identify KML features app](identify-kml-features.png)

## Use case

A user may wish to select a KML feature to view relevant information about it.

## How to use the sample

Tap a feature to identify it. Feature information will be displayed in a callout.

Note: the KML layer used in this sample contains a screen overlay. The screen overlay contains a legend and the logos for NOAA and the NWS. You can't identify the screen overlay.

## How it works

1. Create an `OnTouchListener` on the `MapView`.
2. On tap:
  * Dismiss the callout.
  * Call `identifyLayersAsync(...)` passing in the `KmlLayer`, screen point and tolerance.
  * Await the result of the identify and then the `KmlPlacemark` from the result.
  * Use the placemark to position and populate the content of a callout.
  * Show the callout.
  
Note: There are several types of KML features. This sample only identifies features of type `KmlPlacemark`.

## Relevant API

* KmlLayer
* KmlPlacemark
* KmlPlacemark.getBalloonContent()
* IdentifyLayerResult
* GeoView.identifyLayerAsync(...)

#### Tags
Search and Query
Weather
NOAA
NWS
KML
KMZ
OGC
Keyhole
