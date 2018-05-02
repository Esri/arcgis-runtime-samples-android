# Change Basemaps
### Category: Maps and Scenes
![Change Basemaps App](change-basemaps.png)

The Change Basemaps sample app shows how you can change basemaps from an Android Navigation Drawer.  

## Features
* ArcGISMap
* MapView
* Basemap
* ViewPoint

## Developer Pattern
The `selectBasemaps()` method switches Basemaps based on the position of the navigation drawer selection. The **Basemap** is created from create methods on a `Basemap` object.  

```java
mMap.setBasemap(Basemap.createStreets());
```
