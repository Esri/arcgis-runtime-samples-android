# Delete features (Feature Service)

Delete features from an online feature service.

![Delete Features (Feature Service)](delete-features-feature-service.png)

## Use case

A `FeatureLayer` created using this `ServiceFeatureTable` that is applied to the `ArcGISMap` will display existing Features and allow a user to delete individual features by tapping on them.

## How it works

1. Create a new `ServiceFeatureTable` from a URL.
1. Create a `FeatureLayer` from the `ServiceFeatureTable` created in step 1.
1. Detect a tap on a `Feature` and display a `Callout` by invoking `MapView.getCallout()`.
1. Detect a tap on the contents of the `Callout`, asking the user to confirm they wish to delete the `Feature`.
1. Get the `Feature` by querying the `FeatureTable` to obtain the `Feature` instance, invoking `FeatureLayer.getFeatureTable().queryFeaturesAsync()`.
1. Delete the `Feature` from the `ServiceFeatureTable`, `ServiceFeatureTable.deleteFeatureAsync(Feature)`.
1. Update the new feature to the server, `ServiceFeatureTable.applyEditsAsync()`.

## Relevant API

* ArcGISMap
* Feature
* FeatureEditResult
* FeatureLayer
* MapView
* ServiceFeatureTable

#### Tags

Layers