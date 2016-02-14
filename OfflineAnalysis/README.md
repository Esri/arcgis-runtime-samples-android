# Offline Analysis
The offline analysis app shows how to perform Line of Sight and Viewshed analysis on raster files provisioned on an android device.  The Analysis features are a [BETA API](https://developers.arcgis.com/android/guide/release-notes.htm#ESRI_SECTION1_A1A4B44D91824E9FB79D2E7CF32B4CA1) at v10.2.4 and are subject to change when the API goes final.  We encourage any and all feedback on the API and it's feature capability.  

## Features
* Direct read of raster files
* Line of Sight analysis
* Viewshed analysis
* Override MapOnTouchListener for custom gestures

## Sample Design
This sample has one class, ```MainActivity```, which has two inner classes to override the ```MapOnTounchListener``` for custom gestures.  The raster file local to your device is added to a ```MapView``` in the ```onCreate``` method.  There are two ```Menu``` items added to the ```ActionBar``` in the ```OnCreateOptionsMenu``` method.  The core functionality is provided by the ```performLOS``` and ```calculateViewshed``` methods.  These show the patterns for Line of Sight and Viewshed analysis respectfully.  The high level pattern is offered below:

* Create an analysis object, ```LineOfSight``` or ```Viewshed```, from a raster file
* Create a ```(Raster)Layer``` from the analysis object
* Set some observer features
* Set an observer location, typically through interaction with the map, e.g. ```MapOnTounchListener```

### Add Analysis Beta libs
This sample uses **Beta** native libs that are only available in the [SDK download](https://developers.arcgis.com/android).  Download the SDK and follow the instructions below to work with this sample.

1. Create a **/src/main/jniLibs** directory in the sample project
2. From the root of your SDK download directory, copy the **/libs/[platform]/librs.main.so** and **/libs/[platform]/librender_script_analysis.so** into the **jniLibs** folder created in step 1.  Do not include the `libs` folder from the SDK.  

You should end up with the following project directory: `/src/main/jniLIbs/[platform]/librs.main.so` & `/src/main/jniLIbs/[platform]/librender_script_analysis.so`

Where **[platform]** represents the platform architecture your device is running, e.g.  **/libs/armeabi-v7a/librs.main.so** and **/libs/armeabi-v7a/librender_script_analysis.soo** for ARM.

### Add Raster File
You will need to provision a raster dem file to your android device prior to working with this sample.  A list of supported raster types can be found [here](https://developers.arcgis.com/android/guide/release-notes.htm#ESRI_SECTION1_74BB7A1174F74D27BB681BE5EF619C48).  You can put your raster file anywhere on your device that the app has access to.  By default the app will look for your raster file starting from the primary external storage directory returned by ```Environment.getExternalStorageDirectory()``` with **ArcGIS/samples/OfflineAnalysis/** subdirectory.  You can change the data path by editing the **string.xml** resource file.  It is mandatory that you change the raster file name variable to point to your raster file.  Open **strings.xml** and edit the following string resource variables:

```xml
    <!-- data paths; optionally change to alternate path -->
    <string name="raster_dir">ArcGIS/samples/OfflineAnalysis</string>
    <!-- raster file name; mandatory add your file name here -->
    <string name="raster_file">RASTER_FILE_NAME</string>
```

#### Push file to device
The following ```adb``` command is used to **push** files to your device:  

```
$ adb push <local> <remote>
```

In the commands, ```<local>``` and ```<remote>``` refer to the paths to the target file/directory on your development machine and the device.  For Example:  

```
$ adb push raster.tiff /sdcard/ArcGIS/samples/OfflineAnalysis/
```

More information about using the Android Debug Bridget can be found [here](http://developer.android.com/tools/help/adb.html).  

## Sample Usage
**Offline Analysis**
This sample app supports both Line of Sight and Viewshed analysis on an elevation raster type. The app will open to with the raster file as the basemap of the map.  

* Select the ```ActionBar``` overlay button
* Choose between **Line of Sight** and **Viewshed** for analysis

### Line of Sight
* The default observer is the center of the map
* Long press on the map to change the observer's position
* Single tap the map to change the target's position and execute the **Line of Sight** function
  * Alternatively, you can drag-move on the map to change the target's position

### Viewshed
* Single tap on the map to change the observer's position and execute the **Viewshed** function
