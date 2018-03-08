# Add Graphics with Symbols
### Category: Visualization
The **Add Graphics with Symbols** sample demonstrates how to add points, polylines, and polygons as graphics, set a symbol renderer to the graphics and add to a `GraphicsOverlay`.  The sample also adds `TextSymbol` to represent text as symbols on the `GraphicsOverlay`.

![Add Graphics with Symbols App](add-graphic-symbols.png)

## Features
* MapView
* Graphic
* GraphicsOverlay
* Point
* PointCollection
* Polygon
* Polyline

## Developer Pattern
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
