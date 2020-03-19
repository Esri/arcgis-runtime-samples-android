# Map reference scale

Set the map's reference scale and which feature layers should honor the reference scale.

![Image of map reference scale](map-reference-scale.png)

## Use case

Setting a reference scale on an `ArcGISMap` fixes the size of symbols and text to the desired height and width at that scale. As you zoom in and out, symbols and text will increase or decrease in size accordingly. When no reference scale is set, symbol and text sizes remain the same size relative to the `MapView`.

Map annotations are typically only relevant at certain scales. For instance, annotations to a map showing a construction site are only relevant at that construction site's scale. So, when the map is zoomed out that information shouldn't scale with the `MapView`, but should instead remain scaled with the `ArcGISMap`. 

## How to use the sample

* Use the drop down at the top to set the map's reference scale (1:500,000 1:250,000 1:100,000 1:50,000).
* Tap the button to set the map scale to the reference scale.
* Use the checkboxes in the overflow menu to set which feature layers should honor the reference scale.

## How it works

1. Get and set the reference scale property on the `ArcGISMap` object.
2. If checked, get and set the scale symbols property on each individual `FeatureLayer` object.

## Relevant API

* ArcGISMap
* FeatureLayer

## Additional information

The map reference scale should normally be set by the map's author and not exposed to the end user like it is in this sample. 

## Tags

map, reference scale, scene