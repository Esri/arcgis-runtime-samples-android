# Open mobile map package

Display a map from a mobile map package.

![Image of open mobile map package](open-mobile-mappackage.png)

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

## Offline data
1. Download the data from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=e1f3a7254cb845b09450f54937c16061).  
2. Extract the contents of the downloaded zip file to disk.  
3. Create an ArcGIS/samples/MapPackage folder on your device. You can use the [Android Debug Bridge (adb)](https://developer.android.com/guide/developing/tools/adb.html) tool found in **<sdk-dir>/platform-tools**.
4. Open up a command prompt and execute the ```adb shell``` command to start a remote shell on your target device.
5. Navigate to your sdcard directory, e.g. ```cd /sdcard/```.  
6. Create the ArcGIS/samples/MapPackage directory, ```mkdir ArcGIS/samples/MapPackage```.
7. You should now have the following directory on your target device, ```/sdcard/ArcGIS/samples/MapPackage```. We will copy the contents of the downloaded data into this directory. Note:  Directory may be slightly different on your device.
8. Exit the shell with the, ```exit``` command.
9. While still in your command prompt, navigate to the folder where you extracted the contents of the data from step 1 and execute the following command: 
	* ```adb push Yellowstone.mmpk /sdcard/ArcGIS/samples/MapPackage```


Link | Local Location
---------|-------|
|[Yellowstone Mobile Map Package](https://www.arcgis.com/home/item.html?id=e1f3a7254cb845b09450f54937c16061)| `<sdcard>`/ArcGIS/samples/MapPackage/Yellowstone.mmpk |


## About the data

This sample shows points of interest within a [Yellowstone Mobile Map Package](https://arcgisruntime.maps.arcgis.com/home/item.html?id=e1f3a7254cb845b09450f54937c16061) hosted on ArcGIS Online.

## Tags

mmpk, mobile map package, offline
