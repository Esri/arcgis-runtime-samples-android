# Add point scene layer

View a point scene layer from a scene service.

![Image of a point scene layer](add-point-scene-layer.png)

## Use case

Point scene layers can efficiently display large amounts of point features. While point cloud layers can only display simple symbols, point scene layers can display any type of billboard symbol or even 3D models, as long as the location of the symbol can be described by a point. Points are cached and automatically thinned when zoomed out to improve performance.

## How to use the sample

Pan around the scene and zoom in. Notice how many thousands of additional features appear at each successive zoom scale.

## How it works

1. Create a scene.
2. Create an `ArcGISSceneLayer` with the URL to a point scene layer service.
3. Add the layer to the scene's operational layers collection.

## Relevant API

* ArcGISSceneLayer

## About the data

This dataset contains more than 40,000 points representing world airports. Points are retrieved on demand by the scene layer as the user navigates the scene.

## Additional information

Point scene layers can also be retrieved from scene layer packages (.slpk) and mobile scene packages (.mspk).

## Tags

3D, layers, point scene layer
