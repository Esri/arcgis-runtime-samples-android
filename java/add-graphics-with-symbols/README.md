# Add Graphics with Symbols
Add points, polylines, and polygons as graphics, set a symbol renderer to the graphics and add to a graphics overlay. The sample also adds text symbols to represent text as symbols on the graphics overlay.

![Add Graphics with Symbols App](add-graphics-with-symbols.png)

## How to use the sample
Simply run the sample.

## How it works
Graphics are added to a `GraphicsOverlay` without any symbols or styles. To include a symbol with a graphic, create a `Graphic` with a `Symbol` and `Geometry` and add it to the `GraphicsOverlay`. 

```java
//define a polyline for the boat trip
Polyline boatRoute = getBoatTripGeometry();
//define a line symbol
SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.rgb(128, 0, 128), 4);
//create the graphic
Graphic boatTripGraphic = new Graphic(boatRoute, lineSymbol);
//add to the graphic overlay
graphicOverlay.getGraphics().add(boatTripGraphic);
```

## Relevant API
* Graphic
* GraphicsOverlay
* MapView
* Point
* PointCollection
* Polygon
* Polyline

#### Tags
Visualization