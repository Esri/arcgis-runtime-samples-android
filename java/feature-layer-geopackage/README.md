# Feature Layer (GeoPackage)
Open a GeoPackage and show a GeoPackage feature table in a feature layer.

![Feature layer GeoPackage App](feature-layer-geopackage.png)

## How to use the sample
Provision the device with local data as per the 'Offline Data' section below. Run the app and accept read permissions.

## How it works
1. Create a `GeoPackage` from a local path and load it with `loadAsync()`.
1. On successful load, get a `FeatureTable` from the `GeoPackage` using `getGeoPackageFeatureTables()`.

## Relevant API
* FeatureLayer
* FeatureTable
* GeoPackage

## Offline Data
1. Download the data from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=68ec42517cdd439e81b036210483e8e7).
1. Extract the contents of the downloaded zip file to disk.
1. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
1. Execute the following command: ```adb push AuroraCO.gpkg /sdcard/ArcGIS/Samples/GeoPackage/AuroraCO.gpkg```

Link | Local Location
---------|-------|
|[Aurora CO GeoPackage](https://www.arcgis.com/home/item.html?id=68ec42517cdd439e81b036210483e8e7)| `<sdcard>`/ArcGIS/Samples/GeoPackage/AuroraCO.gpkg|

#### Tags
Layers