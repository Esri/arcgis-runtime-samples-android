# Show Magnifier
Tap and hold on a map to get a magnifier.

![Show Magnifier App](show-magnifier.png)

## How to use the sample
Tap and drag your finger across the map to move the magnifier across the map.

## How it works
`MapView.setMagnifierEnabled()` take a boolean to determine whether a magnifier should be shown on the `Map` when the user performs a long press gesture.  The default value is `false`.  You can also use the `MapView.setAllowMagnifierToPanMap()` to `true` to allow the map to be panned automatically when the magnifier gets near the edge of the `Map`.

## Relevant API
* ArcGISMap
* MapView
* Basemap.Type

#### Tags
MapViews, SceneViews and UI