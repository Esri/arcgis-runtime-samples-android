# Set annotation sublayer visibility

Use annotation sublayers to control the scales at which different annotation is visible.

![Set annotation sublayer visibility App](set-annotation-sublayer-visibility.png)

## Use case

Annotation, which differs from labels by having a fixed place and size, is typically only relevant at particular scales. Annotation sublayers allow for finer control of annotation by allowing properties (like visibility in the map and legend) to be set and others to be read (like name) on subtypes of an annotation layer.

An annotation dataset which marks valves as "Opened" or "Closed", might be set to display the "Closed" valves over a broader range of scales than the "Opened" valves, if the "Closed" data is considered more relevant by the map's author. Regardless, the user can be given a manual option to set visibility of annotation sublayers on and off, if required.

## How to use the sample

Start the sample and take note of the visibility of the annotation. Zoom in and out to see the annotation turn on and off based on scale ranges set on the data. The scale ranges were set by the map's author using ArcGIS Pro:

* The "Open" annotation sublayer has its minimum scale set to 1:500 and its maximum scale set to 1:1500.
* The "Closed" annotation sublayer has its minimum scale set to 1:500 and its maximum scale set to 1:5000.

Use the checkboxes to manually turn the "Open" and "Closed" annotation sublayers on and off.

## How it works

1. Load the `MobileMapPackage`.
2. Populate checkbox text with the `AnnotationSublayer` names.
3. Wire up the checkboxes to toggle the annotation sublayer's visibility.
4. Add a listener for changes in map view navigation and update the current scale UI element at the bottom of the screen on navigation.
 
## Relevant API

* AnnotationLayer
* AnnotationSublayer
* LayerContent

## Offline Data

1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=b87307dcfb26411eb2e92e1627cb615b).
2. Extract the contents of the downloaded zip file to disk.
3. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
4. Execute the following command:
`adb push GasDeviceAnno.mmpk /sdcard/ArcGIS/Samples/MapPackage/GasDeviceAnno.mmpk`


Link | Local Location
---------|-------|
|[Gas Device Anno Mobile Map Package](https://arcgisruntime.maps.arcgis.com/home/item.html?id=b87307dcfb26411eb2e92e1627cb615b)| `<sdcard>`/ArcGIS/Samples/MapPackage/GasDeviceAnno.mmpk|

#### Tags
Visualization
Annotation
utilities
text
scale
