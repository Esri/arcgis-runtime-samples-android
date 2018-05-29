# List Related Features
Shows related features to any feature tapped on.

![List Related Features App](list-related-features.png)

## How to use the sample
The List Related Features app has a `MapView` loaded with a web map `FeatureLayer`. When you tap on a `Feature` you get a returned list of related features in an Android `BottomSheet`. The `FeatureLayer` has more than one relationship, the `ListView` results for each relationship.

## How it works
The `FeatureTable` from a selected `Feature` is used as a parameter on a `FeatureTable.queryRelatedFeaturesAsync` method.  The results returned represent related `Feature`s of which you can obtain the `ArcGISFeatureLayerInfo` to get the field used to filter the attribute values and notify the `ArrayAdapter` of the changes.

## Relevant API
* ArcGISFeature
* ArcGISFeatureLayerInfo
* ArcGISFeatureTable
* RelatedFeatureQueryResult
* RelatedQueryParameters

#### Tags
Search and Query