# Service feature table (Cache)

Display a feature layer from a service using the **on interaction cache** feature request mode.

![Image of service feature table on interaction cache](service-feature-table-cache.png)

## Use case

`ServiceFeatureTable` supports three request modes, which define how features are requested from the service and stored in the local table. The feature request modes have different performance characteristics. Use **On interaction cache** in scenarios with large amounts of infrequently edited data. See [Table performance concepts](https://developers.arcgis.com/net/latest/wpf/guide/layers.htm#ESRI_SECTION1_40F10593308A4718971C9A8F5FB9EC7D) to learn more.

## How to use the sample

Run the sample and pan and zoom around the map. With each interaction, features will be requested and stored in a local cache. Each subsequent interaction will display features from the cache and only request new features from the service.

## How it works

1. Set the `ServiceFeatureTable.FeatureRequestMode` property of the service feature table to `ON_INTERACTION_CACHE` before the table is loaded.
2. Add the table to the map using a `FeatureLayer`; features will be requested for the visible extent as the user pans and zooms.

## Relevant API

* FeatureLayer
* FeatureRequestMode.ON_INTERACTION_CACHE
* ServiceFeatureTable
* ServiceFeatureTable.setFeatureRequestMode

## About the data

The U.S. National Bridge Inventory describes 600,000 bridges in the United States. The sample uses [US Bridges](https://arcgisruntime.maps.arcgis.com/home/item.html?id=250b103a722c4e1ea71e562eac61be1b), a modified copy of the U.S. National Bridge Inventory hosted on ArcGIS Online. The sample opens with an initial visible extent centered over Bridgeport, CT.

## Additional information

**On interaction cache** is the default feature request mode. Features are requested automatically for the visible extent as the users pans and zooms the map. If the user returns to an area where features have previously been requested, those features won't be requested again.

## Tags

cache, feature request mode, performance
