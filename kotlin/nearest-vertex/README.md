# Nearest vertex

Find the closest vertex and coordinate of a geometry to a point.

![Image of nearest vertex](nearest-vertex.png)

## Use case

Determine the shortest distance between a location and the boundary of an area. For example, developers can snap imprecise user clicks to a geometry if the click is within a certain distance of the geometry.

## How to use the sample

Click anywhere on the map. An orange cross will show at that location. A blue circle will show the polygon's nearest vertex to the point that was clicked. A red diamond will appear at the coordinate on the geometry that is nearest to the point that was clicked. If clicked inside the geometry, the red and orange markers will overlap. The information box showing distance between the clicked point and the nearest vertex/coordinate will be updated with every new location clicked.

## How it works

1. Get a `Geometry` and a `Point` to check the nearest vertex against.
2. Call `GeometryEngine.nearestVertex(inputGeometry, point)`.
3. Use the returned `ProximityResult` to get the `Point` representing the polygon vertex, and to determine the distance between that vertex and the clicked point.
4. Call `GeometryEngine.nearestCoordinate(inputGeometry, point)`.
5. Use the returned `ProximityResult` to get the `Point` representing the coordinate on the polygon, and to determine the distance between that coordinate and the clicked point.

## Relevant API

* GeometryEngine
* ProximityResult

## Tags

analysis, coordinate, geometry, nearest, proximity, vertex
