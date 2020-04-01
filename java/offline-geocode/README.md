# Offline geocode

Geocode addresses to locations and reverse geocode locations to addresses offline.

![Image of offline geocode](offline-geocode.png)

## Use case

You can use an address locator file to geocode addresses and locations. For example, you could provide offline geocoding capabilities to field workers repairing critical infrastructure in a disaster when network availability is limited.

## How to use the sample

Select an address from the drop-down list to geocode the address and view the result on the map. Tap the location you want to reverse geocode. Single tap on the pin to highlight it and then tap-hold and drag on the map to get real-time geocoding.

## How it works

1. Use the path of a .loc file to create a `LocatorTask` object. 
2. Set up `GeocodeParameters` and call `GeocodeAsync` to get geocode results.

## Relevant API

* GeocodeParameters
* GeocodeResult
* LocatorTask
* ReverseGeocodeParameters

## Offline data
The sample depends on basemap data to be located on the device. This includes installing a local tile map cache (tpk) to device as described below:

1. Download the data from the table below. 
2. Extract the contents of the downloaded zip file to disk. 
3. Create an ArcGIS/samples/OfflineGeocoding folder on your device. You can use the [Android Debug Bridge (adb)](https://developer.android.com/guide/developing/tools/adb.html) tool found in **<sdk-dir>/platform-tools**.
4. Open up a command prompt and execute the `adb shell` command to start a remote shell on your target device.
5. Navigate to your sdcard directory, e.g. `cd /sdcard/`.  
6. Create the ArcGIS/samples/FLGdb directory, `mkdir ArcGIS/samples/OfflineGeocoding`.
7. You should now have the following directory on your target device, `/sdcard/ArcGIS/samples/OfflineGeocoding`. We will copy the contents of the downloaded data into this directory. Note:  Directory may be slightly different on your device.
8. Exit the shell with the, `exit` command.
9. While still in your command prompt, navigate to the folder where you extracted the contents of the data from step 1 and execute the following command: 
	* `adb push streetmap_SD.tpk /sdcard/ArcGIS/samples/OfflineGeocoding`
	* `adb push san-diego-locator.loc /sdcard/ArcGIS/samples/OfflineGeocoding`
	* `adb push san-diego-locator.locb /sdcard/ArcGIS/samples/OfflineGeocoding`
	* `adb push san-diego-locator.lox /sdcard/ArcGIS/samples/OfflineGeocoding`
	* `adb push san-diego-locator.loc.x /sdcard/ArcGIS/samples/OfflineGeocoding`

Link     |
---------|
|[San Diego Streets Tile Package](http://www.arcgis.com/home/item.html?id=1330ab96ac9c40a49e59650557f2cd63)|
|[San Diego Offline Locator](http://www.arcgis.com/home/item.html?id=344e3b12368543ef84045ef9aa3c32ba)|

## Tags

geocode, geocoder, locator, offline, package, query, search
