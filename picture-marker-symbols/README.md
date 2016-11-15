# Picture marker symbols
This sample shows how to create picture marker symbols from the different types of picture resources which are available, whether they are sourced from a URL, locally on the device or in the app.

![Picture Marker Symbols](picture-marker-symbols.png) 

## How it works
The 3 picture marker symbols that you see in the app are all constructed from different types of resources and then added to a graphic which is then added to a graphics overlay. The campsite icon is constructed from a URL, because this is a remote resource the symbol needs to be loaded before it is added to a graphic and added to the map. The blue pin with a star is created from an application resource called a `Drawable`, these also need to be loaded before they are added to the map. The orange pin is created from a file path on disk (which is written to disk when the app starts and cleaned up when the app closes).

## Developer Pattern
You can create a `PictureMarkerSymbol` from a URL.

```java
//Create a picture marker symbol from a URL resource
//When using a URL, you need to call load to fetch the remote resource
final PictureMarkerSymbol campsiteSymbol = new PictureMarkerSymbol(
    "http://sampleserver6.arcgisonline.com/arcgis/rest/services/Recreation/FeatureServer/0/images/e82f744ebb069bb35b234b3fea46deae");
//Optionally set the size, if not set the image will be auto sized based on its size in pixels,
//its appearance would then differ across devices with different resolutions.
campsiteSymbol.setHeight(18);
campsiteSymbol.setWidth(18);
campsiteSymbol.loadAsync();
```