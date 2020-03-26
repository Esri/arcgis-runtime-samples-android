# Feature layer shapefile

Open a shapefile stored on the device and display it as a feature layer with default symbology.

![Image of feature layer shapefile](feature-layer-shapefile.png)

## Use case

Shapefiles store location, shape and attributes of geospatial vector data. Shapefiles can be loaded directly into ArcGIS Runtime.

## How to use the sample

Run the sample and accept read permissions.

## How it works

1. Create a `ShapefileFeatureTable` passing in the URL of a shapefile.
2. Create a `FeatureLayer` using the shapefile feature table.
3. Add the layer to the map's operational layers.

## Relevant API

* FeatureLayer
* ShapefileFeatureTable

## Offline data

1. Download the data from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=d98b3e5293834c5f852f13c569930caa).
1. Extract the contents of the downloaded zip file to disk.
1. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
1. Execute the following command:
`adb push . /sdcard/ArcGIS/Samples/ShapeFile/Aurora_CO_shp/`

Link | Local Location
---------|-------|
|[Public Art Shapefile](https://www.arcgis.com/home/item.html?id=d98b3e5293834c5f852f13c569930caa)| `<sdcard>`/ArcGIS/Samples/ShapeFile/Aurora_CO_shp/Public_Art.shp|

## Tags

Layers, shapefile, shp, vector
