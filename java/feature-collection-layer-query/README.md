# Feature Collection Layer (Query)
Create a feature collection layer to show a query result from a service feature table. The feature collection is then displayed on a map with a feature collection layer.

![Feature Collection Layer (Query) App](feature-collection-layer-query.png)

## How to use the sample
Simply run the app.

## How it works
A query is performed using the `.queryFeaturesAsync(queryParameters)` method on `FeatureTable`. The result of the query is used to instantiate an `FeatureCollectionTable`. The table is used to instantiate an `FeatureCollection` which is then use to initialize a `FeatureCollectionLayer`. The layer is then displayed on the map by adding it to the operational layers array.

## Relevant API
* FeatureCollection
* FeatureCollectionLayer
* FeatureCollectionTable
* FeatureQueryResult
* QueryParameters
* ServiceFeatureTable

#### Tags
Search and Query
