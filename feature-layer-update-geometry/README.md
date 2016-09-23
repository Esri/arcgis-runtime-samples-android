#Feature Layer Update Geometry
This sample demonstrates how to update geometry in a feature layer.

![](feature-layer-update-geometry.png)

## How to use the sample
Tap on a feature on the map to select a feature. Once the feature is selected, tap on map to update its geometry

## How it works
The map view  provides a way to add a listener to screen taps using the ```setOnTouchListener```method. The app uses the ```MotionEvent``` passed in to the ```onSingleTapConfirmed``` method to perform identify/update geometry on mapview  based on the tolerance. Updates the geometry of the identified feature using ```setGeometry``` method and the updated feature is passed to the ```updateFeatureAsync``` method on FeatureTable. Finally the edits are applied to the service using ```applyEditsAsync``` method on FeatureTable