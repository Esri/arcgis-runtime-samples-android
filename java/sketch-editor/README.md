# Sketch editor

Use the Sketch Editor to edit or sketch a new point, line, or polygon geometry on to a map.

![Image of sketch editor App](sketch-editor.png)

## Use case

A field worker could annotate features of interest on a map (via the GUI) such as location of dwellings (marked as points), geological features (polylines), or areas of glaciation (polygons). 

## How to use the sample

Choose which geometry type to sketch from one of the available buttons along the bottom of the screen. Choose from points, multipoints, polylines, polygons, freehand polylines, and freehand polygons.

Use the buttons at the top of the screen to: undo or redo change made to the sketch whilst in sketch mode, and to save the sketch to the graphics overlay.

## How it works

1.  Create a `SketchEditor` and pass it to the MapView with `mapView.setSketchEditor(sketchEditor)`.
2.  Use `SketchEditor.start(SketchCreationMode)` to start sketching.
 * If editing an existing graphic's geometry, use `SketchEditor.start(graphic.getGeometry)`.
3.  Check to see if undo and redo are possible during a sketch session using `sketchEditor.canUndo()` and `sketchEditor.canRedo()`. If it's possible, use `sketchEditor.undo()` and `sketchEditor.redo()`.
4.  Check if sketch is valid using `sketchEditor.isSketchValid()`, then allow the sketch to be saved to a `GraphicsOverlay`.
5.  Get the geometry of the sketch using `sketchEditor.getGeometry()`, and create a new `Graphic` from that geometry. Add the graphic to the graphics overlay.
6.  To exit the sketch editor, use `sketchEditor.stop()`.

## Relevant API

* Geometry
* Graphic
* GraphicsOverlay
* MapView
* SketchCreationMode
* SketchEditor

## Tags

draw, edit, sketch
