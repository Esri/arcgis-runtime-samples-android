# Hillshade renderer

This sample demonstrates how to use hillshade renderer on a raster

## How to use the sample

Tap on the wrench button in the Action Bar to change the settings for the `HillshadeRenderer`. The sample allows you to change the `Altitude`, `Azimuth` and `Slope type`. You can tap on the `Render` button to update the raster.

![](image1.png)
![](image2.png)

## How it works

The sample uses the `HillshadeRenderer` class to render new hillshades. The parameters provided by the user are passed to `HillshadeRender` at instantiation: `new HillshadeRenderer(mAltitude, mAzimuth, mZFactor, mSlopeType, mPixelSizeFactor, mPixelSizePower, mOutputBitDepth);` which returns a `RasterRenderer`. The `RasterRenderer` is then added to the `RasterLayer`. 

## Provision your device
1. Download the **TBD.zip** data from [ArcGIS Online](TBD).  
2. Extract the contents of the downloaded zip file to disk.  
3. Create an ArcGIS/samples/raster folder on your device. You can use the [Android Debug Bridge (adb)](https://developer.android.com/guide/developing/tools/adb.html) tool found in **<sdk-dir>/platform-tools**.
4. Open up a command prompt and execute the `adb shell` command to start a remote shell on your target device.
5. Navigate to your sdcard directory, e.g. `cd /sdcard/`.  
6. Create the ArcGIS/samples directory, `mkdir ArcGIS/samples/raster`.
7. You should now have the following directory on your target device, `/sdcard/ArcGIS/samples/raster`. We will copy the contents of the downloaded data into this directory. Note:  Directory may be slightly different on your device.
8. Exit the shell with the, `exit` command.
9. While still in your command prompt, navigate to the root folder where you extracted the contents of the data from step 1 and execute the following command: 
	* `adb push TBD/. /sdcard/ArcGIS/samples/raster`
	

	Link | Local Location
	---------|-------|
	|[TBD.zip](TBD)| `<sdcard>`/ArcGIS/samples/raster/srtm.tiff |
