# Map Reference Scale

Set the map's reference scale and which feature layers should scale their symbols and text to it.

![Map Reference Scale App](map-reference-scale.png)

## Use cases (this is a placeholder title and subject to change based on team decision)
Map annotations are typically only relevant at certain scales. For instance, annotations to a construction site are only relevant to that construction site's scale. So, when the map is zoomed out that information shouldn't scale with the `MapView`, but should instead remain scaled with the `ArcGISMap`. 

## How to use the sample
* Use the slider to set the map's reference scale
* Use the menu checkboxes to set which feature layers should scale according to the reference scale

## How it works
Get and set:

* the reference scale property on the `ArcGISMap`.
* the scale symbols property on each individual `FeatureLayer`.

## Relevant API
* ArcGISMap
* FeatureLayer

#### Tags
Maps & Scenes