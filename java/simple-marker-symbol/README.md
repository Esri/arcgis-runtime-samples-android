#Simple marker symbol
This sample adds a graphic to a graphics overlay with a symbol of a red point specified via a simple marker symbol.

![Simple Marker Symbol app screenshot](simple-marker-symbol.png)

## Features
- Graphic
- GraphicsOverlay
- SimpleMarkerSymbol

## Developer Pattern
A point geometry is created from some known coordinates, a simple marker symbol is constructed and both are set on a graphic. The graphic is added to a graphics overlay in the map view so that it is visible.

```java
//create a simple marker symbol
SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 12); //size 12, style of circle

//add a new graphic with a new point geometry
Point graphicPoint = new Point(-226773, 6550477, SpatialReferences.getWebMercator());
Graphic graphic = new Graphic(graphicPoint, symbol);
graphicsOverlay.getGraphics().add(graphic);
```
