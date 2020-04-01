# Show callout

Show a callout with the latitude and longitude of user-tapped points.

![Image of show callout](show-callout.png)

## Use case

Callouts are used to display temporary detail content on a map. You can display text and arbitrary UI controls in callouts.

## How to use the sample

Tap anywhere on the map. A callout showing the WGS84 coordinates for the tapped point will appear.

## How it works

1. When the user taps, get the tapped location and create a map `Point` from it using `MapView.screenToLocation(tappedLocation)`.
2. Project the point's geometry to WGS84 using `GeometryEngine.project(mapPoint, SpatialReferences.getWgs84())`.
3. Create a new Android TextView object and set its text to the coordinate string from the point. 
4. Create a new `Callout` with `MapView.getCallout()` and set its location on the map with `callout.setLocation(mapPoint)`.
5. Set the callout's content with `callout.setContent(TextView)`and display it on the map view with `callout.show()`.

## Relevant API

* Callout
* MapView
* Point

## Tags

balloon, bubble, callout, flyout, flyover, info window, popup, tap
