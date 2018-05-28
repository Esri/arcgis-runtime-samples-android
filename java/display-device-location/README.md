# Display Device Location       
Enable Location Display and switch between different types of AutoPan Modes. It also demonstrates how to use the Android Support Library to check for, and request, location permissions.

![Display Device Location](display-device-location.png)

## How to use the sample
This sample starts with a Map with an imagery basemap loaded in the MapView and Location display turned off. When you tap on the spinner it gives you the list of possible AutoPan Mode options along with turning the Location display on or off.

* Stop - Stops the Location Display
* On - Starts the Location Display with AutoPan Mode set to Off
* Re-Center - Starts location display with auto pan mode set to Default
* Navigation - Starts location display with auto pan mode set to Navigation
* Compass - Starts location display with auto pan mode set to Compass

## How it works
1. Create a `MapView`.
1. Get the `LocationDisplay` by calling `.getLocationDisplay()` on the `MapView`.
1. Use `start()` and `stop()` on the `LocationDisplay` as necessary.

Note: Location permissions are required for this sample.

## Relevant API
* ArcGISMap
* MapView
* LocationDisplay
* LocationDisplay.AutoPanMode

#### Tags
MapViews, SceneViews and UI
