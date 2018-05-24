# Statistical Query
Query a table to get aggregated statistics back for a specific field.

![Statistical Query App](statistical-query.png)

## How to use the sample
Change the `MapView` extent and use the two checkboxes to influence statistical queries. Trigger a query by hitting the 'Get Statistics' button.

## How it works
1. Create a `ServiceFeatureTable` with a URL to the REST endpoint of a feature service. 
1. Create `StatisticsQueryParameters`, and `StatisticDefinition` objects, and add to the parameters. These definitions define the various statistics that we would like to compute from a given field, including:
    * average
    * count
    * minimum
    * maximum 
    * sum
    * standard deviation
    * variance
1. Execute `queryStatistics` on the `ServiceFeatureTable`. Depending on the state of the two checkboxes, additional parameters are set. The query runs asynchronously, and once complete, this gives access to the `QueryStatisticsResult`, which contains key/value pairs.

## Relevant API
* QueryParameters
* ServiceFeatureTable
* StatisticDefinition
* StatisticRecord
* StatisticType
* StatisticsQueryParameters
* StatisticsQueryResult

#### Tags
Search and Query