# Feature Layer Show Attributes
Return all loaded features from a query to show all attributes.

![Feature Layer Show Attributes App](feature-layer-show-attributes.png)

## How to use the sample
Tap on a feature to see its attributes in a callout.

## How it works 
The sample creates an instance of `ServiceFeatureTable` and query's features based on user selected feature whereby we explicitly request all attributes to be returned with `ServiceFeatureTable.QueryFeatureFields.LOAD_ALL`. `ArcGISFeature` objects are loadable so we get the `FeatureQueryResult` and iterate through the results creating a `Map` of all available attributes as name value pairs.  We use those name value pairs in a `Callout` for display.  

## Relevant API
* Feature
* FeatureLayer
* FeatureQueryResult
* ServiceFeatureTable

#### Tags
Search and Query