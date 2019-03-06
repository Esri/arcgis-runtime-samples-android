# Add features (Feature Service)

Add new features to an online feature service.

![Add Features (Feature Service)](add-features-feature-service.png)

## Use case

A `FeatureLayer` created using this `SeviceFeatutreTable` that is applied to the `ArcGISMap` will display any new Features automatically.

## How it works

1. Create a service feature table from a URL, `new ServiceFeatureTable("URL")`
1. Create a `FeatureLayer` from the service feature table, `new FeatureLayer(ServiceFeatureTable)`
1. Create a feature with attributes and a location using service feature table, `ServiceFeatureTable.createFeature(attributes, location)`
1. Apply the addition to the service feature table, `ServiceFeatureTable.addFeatureAsync(Feature)`
1. Update the new feature to the server, `ServiceFeatureTable.applyEditsAsync()`

## Relevant API

* ArcGISMap
* Feature
* FeatureEditResult
* FeatureLayer
* MapView
* ServiceFeatureTable

#### Tags

Layers