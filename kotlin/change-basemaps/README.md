# Change Basemaps
### Category: Layers
![Change Basemaps App](change-basemaps.png)

The Change Basemaps sample app shows how you can change basemaps from an Android Navigation Drawer.  

## Features
* ArcGISMap
* MapView
* Basemap
* ViewPoint

## Developer Pattern
The `selectBasemap(position: Int)` function switches Basemaps based on the position of the navigation drawer selection. The **Basemap** is created from create methods on a `Basemap` object.  

```kotlin
mapView.map.basemap = Basemap.createStreets()
```
