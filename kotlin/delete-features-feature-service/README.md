# Delete features (Feature Service)

Delete features from an online feature service.

![Delete Features (Feature Service)](delete-features-feature-service.png)

## Use case

A `FeatureLayer` created using this `SeviceFeatutreTable` that is applied to the `ArcGISMap` will display existing Features and allow a user to delete individual features by tapping on them.

## How it works

1. Create a service feature table from a URL, `new ServiceFeatureTable("URL")`
2. Create a `FeatureLayer` from the service feature table, `new FeatureLayer(ServiceFeatureTable)`
3. Detect a tap on a `Feature` and display a `Callout` by invoking `MapView.getCallout()`
4. Detect a tap on the contents of the `Callout`, asking the user to confirm they wish to delete the `Feature`
5. Get the `Feature` by querying the `FeatureTable` to obtain the `Feature` instance, invoking `FeatureLayer.getFeatureTable().queryFeaturesAsync()`
6. Delete the `Feature` from the `ServiceFeatureTable`, `ServiceFeatureTable.deleteFeatureAsync(Feature)`
7. Update the new feature to the server, `ServiceFeatureTable.applyEditsAsync()`

## Relevant API

* ArcGISMap
* Feature
* FeatureEditResult
* FeatureLayer
* MapView
* ServiceFeatureTable

#### Tags

Layers