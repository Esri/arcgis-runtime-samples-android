# Map Sketching

![Map Sketching App](map-sketching.png)

This sample demonstrates how you can sketch graphics on a SketchGraphicsOverlay. Points, Polylines, and Polygons can be drawn. Drawing events can be undone and redone, and the entire contents of the SketchGraphicsOverlay can be erased.

## Features
* ArcGISMap
* MapView
* Basemap.Type
* GraphicsOverlay
* Point, Polyline, Polygon

## How to use

#### Sketch Point

  1. Tap the point icon in the bottom left.
  2. Single tap on the map. A red placement symbol will be drawn on the map.
  3. Optionally, tap again or drag the placement symbol to reposition the point.
  4. Long press or tap point icon to finish the point, which will change to a blue placed symbol.

#### Sketch Polyline

  1. Tap the polyline icon in the bottom left.
  2. Single tap on the map. A red placement symbol will be drawn on the map.
  3. All additional single taps will add a new point, change the symbol of the previous point to the polyline vertex symbol and add a midpoint to the new line segment.
  4. Optionally, any vertex or midpoint can be single tapped to select it, and then dragged to reposition it.
  5. Long press or tap polyline icon to finish the polyline, which will change the final working point to the polyline vertex symbol, remove all midpoints, and change the line color to blue.

#### Sketch Polygon

  1. Tap the polygon icon in the bottom left.
  2. Single tap on the map. A red placement symbol will be drawn on the map.
  3. All additional single taps will add a new point, change the symbol of the previous point to the polyline vertex symbol and add a midpoint to the new line segment. Once three or more points are sketched, the polygon will be drawn within the polyline.
  4. Optionally, any vertex or midpoint can be single tapped to select it, and then dragged to reposition it.
  5. Long press or tap polygon icon to finish the polygon, which will change the final working point to the polyline vertex symbol, remove all midpoints, and change the line color to blue.

#### Undo

  1. First, a drawing event must take place (adding a point, polyline, polygon, moving a point, etc) for the undo button to be enabled.
  2. Click the undo button in the bottom right, and the last event will be undone.
  3. Once all current events are undone, the undo button will become disabled again.

#### Redo

  1. First, an event must be undone for the redo button to be enabled.
  2. Click the redo button in the bottom right, and the last undone event will be redone.
  3. Once all current undone events are redone, the redo button will become disabled again.

#### Clear

  1. First, there must be graphics presents on the SketchGraphicsOverlay for the clear button to be enabled.
  2. Click the clear button in the bottom right, and all of the current graphics will be removed.
  3. When no graphics are present on the SketchGraphicsOverlay (either by undoing them all or clearing them), the clear button will be disabled.
