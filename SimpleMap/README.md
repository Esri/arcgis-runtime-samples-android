# SimpleMap
The SimpleMap sample using the new at v10.2.6 simplification API from the [Application Toolkit for ArcGIS Android](https://developers.arcgis.com/android/guide/application-toolkit-for-arcgis-android.htm).  It shows how to access a a MapView defined in an XML layout, add a graphic to the MapView, and adding a single click listener to the MapView that performs a reverse geocode, resulting in an icon with callout added at the returned address. The state of the map (resolution and center, and the reverse geocoded graphic) are preserved if the activity is destroyed by the system.  

##Handing configuration changes
This sample demonstrates one approach to handling changes required when the device is rotated or a keyboard is shown. By using the android:configChanges attribute in the manifest file, the MapView and it's contents are preserved in these cases, giving minimal disruption to what the user sees on the screen. This approach may not be suitable in other apps where use is made of config-dependent layouts and other assets. See the following article for a description of how to handle config changes yourself:
http://developer.android.com/guide/topics/resources/runtime-changes.html#HandlingTheChange

## Features
* Reverse geocode tapped location using toolkit GeocodeHelper
* Add graphic to map using toolkit MapViewHelper
* Handle configuration changes manually to maintain map state through configuration changes 

## Sample Requirements
The SimpleMap sample depends on the Application Toolkit for ArcGIS Android. For instructions on how to add this toolkit to your project, and more information about the toolkit, see [Application Toolkit for ArcGIS Android](https://developers.arcgis.com/android/guide/application-toolkit-for-arcgis-android.htm).
