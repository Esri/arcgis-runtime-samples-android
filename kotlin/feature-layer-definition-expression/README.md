# Feature layer definition expression

Limit the features displayed on a map with a definition expression.

![Image of feature layer definition expression](feature-layer-definition-expression.png)

## Use case

Set a definition expression to filter out the features to be displayed. You might filter a dataset of tree quality selecting for only those trees which require maintenance or are damaged.

## How to use the sample

Tap the 'Apply Expression' button to limit the features requested from the feature layer to those specified by the SQL query definition expression. Tap the 'Reset' button to remove the definition expression on the feature layer, which returns all the records.

## How it works

1. Create a `ServiceFeatureTable` from a URL.
2. Create a `FeatureLayer from the service feature table.
3. Set the limit of the features on your feature layer using the `setDefinitionExpression()`.

## Relevant API

* FeatureLayer
* FeatureLayer.setDefinitionExpression
* ServiceFeatureTable

## About the data

This map displays point features related to crime incidents that have been reported by city residents.

## Tags

definition expression, filter, limit data, query, restrict data, SQL, where clause
