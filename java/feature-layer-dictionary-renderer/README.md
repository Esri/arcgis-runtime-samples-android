# Dictionary Renderer with Feature Layer

Demonstrates how to display a FeatureLayer with military symbology, using ArcGIS Runtime's DictionaryRenderer.

![Dictionary Renderer with Feature Layer App](feature-layer-dictionary-renderer.png)

## How it works

This sample loads a number of point, line, and polygon feature tables from a Runtime geodatabase. For each feature table, a FeatureLayer is created, and a DictionaryRenderer object is created and applied to the layer. Note that each layer needs its own renderer, though all renderers can share the DictionarySymbolStyle, in which case all layers will use the same symbology specification (MIL-STD-2525D in the case of this sample). Each layer is added to the map, and when all layers are loaded, the map's viewpoint is set to zoom to the full extent of all feature layers.


## Relevant API

* ArcGISMap
* Basemap
* DictionaryRenderer
* Envelope
* FeatureLayer
* Geodatabase
* GeometryEngine
* MapView
* SymbolDictionary


## Offline data
1. Download the data from the table below.
2. Extract the contents of the downloaded zip file to disk.  
3. Create an ArcGIS/samples/Dictionary folder on your device. You can use the [Android Debug Bridge (adb)](https://developer.android.com/guide/developing/tools/adb.html) tool found in **<sdk-dir>/platform-tools**.
4. Open up a command prompt and execute the ```adb shell``` command to start a remote shell on your target device.
5. Navigate to your sdcard directory, e.g. ```cd /sdcard/```.  
6. Create the ArcGIS/samples/Dictionary directory, ```mkdir ArcGIS/samples/Dictionary```.
7. You should now have the following directory on your target device, ```/sdcard/ArcGIS/samples/Dictionary```. We will copy the contents of the downloaded data into this directory. Note:  Directory may be slightly different on your device.
8. Exit the shell with the, ```exit``` command.
9. While still in your command prompt, navigate to the folder where you extracted the contents of the data from step 1 and execute the following command: 
	* ```adb push militaryoverlay.geodatabase /sdcard/ArcGIS/samples/Dictionary```
	* ```adb push mil2525d.stylx /sdcard/ArcGIS/samples/Dictionary```


Link | Local Location
---------|-------|
|[Mil2525d Stylx File](https://www.arcgis.com/home/item.html?id=e34835bf5ec5430da7cf16bb8c0b075c)| `<sdcard>`/ArcGIS/samples/Dictionary/mil2525d.stylx |
|[Military Overlay geodatabase](https://www.arcgis.com/home/item.html?id=e0d41b4b409a49a5a7ba11939d8535dc)| `<sdcard>`/ArcGIS/samples/Dictionary/militaryoverlay.geodatabase |


## Tags

Layers.