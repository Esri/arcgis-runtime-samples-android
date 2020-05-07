# Set map spatial reference

Specify a map's spatial reference.

![Image of map spatial reference](set-map-spatial-reference.png)

## Use case

Choosing the correct spatial reference is important for ensuring accurate projection of data points to a map.  

## How to use the sample

Pan and zoom around the map. Observe how the map is displayed using the World Bonne spatial reference.

## How it works

1. Instantiate an `ArcGISMap` object using a spatial reference e.g. `ArcGISMap(SpatialReference.create(54024))`.
2. Instantiate a `Basemap` object using an `ArcGISMapImageLayer` object.
3. Set the basemap to the map using `map.setBasemap(basemap)`.
4. Pass the map to a `MapView` object using `MapView.setMap(map)`.

The ArcGIS map image layer will now use the spatial reference set to the ArcGIS map (World Bonne (WKID: 54024)) and not its default spatial reference.
 
## Relevant API

* ArcGISMap
* ArcGISMapImageLayer
* Basemap
* MapView
* SpatialReference

## Additional information

Operational layers will automatically project to this spatial reference when possible.

## Tags

project, SpatialReference, WKID
