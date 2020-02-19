# Feature layer (geodatabase)

Display features from a local geodatabase.

![Image of feature layer geodatabase](feature-layer-geodatabase.png)

## Use case

Accessing data from a local geodatabase is useful when working in an environment that has an inconsistent internet connection or that does not have an internet connection at all. For example, a department of transportation field worker might source map data from a local geodatabase when conducting signage inspections in rural areas with poor network coverage. 

## How to use the sample

Pan and zoom around the map. View the data loaded from the geodatabase.

## How it works

1. Create a geodatabase using the provided local resource, `new Geodatabase(geodatabaseResourceUrl)`.
2. Wait for geodatabase to load, `Geodatabase.addDoneLoadingListener(runnable)`.
3. Get the 'Trailheads' `GeodatabaseFeatureTable` from the geodatabase, `Geodatabase.getGeodatabaseFeatureTable(tableName)`.
4. Create feature layer using the table from above, `new FeatureLayer(geodatabaseFeatureTable)`.
5. Add feature layer to `ArcGISMap` with `ArcGISMap.getOperationalLayers().add(featureLayer)`.

## Relevant API

* FeatureLayer
* Geodatabase
* GeodatabaseFeatureTable

## Offline data

1. Download the geodatabase from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=2b0f9e17105847809dfeb04e3cad69e0).
1. Open your command prompt and navigate to the folder where you downloaded the data from step 1.
1. Execute the following commands:

    `adb push LA_Trails.geodatabase /Android/data/com.esri.arcgisruntime.sample.featurelayergeodatabase/files/LA_Trails.geodatabase`

Link | Local Location
---------|-------|
|[Los Angeles Trailheads](https://www.arcgis.com/home/item.html?id=2b0f9e17105847809dfeb04e3cad69e0)| `/Android/data/com.esri.arcgisruntime.sample.featurelayergeodatabase/files/geodatabase/LA_Trails.geodatabase |

## About the data

The sample shows trailheads in the greater Los Angeles area displayed on top of a vector tile basemap.

## Additional information

One of the ArcGIS Runtime data set types that can be accessed via the local storage of the device (i.e. hard drive, flash drive, micro SD card, USB stick, etc.) is a mobile geodatabase. A mobile geodatabase can be provisioned for use in an ArcGIS Runtime application by ArcMap. The following provide some helpful tips on how to create a mobile geodatabase file:

In ArcMap, choose File > Share As > ArcGIS Runtime Content from the menu items to create the .geodatabase file (see the document: http://desktop.arcgis.com/en/arcmap/latest/map/working-with-arcmap/creating-arcgis-runtime-content.htm). 

Note: You could also use the 'Services Pattern' and access the Geodatabase class via a Feature Service served up via ArcGIS Online or ArcGIS Enterprise. Instead of using the Geodatabase class to access the .geodatabase file on disk, you would use GeodatabaseSyncTask point to a Uri instead. For more information review the document: https://developers.arcgis.com/net/latest/wpf/guide/create-an-offline-layer.htm.

## Tags

geodatabase, mobile, offline
