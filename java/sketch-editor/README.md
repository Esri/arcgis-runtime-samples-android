# Sketch Editor
The Sketch Editor can be used to draw on a map.

![Sketch Editor App](sketch-editor.png)

# How to use the sample
Use the buttons along the bottom of the screen to sketch: points, multipoints, polylines, polygons, freehand lines, and freehand polylines.

Use the buttons at the top of the screen to: undo, redo, and stop (finish) the sketch.

# How it works
1. Create a `SketchEditor` and add it to a `MapView`.
1. Use `SketchEditor.start(<SketchCreationMode>)` to start sketching. Modes (as enum) include:
	* `SketchCreationMode.POINT`
	* `SketchCreationMode.MULTIPOINT`
	* `SketchCreationMode.POLYLINE`
	* `SketchCreationMode.POLYGON`
	* `SketchCreationMode.FREEHAND_LINE`
	* `SketchCreationMode.FREEHAND_POLYGON`
1. Use `.undo()` and `.redo()` on your instance of sketch editor to undo and redo sketch events.
1. Use `.getGeometry()` on your instance of sketch editor to get the geometry and, for instance, use to to define a new `Graphic`.
1. Finally, use `.stop()` on your instance of sketch editor to stop sketching.

# Relevant API
* MapView
* SketchCreationMode
* SketchEditor

#### Tags
Maps and Scenes