# Sync Map and Scene Viewpoints
This sample demonstrates how to keep the viewpoints of multiple map or scene views in sync, so that navigating one view immediately updates the others.

![Sync Map and Scene Viewpoints App](sync-map-and-scene-viewpoints.png)

## How to use the sample
Pan, zoom, and rotate the map or scene view. The other view will update automatically to match your navigation. Note that maps are 2D while scenes are 3D, so the results may not look identical, but the centers and scales will be kept the same.

## How it works
`GeoView`, the parent class of both `MapView` and `SceneView`, has a property `ViewpointChangeListener` which is called each time the viewpoint updates. Inside this listener we get the viewpoint of the `GeoView` by calling `.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)`. We then pass that viewpoint into `setViewpoint(...)` on the other `GeoView`, thus synchronizing both views.

## Relevant API
* `GeoView`
* `GeoView.addViewpointChangedListener(...)`
* `GeoView.isNavigating()`
* `GeoView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)`
* `GeoView.setViewpoint(...)`

#### Tags
MapViews, SceneViews and UI