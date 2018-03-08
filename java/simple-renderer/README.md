# Simple renderer
### Category: Visualization
This sample demonstrates how to create a simple renderer and add it to a graphics overlay. Renderers define the symbology for all graphics in a graphics overlay (unless they are overridden by setting the symbol directly on the graphic). Simple renderers can also be defined on feature layers using the same code.

![Simple Renderer App](simple-renderer.png)

## Features

* Graphic
* GraphicsOverlay
* SimpleMarkerSymbol
* SimpleRenderer

## Developer Pattern

A simple renderer is created using a simple marker symbol (red cross) and set onto a graphics overlay. 3 points are created and added to graphics which are then added to the graphics overlay.   The graphics overlay is added to the map view so it is visible, all graphics are then styled with the same symbol that was defined in the renderer.

```java
//create a simple symbol for use in a simple renderer
SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.RED, 12); //size 12, style of cross
SimpleRenderer renderer = new SimpleRenderer(symbol);

//apply the renderer to the graphics overlay (so all graphics will use the same symbol from the renderer)
graphicOverlay.setRenderer(renderer);
```
