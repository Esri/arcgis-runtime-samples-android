# Time based query

Query data using a time extent. 

![Image of time based query](time-based-query.png)

## Use case

This workflow can be used to return records that are between a specified start and end date. For example, records of Canada goose sightings over time could be queried to only show sightings during the winter migration time period.

## How to use the sample

Run the sample, and a subset of records will be displayed on the map.

## How it works

1. Create an instance of `ServiceFeatureTable` by passing a URL to the REST endpoint of a time-enabled service. Time-enabled services will have TimeInfo defined in the service description. This information is specified in ArcMap or ArcGIS Pro prior to publishing the service.
2. Set the feature request mode of the service feature table to manual cache mode, so you can control how and when the feature table is populated with data.
3. Create a `FeatureLayer` from the table and add it to the map's operational layers.
4. Create a `TimeExtent` object by specifying start and end date/time objects. Then create an instance of `QueryParameters` and set its time extent with `queryParameters.setTimeExtent(timeExtent)`.
5. Finally, use `populateFromServiceAsync()` on the service feature table, passing in the query parameters.
6. The feature table is populated with data that matches the provided query.

## Relevant API

* QueryParameters
* ServiceFeatureTable
* TimeExtent

## About the data

This sample uses Atlantic hurricane data from the year 2000. The data is from the National Hurricane Center (NOAA / National Weather Service).

## Tags

query, time, time extent
