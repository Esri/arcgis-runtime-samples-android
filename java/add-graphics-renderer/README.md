# Add Graphics Renderer

![Add Graphics Renderer App](add-graphics-renderer.png)

The **Add Graphics Renderer** sample demonstrates how to add graphics to a List, create a `SimpleRenderer` to represent a symbol and style, and add the renderer to the `MapView`.

## Features
* MapView
* Graphic
* GraphicsOverlay
* ListenableList
* SimpleRenderer
* SimpleMarkerSymbol

## Developer Pattern
Graphics are added to a `GraphicsOverlay` without any symbols or styles. You create a `Renderer` to add to the `GraphicsOverlay` which defines the symbol as `SimpleMarkerSymbol` which sets the style to be rendered.

```java
// point graphic
Point pointGeometry = new Point(40e5, 40e5, SpatialReferences.getWebMercator());
// red diamond point symbol
SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.RED, 10);
// create graphic for point
Graphic pointGraphic = new Graphic(pointGeometry);
// create a graphic overlay for the point
GraphicsOverlay pointGraphicOverlay = new GraphicsOverlay();
// create simple renderer
SimpleRenderer pointRenderer = new SimpleRenderer(pointSymbol);
pointGraphicOverlay.setRenderer(pointRenderer);
// add graphic to overlay
pointGraphicOverlay.getGraphics().add(pointGraphic);
// add graphics overlay to the MapView
mMapView.getGraphicsOverlays().add(pointGraphicOverlay);
```
