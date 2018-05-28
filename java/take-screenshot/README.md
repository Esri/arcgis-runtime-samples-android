# Take Screenshot
Export a map as an image file. `MapView.exportImageAsync` exports the map to Bitmap format which can be saved in any image file format.

![Take Screenshot App](take-screenshot.png)

## How to use the sample
Tap the camera icon the upper right to take a screenshot of the current view on the `MapView`.

## How it works
1. Call `exportImageAsync` on the `MapView` and set it to a `ListenableFuture<Bitmap>`.
1. On done, call `get()` on the `ListenableFuture<Bitmap>` and save it to the device.

## Relevant API
* ArcGISMap
* ExportImageAsync
* ListenableFuture<Bitmap>
* MapView

#### Tags
MapViews, SceneViews and UI