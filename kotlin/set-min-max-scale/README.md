# Set min max scale

Restrict zooming between specific scale ranges.

![Image of min max scale](set-min-max-scale.png)

## Use case

Data may only appear at a certain scale on a map, and may be visually lost if zooming too far in or out. Setting the minimum and maximum scales ensures the zoom extents are appropriately limited for the purposes of the map.

## How to use the sample

Zoom in and out of the map. The zoom extents of the map are limited between the given minimum and maximum scales.

## How it works

1. Instantiate an `ArcGISMap` object.
2. Set min and max scales of map with `map.minScale` and `map.maxScale`.
3. Pass the map to a `MapView` object.

## Relevant API

* ArcGISMap
* Basemap
* MapView
* ViewPoint

## Tags

area of interest, level of detail, maximum, minimum, scale, viewpoint
