# Cut Geometry
Cut a geometry with a polyline using the GeometryEngine.

![Cut Geometry App](cut-geometry.png)

## How to use the sample
Click the "Cut" button to cut the polygon with the polyline and see the resulting parts.

## How it works
To cut a geometry with a polyline:

1. Use the static method `GeometryEngine.cut(geometry, polyline)`.
1. Loop through the `List <Geometry>` of cut pieces. Keep in mind that some of these geometries may be multi-part.

## Relevant API
* ArcGISMap
* Basemap
* Geometry
* GeometryEngine
* Graphic
* GraphicsOverlay
* MapView
* Point
* PointCollection
* Polygon
* Polyline
* SimpleFillSymbol
* SimpleLineSymbol
* SpatialReferences

#### Tags
Edit and Manage Data
