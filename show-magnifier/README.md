# Show Magnifier

![Show Magnifier App](show-magnifier.png)

This sample demonstrates how you can tap and hold on a map to get the magnifier. You can also pan while tapping and holding to move the magnifier across the map.

```MapView.setMagnifierEnabled()``` take a boolean to determine whether a magnifier should be shown on the ```Map``` when the user performs a long press gesture.  The default value is ```false```.  You can also use the ```MapView.setAllowMagnifierToPanMap()``` to ```true``` to allow the map to be panned automatically when the magnifier gets near the edge of the ```Map```.

```java
// enable magnifier
mMapView.setMagnifierEnabled(true);
// allow magnifier to pan near the edge of the map bounds
mMapView.setAllowMagnifierToPanMap(true);
```

## Features
* ArcGISMap
* MapView
* Basemap.Type
