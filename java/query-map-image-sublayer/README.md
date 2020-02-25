# Query map image sublayer

Find features in a sublayer based on attributes and location.

![Image of query map image sublayer](query-map-image-sublayer.png)

## Use case

Sublayers of an `ArcGISMapImageLayer` may expose a `ServiceFeatureTable` through a `table` property. This allows you to perform the same queries available when working with a table from a `FeatureLayer`: attribute query, spatial query, statistics query, query for related features, etc. An image layer with a sublayer of counties can be queried by population to only show those above a minimum population.

## How to use the sample

Specify a minimum population in the input field (values under 1810000 will produce a selection in all layers) and tap the query button to query the sublayers in the current view extent. After a short time, the results for each sublayer will appear as graphics.

## How it works

1. Create an `ArcGISMapImageLayer` object using the URL of an image service.
2. After loading the layer, get the sublayer you want to query with `(ArcGISMapImageSublayer) layer.getSubLayers().get(index)`.
3. Load the sublayer, and then get its `ServiceFeatureTable` with `sublayer.getTable()`.
4. Create a `QueryParameters` object. You can use `queryParameters.setWhereClause(sqlQueryString)` to query against a table attribute and/or set `queryParameters.setGeometry(extent)` to limit the results to an area of the map.
5. Call `sublayerTable.queryFeaturesAsync(queryParameters)` to get a `FeatureQueryResult` with features matching the query. The result is an iterable of features.

## About the data

The `ArcGISMapImageLayer` in the map uses the "USA" map service as its data source. This service is hosted by ArcGIS Online, and is composed of four sublayers: "states", "counties", "cities", and "highways".
Since the `cities`, `counties`, and `states` tables all have a `POP2000` field, they can all execute a query against that attribute and a map extent.

## Relevant API

* ArcGISMapImageLayer
* ArcGISMapImageSublayer
* QueryParameters
* ServiceFeatureTable

## Tags

search and query
