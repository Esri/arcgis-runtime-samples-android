# Delete features (feature service)

Delete features from an online feature service.

![Image of delete features feature service](delete-features-feature-service.png)

## Use case

Sometimes users may want to delete features from an online feature service.

## How to use the sample

Tap on a feature on the map and tap the trash icon to delete it.

## How it works

1. Create a `ServiceFeatureTable` object from a URL.
2. Create a `FeatureLayer` object from the `ServiceFeatureTable`.
3. Select features from the `FeatureTable` via `queryFeaturesAsync()`.
4. Remove the selected features from the `ServiceFeatureTable` using `deleteFeaturesAsync()`.
5. Update the table on the server using `applyEditsAsync()`.

## Relevant API

* Feature
* FeatureLayer
* ServiceFeatureTable

## Tags

deletion, feature, online, Service, table
