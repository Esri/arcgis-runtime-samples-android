# Browse WFS layers

Browse a WFS service for layers and add them to the map.

![Image of browse WFS layers](browse-wfs-layers.png)

## Use case

Services often have multiple layers available for display. For example, a feature service for a city might have layers representing roads, land masses, building footprints, parks, and facilities. A user can choose to only show the road network and parks for a park accessibility analysis.

## How to use the sample

A list of layers in the WFS service will be shown. Select a layer to display.

Some WFS services return coordinates in X,Y order, while others return coordinates in lat/long (Y,X) order. If you don't see features rendered or you see features in the wrong location, use the checkbox to change the coordinate order and reload.

## How it works

1. Create a `WfsService` object with a URL to a WFS feature service.
2. Obtain a list of `WfsLayerInfo` from `WfsService.getServiceInfo()`.
3. When a layer is selected, create a `WfsFeatureTable` from the `WfsLayerInfo`.
4. Create a feature layer from the feature table.
5. Add the feature layer to the map.

## Relevant API

* FeatureLayer
* WfsFeatureTable
* WfsFeatureTable.AxisOrder
* WfsLayerInfo
* WfsService
* WfsServiceInfo

## About the data

The sample is configured with a sample WFS service, but you can load other WFS services if desired. The default service shows [Seattle downtown features](https://arcgisruntime.maps.arcgis.com/home/item.html?id=1b81d35c5b0942678140efc29bc25391) hosted on ArcGIS Online.

## Tags

browse, catalog, feature, layers, OGC, service, web, WFS
