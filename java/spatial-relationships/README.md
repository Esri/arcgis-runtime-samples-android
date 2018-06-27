# Spatial Relationships 
Shows how to use the `GeometryEngine` to determine spatial relationships between two geometries

![Spatial Relationships App](spatial-relationships.png)

## How to use this sample 
Click on one of the three graphics to select it. The relationships activity will show the spatial relationships the selected graphic has to the other graphic geometries.

## How it works
To check the relationship between geometries.

1. Get the geometry from two different graphics. In this example the geometry of the selected graphic is compared to the geometry of each graphic not selected.
1. Use the methods in `GeometryEngine` to check the relationship between the geometries, e.g. `contains`, `disjoint`, `intersects`, etc. If the method returns `true`, the relationship exists.

## Relevant API
* ArcGISMap
* Basemap
* Geometry
* GeometryEngine
* GeometryType
* Graphic
* GraphicsOverlay
* MapView
* Point
* PointCollection
* Polygon
* Polyline
* SimpleFillSymbol
* SimpleLineSymbol
* SimpleMarkerSymbol

#### Tags
Analysis

