# Feature layer show attributes

Return all loaded features from a query to show all attributes.

![Feature Layer Show Attributes App](feature-layer-show-attributes.png)

## Use case

Attributes can be used to help describe the objects represented by a feature. A geologist might use an attribute to describe the rock type of polygons representing surface geology. Archaeologists might use attributes to record the stratigraphic layer of finds represented as point features.

## How to use the sample

Tap on a feature to see its attributes in a callout.

## How it works

1. Create an instance of a `ServiceFeatureTable`.
2. Identify selected features with `mMapView.identifyLayerAsync(...)` and pass in the feature layer and tapped location to get all of the attributes for the feature at that location.
3. Get the `IdentifyLayerResult` and iterate through the results to display each attribute in a callout.

## Relevant API

* Feature
* FeatureLayer
* FeatureQueryResult
* ServiceFeatureTable

## Tags

features, layers, query, attributes
