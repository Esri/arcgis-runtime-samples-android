# Show labels on layer in 3D

Display custom labels in a 3D scene.

![Show labels on layer in 3D](show-labels-on-layer-3d.png)

## Use case

Labeling features is useful to visually display information or attributes on a scene. For example, city officials or maintenance crews may want to show installation dates of features of a gas network.

## How to use the sample

Pan and zoom to explore the scene. Notice the labels showing installation dates of features in the 3D gas network.

## How it works

1. Create an `Scene` from a `PortalItem`.
2. Add the scene to an `SceneView` and load it.
3. After loading is complete, obtain the `FeatureLayer` from one of the `GroupLayer`s in the scene's `operationalLayers`.
4. Create an `AGSTextSymbol` to use for displaying the label text.
5. Create an `AGSLabelDefinition` using an  `AGSArcadeLabelExpression`.
6. Add the definition to the feature layer's `labelDefinitions` array.
7. Set the feature layer's `labelsEnabled` property to `true`.

## Relevant API

* ArcadeLabelExpression
* FeatureLayer
* LabelDefinition
* Scene
* SceneView
* TextSymbol

## About the data

This sample shows a [New York City infrastructure](https://www.arcgis.com/home/item.html?id=850dfee7d30f4d9da0ebca34a533c169) scene hosted on ArcGIS Online.

## Tags

3D, arcade, attribute, buildings, label, model, scene, symbol, text, URL, visualization