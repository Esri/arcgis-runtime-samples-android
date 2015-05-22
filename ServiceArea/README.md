# Service Area
 A network service area is a region that encompasses all accessible streets. For instance, the 5-minute service area for a point on a network includes all the streets that can be reached within five minutes from that point.

## Sample Design
The Service Area sample uses the ServiceAreaTask to solve the problem of finding the service area polygons for a selected facility to the specified parameters.  On single tapping the map, the Service Area sample calculates the three service area polygons defined by the three break value EditText boxes using the location as the source facility. You can edit the Impedance breaks by tapping the edit icon from the action bar.  All the break values must be unique for the sample to work.

## Features
* [ServiceAreaTask](https://developers.arcgis.com/android/api-reference/reference/com/esri/core/tasks/na/ServiceAreaTask.html)
* [ServiceAreaParameters](https://developers.arcgis.com/android/api-reference/reference/com/esri/core/tasks/na/ServiceAreaParameters.html)
* [ServiceAreaResult](https://developers.arcgis.com/android/api-reference/reference/com/esri/core/tasks/na/ServiceAreaResult.html)
* [AsyncTask](http://developer.android.com/reference/android/os/AsyncTask.html)
* [Fragments](http://developer.android.com/guide/components/fragments.html)
* [FragmentManager](http://developer.android.com/reference/android/app/FragmentManager.html)
* [FragmentTransaction](http://developer.android.com/reference/android/app/FragmentTransaction.html)
