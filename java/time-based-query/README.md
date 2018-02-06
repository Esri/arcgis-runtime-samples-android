# Time Based Query

This sample demonstrates how to apply a time-based parameter to a feature layer query

![Time Based Query App](time-based-query.png)

## Features

* QueryParameters
* ServiceFeatureTable
* TimeExtent

## How it works

1. Create an instance of `ServiceFeatureTable` in manual cache mode.
1. Create a feature layer from the table and add it to the map's operational layers.
1. Then create an instance of `QueryParameters` and specify a time extent with a start and end time. 
1. Finally, use `.populateFromServiceAsync(query, clearCache, outFields)` method present on `ServiceFeatureTable` to populate features based on the specified time interval.