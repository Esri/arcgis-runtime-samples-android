# Colormap Renderer
Use a `ColormapRenderer` on `RasterLayer`. `ColormapRenderer` can be used to replace values on a `RasterLayer` with a color based on the original value brightness.

![Colormap Renderer App](colormap-renderer.png)

## How to use the sample
Simply start the app and allow read permissions.

## How it works
1.	Create a `Raster` from a raster file.
2.	Create a `RasterLayer` from the `Raster`.
3.	Create a `List<Integer>` representing colors. Colors at the beginning of the list replace the darkest values in the raster and colors at the end of the list replaced the brightest values of the raster.</li>
4.	Create a `ColormapRenderer` with the color list and apply it to the `RasterLayer` with `rasterLayer.setRasterRenderer(colormapRenderer)`.

## Relevant API
* ArcGISMap
* Basemap
* ColormapRenderer
* Raster
* RasterLayer

## Offline data
1. Download the **shasta-bw.zip** data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=95392f99970d4a71bd25951beb34a508).  
2. Extract the contents of the downloaded zip file to disk.  
3. Create an ArcGIS/samples/raster folder on your device. You can use the [Android Debug Bridge (adb)](https://developer.android.com/guide/developing/tools/adb.html) tool found in **<sdk-dir>/platform-tools**.
4. Open up a command prompt and execute the `adb shell` command to start a remote shell on your target device.
5. Navigate to your sdcard directory, e.g. `cd /sdcard/`.  
6. Create the ArcGIS/samples directory, `mkdir ArcGIS/samples/raster`.
7. You should now have the following directory on your target device, `/sdcard/ArcGIS/samples/raster`. We will copy the contents of the downloaded data into this directory. Note:  Directory may be slightly different on your device.
8. Exit the shell with the, `exit` command.
9. While still in your command prompt, navigate to the root folder where you extracted the contents of the data from step 1 and execute the following command: 
	* `adb push ShastaBW/. /sdcard/ArcGIS/samples/raster`


Link | Local Location
---------|-------|
|[shasta-bw.zip](https://arcgisruntime.maps.arcgis.com/home/item.html?id=95392f99970d4a71bd25951beb34a508)| `<sdcard>`/ArcGIS/samples/raster/ShastaBW.tif |
	
#### Tags
Visualization
