
# Export Map As Image
       
This sample demonstrates how you can export current map view as an image and share with other applications.
         
## Features
* Map
* MapView
* ExportImageAsync

## How to use the sample
This sample starts with the Map with a web map portal item loaded in the MapView.
The float button is to export current map view to an image and the map image can be shared via others applications.

## Developer Pattern

```java
private MapView mMapView;

....
// get the MapView's LocationDisplay
mLocationDisplay = mMapView.getLocationDisplay();

....
final ListenableFuture<Bitmap> export = mMapView.exportImageAsync();
export.addDoneListener(new Runnable() {
  @Override public void run() {
    try {
      Bitmap currentMapImage = export.get();
      shareImage(saveToFile(currentMapImage));
    } catch (Exception e) {
      Log.d(TAG, "Fail to export map image: " +e.getCause().toString());
    }
  }
});


```