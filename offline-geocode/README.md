# Offline geocode
This sample demonstrates how you can geocode addresses to locations and reverse geocode locations to addresses when the device does not have network connectivity

# How to use the sample
The sample depends on basemap data to be located on the device. This includes installing a local tile map cache (tpk) to device as described below:

 * Download San Diego [tpk](http://www.arcgis.com/home/item.html?id=1330ab96ac9c40a49e59650557f2cd63) and [locator](http://www.arcgis.com/home/item.html?id=344e3b12368543ef84045ef9aa3c32ba) data from ArcGIS.
 * Create the the sample data folder at the root folder on your device, /{device-externalstoragepath}/ArcGIS/samples/Offlinegeocode.
 * Push the downloaded data contents from step 1 to your device:
  * /{device-externalstoragepath}/ArcGIS/samples/OfflineGeocoding/streetmap_SD.tpk
  * /{device-externalstoragepath}/ArcGIS/samples/OfflineGeocoding/san-diego-locator.loc
  * /{device-externalstoragepath}/ArcGIS/samples/OfflineGeocoding/san-diego-locator.locb
  * /{device-externalstoragepath}/ArcGIS/samples/OfflineGeocoding/san-diego-locator.lox
  * /{device-externalstoragepath}/ArcGIS/samples/OfflineGeocoding/san-diego-locator.loc.xml

  
---  
|  Initial screen                                             |  List                                         |
|:-----------------------------------------------------------:|:-----------------------------------------------------------:|
|![initial](https://cloud.githubusercontent.com/assets/12448081/16972352/8cc3681e-4ddf-11e6-864e-aa2726e0631e.png)|![list](https://cloud.githubusercontent.com/assets/12448081/17008290/b5ffa178-4ea4-11e6-8913-1fa3cd3711d1.png)|      

|  Geocode                                                    |  Reverse Geocode
|:-----------------------------------------------------------:|:-----------------------------------------------------------:|
|![geocode](https://cloud.githubusercontent.com/assets/12448081/16972376/b67528aa-4ddf-11e6-81f4-0a3559cd7fdd.png)|![revGeocode](https://cloud.githubusercontent.com/assets/12448081/16972382/c0fff502-4ddf-11e6-8d3e-0b26e06ec216.png)|
---

Type the address in the Search menu option or select from the list to `Geocode` the address and view the result on the map. Long-press on the location you want to `Reverse Geocode`. Selecting the output pin enables real-time reverse geocoding. Select the pin to highlight the `PictureMarkerSymbol` and tap-hold and drag on the map to get real-time geocoding.


# Developer Pattern
Use the path of SanFrancisco.loc to create an object of `LocatorTask`. Set up `GeocodeParameters` and run asynchronous method geoCodeAsync to get GeocodeResults. Methods ```getDisplayLocation()``` and `getLabel()` on geocode results is then used to fetch location and address.

```java
// Execute async task to find the address
        mLocatorTask.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
                    // Call geocodeAsync passing in an address
                    final ListenableFuture<List<GeocodeResult>> geocodeFuture = mLocatorTask.geocodeAsync(address,
                            mGeocodeParameters);
                    geocodeFuture.addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Get the results of the async operation
                                List<GeocodeResult> geocodeResults = geocodeFuture.get();

                                if (geocodeResults.size() > 0) {
                                    // Use the first result - for example
                                    // display on the map
                                    mGeocodedLocation = geocodeResults.get(0);
                                    displaySearchResult(mGeocodedLocation.getDisplayLocation(), mGeocodedLocation.getLabel());

                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.location_not_foud) + address,
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (InterruptedException|ExecutionException e) {
                                // Deal with exception...
                                e.printStackTrace();
                            }
                            geocodeFuture.removeDoneListener(this);
                        }
                    });
                }
            }
        });
        mLocatorTask.loadAsync();
```
On similar basis, after setting up ```ReverseGeocodeParameters```, ```LocatorTask.reverseGeocodeAsync``` returns a list of geocode results.
