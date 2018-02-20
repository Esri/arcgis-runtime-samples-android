# OpenStreetMap Layer

![OpenStreetMap Layer App](openstreetmap-layer.png)

This sample demonstrates how to add the OpenStreetMap layer to a map as a Basemap.

## Features

* ArcGISMap
* Basemap
* MapView

## Developer Pattern

Instantiate an `ArcGISMap` with a `Basemap.Type.OPEN_STREET_MAP` and add it to a `MapView`. The attribution text that is required by OpenStreetMap is automatically added to the `MapView's` attributionText without any additional code.

```java
// instantiate an ArcGISMap with OpenStreetMap Basemap
ArcGISMap map = new ArcGISMap(Basemap.Type.OPEN_STREET_MAP, 34.056295, -117.195800, 10);
mMapView.setMap(map);
```