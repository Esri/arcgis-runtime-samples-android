# Map Reference Scale
Set the map's reference scale and which feature layers should honor the reference scale.

![Map Reference Scale App](map-reference-scale.png)

## Use case
Setting a reference scale on an `ArcGISMap` fixes the size of symbols and text to the desired height and width at that scale. As you zoom in and out, symbols and text will increase or decrease in size accordingly. When no reference scale is set, symbol and text sizes remain the same size relative to the `MapView`.

Map annotations are typically only relevant at certain scales. For instance, annotations to a map showing a construction site are only relevant at that construction site's scale. So, when the map is zoomed out that information shouldn't scale with the `MapView`, but should instead remain scaled with the `ArcGISMap`. 

## How to use the sample
* Use the spinner at the top to set the map's reference scale.
* Use the menu checkboxes to set which feature layers should scale according to the reference scale.
* Click the button at the bottom to set the map scale (zoom) to the reference scale.

## How it works
* Get and set the reference scale property on the `ArcGISMap`.
* Get and set the scale symbols property on each individual `FeatureLayer`.

## Relevant API
* ArcGISMap
* FeatureLayer

## Additional Information
The map reference scale should normally be set by the map's author and not exposed to the end user like it is in this sample. 

#### Tags
Maps & Scenes