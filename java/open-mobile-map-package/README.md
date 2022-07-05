# Open mobile map package

Display a map from a mobile map package.

![Image of open mobile map package](open-mobile-map-package.png)

## Use case

A mobile map package is an archive containing the data (specifically, basemaps and features) used to display an offline map.

## How to use the sample

When the sample opens, it will automatically display the map in the mobile map package. Pan and zoom to observe the data from the mobile map package.

## How it works

1. Create a `MobileMapPackage` specifying the path to the .mmpk file.
2. Load the mobile map package with `mmpk.loadAsync()`.
3. After it successfully loads, get the map from the .mmpk and add it to the map view: `mapView.setMap(mmpk.getMaps().get(0))`.

## Relevant API

* MapView
* MobileMapPackage

## Offline Data

1. Download the data from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=e1f3a7254cb845b09450f54937c16061).
2. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
3. Push the data into the scoped storage of the sample app:
`adb push Yellowstone.mmpk /Android/data/com.esri.arcgisruntime.sample.openmobilemappackage/files/Yellowstone.mmpk`

## About the data

This sample shows points of interest within a [Yellowstone Mobile Map Package](https://arcgisruntime.maps.arcgis.com/home/item.html?id=e1f3a7254cb845b09450f54937c16061) hosted on ArcGIS Online.

## Tags

mmpk, mobile map package, offline
