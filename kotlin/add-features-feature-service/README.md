# Add features (Feature Service)

Add new features to an online feature service.

![Add Features Feature Service App](add-features-feature-service.png)

## Use case

A `FeatureLayer` created using this `ServiceFeatureTable` that is applied to the `ArcGISMap` will display any new Features automatically.

## How to use the sample

Tap on the map to add a feature.

## How it works

1. Create a new `ServiceFeatureTable` from a URL.
1. Create a new `FeatureLayer` from the service feature table.
1. Create a new `Feature` with attributes and a location using the service feature table by calling `createFeature(attributes, location)`.
1. Apply the addition to the service feature table with `addFeatureAsync(Feature)`.
1. Update the new feature to the server by calling `ServiceFeatureTable.applyEditsAsync()`.

## Relevant API

* ArcGISMap
* Feature
* FeatureEditResult
* FeatureLayer
* MapView
* ServiceFeatureTable

#### Tags

Layers
