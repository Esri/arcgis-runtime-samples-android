# Picture Marker Symbols
Create picture marker symbols from the different types of picture resources available, whether they are sourced from a URL, locally on the device, or in the app.

![Picture Marker Symbols App](picture-marker-symbols.png) 

## How to use the sample
Run the sample and allow read permission.

## How it works
The 3 picture marker symbols that you see in the app are all constructed from different types of resources and then added to a graphic which is then added to a graphics overlay. The campsite icon is constructed from a URL, because this is a remote resource the symbol needs to be loaded before it is added to a graphic and added to the map. The blue pin with a star is created from an application resource called a `Drawable`, these also need to be loaded before they are added to the map. The orange pin is created from a file path on disk (which is written to disk when the app starts and cleaned up when the app closes).

## Relevant API
* Graphic
* GraphicsOverlay
* MapView
* PictureMarkerSymbol
* Point

#### Tags
Visualization