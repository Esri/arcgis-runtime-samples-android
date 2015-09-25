
# Display Device Location       
       
This sample demonstrates how you can enable Location Display and switch between different types of AutoPan Modes.       
         
![](display-device-location-1.png)
![](display-device-location-2.png)
![](display-device-location-3.png)

## Features
* Map
* MapView
* LocationDisplay
* LocationDisplay.AutoPanMode
 
## How to use the sample
This sample starts with the Map with an imagery basemap loaded in the MapView and Location display turned off. When you tap on the spinner it gives you the list of possible AutoPan Mode options along with turning the Location display on or off.

* Stop - Stops the Location Display 
* On - Starts the Location Display with AutoPan Mode set to Off
* Re-Center - Starts location display with auto pan mode set to Default
* Navigation - Starts location display with auto pan mode set to Navigation
* Compass - Starts location display with auto pan mode set to Compass

## Developer Pattern

```java
private MapView mMapView;
private LocationDisplay mLocationDisplay;

....
// get the MapView's LocationDisplay
mLocationDisplay = mMapView.getLocationDisplay();

....
// Start Location Display
if (!mLocationDisplay.isStarted())
mLocationDisplay.start();

....
// Stop Location Display
if (mLocationDisplay.isStarted())
mLocationDisplay.stop();
```

**Note :** You need to request user permissions in you app's Android manifest file by declaring either the ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission, in order to recieve location updates from NETWORK_PROVIDER or GPS_PROVIDER

``` xml
<manifest ... >
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    ...
</manifest>
```
