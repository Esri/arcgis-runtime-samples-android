# Symbolize a shapefile

This sample demonstrates how to override the default renderer of a shapefile when displaying with a FeatureLayer.

![Symbolize Shapefile App](symbolize-shapefile.png)

## How it works

1. Create a `ShapefileFeatureTable` by setting the path to a shapefile. 
2. To display the shapefile, create a `FeatureLayer` and set the feature layer table property to the `ShapeFileFeatureTable`.
3. If the layer is added to the `Map`, the default black and gray symbology is applied.
4. To override the default symbology, create a `SimpleRenderer` is created.
5. A `SimpleRenderer` is used to apply the same symbol to every feature. In this case, a yellow `SimpleFillSymbol` with a red `SimpleLineSymbol` as an outline.
6. Apply the symbol by setting the renderer to the `FeatureLayer`.

## Features

* FeatureLayer
* ShapefileFeatureTable
* SimpleRenderer
* SimpleFillSymbol
* SimpleLineSymbol

## Provision your device
1. Download the data from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=d98b3e5293834c5f852f13c569930caa).  
2. Extract the contents of the downloaded zip file to disk.  
3. Create an ArcGIS/Samples/Shapefile folder on your device. You can use the [Android Debug Bridge (adb)](https://developer.android.com/guide/developing/tools/adb.html) tool found in **<sdk-dir>/platform-tools**.
4. Open up a command prompt and execute the ```adb shell``` command to start a remote shell on your target device.
5. Navigate to your sdcard directory, e.g. ```cd /sdcard/```.  
6. Create the ArcGIS/samples/MapPackage directory, ```mkdir ArcGIS/Samples/Shapefile```.
7. You should now have the following directory on your target device, ```/sdcard/ArcGIS/Samples/Shapefile```. We will copy the contents of the downloaded data into this directory. Note:  Directory may be slightly different on your device.
8. Exit the shell with the, ```exit``` command.
9. While still in your command prompt, navigate to the folder where you extracted the contents of the data from step 1 and execute the following command: 
	* ```adb push Aurora_CO_shp /sdcard/ArcGIS/Samples/Aurora_CO_shp```


Link | Local Location
---------|-------|
|[Aurora Subdivions Shapefile](https://www.arcgis.com/home/item.html?id=d98b3e5293834c5f852f13c569930caa)| `<sdcard>`/ArcGIS/Samples/Shapefile/Aurora_CO_shp/Subdivisions.shp |