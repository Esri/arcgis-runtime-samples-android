# Dictionary Renderer with Graphics Overlay

The dictionary renderer creates graphics using a local mil2525d style file and a XML file with key, value attributes for each graphic.

![Dictionary Renderer Graphics Overlay App](dictionary-renderer-graphics-overlay.png)

## How it works

This sample loads a number of point, line, and polygon military elements from an XML file and adds them as graphics to a `GraphicsOverlay`. A `DictionaryRenderer` is applied to the `GraphicsOverlay` in order to display the graphics with MIL-STD-2525D military symbology.

## Relevant API

* DictionaryRenderer
* DictionarySymbolStyle
* GraphicsOverlay

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
	* ```adb push mil2525d.stylx /sdcard/ArcGIS/samples/Dictionary```

Link | Local Location
---------|-------|
|[Mil2525d Stylx File](https://www.arcgis.com/home/item.html?id=e34835bf5ec5430da7cf16bb8c0b075c)| `<sdcard>`/ArcGIS/samples/Dictionary/mil2525d.stylx |

### Tags
Visualization
