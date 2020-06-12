# Offline geocode

Geocode addresses to locations and reverse geocode locations to addresses offline.

![Image of offline geocode](offline-geocode.png)

## Use case

You can use an address locator file to geocode addresses and locations. For example, you could provide offline geocoding capabilities to field workers repairing critical infrastructure in a disaster when network availability is limited.

## How to use the sample

Search for an address from the search bar or select a suggestion to `Geocode` the address and view the result on the map. Tap the location you want to reverse geocode, or double-tap and drag on the map to get real-time geocoding.

## How it works

1. Use the path of a .loc file to create a `LocatorTask` object.
2. Set up `GeocodeParameters` and call `GeocodeAsync` to get geocode results.

## Relevant API

* GeocodeParameters
* GeocodeResult
* LocatorTask
* ReverseGeocodeParameters

## Offline Data

1. Download the data [San Diego Streets Tile Package](http://www.arcgis.com/home/item.html?id=1330ab96ac9c40a49e59650557f2cd63) and [San Diego Offline Locator](http://www.arcgis.com/home/item.html?id=344e3b12368543ef84045ef9aa3c32ba) from ArcGIS Online.
2. Extract the contents of the downloaded zip file to disk.
3. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
4. Push the data into the scoped storage of the sample app:
 	* `adb push streetmap_SD.tpk /Android/data/com.esri.arcgisruntime.sample.offlinegeocode/files/streetmap_SD.tpk`
	* `adb push san-diego-locator /Android/data/com.esri.arcgisruntime.sample.offlinegeocode/files/san-diego-locator`

## Tags

geocode, geocoder, locator, offline, package, query, search
