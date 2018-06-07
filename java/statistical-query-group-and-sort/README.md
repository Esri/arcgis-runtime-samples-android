# Statistical Query Group and Sort
Query a `ServiceFeatureTable` to get statistics for one or more specified fields. The sample queries a service feature table of US states to get the selected statistics. The results can be grouped and sorted using one or several fields in the table.

![Statistical Query Group and Sort App](statistical-query-group-and-sort.png)

## How to use the sample
1. Select a combination of fields and statistic types to include in the query. Use the 'Add' button to add selections to the list.
1. (Optional) Choose one or more fields to group the results on by checking boxes next to field names in the 'Group Field(s)' list. Grouping by SUB_REGION, for example, will show results grouped (summarized) for each region ('Pacific', 'Mountain', etc).
1. (Optional) Choose one or more fields to order results by. Use the '>>' and '<<' to add or remove fields to sort by. NOTE: Only those fields selected for grouping are valid choices for ordering results.
1. Execute the query by hitting the 'Get Statistics' button. Results will be displayed in an expandable list view in a new activity. Results will be grouped and sorted according to the chosen fields.

## How it works
1. Create `StatisticsQueryParameters` with a list of `StatisticDefinition`s.
1. Add a list of fields (as strings) to group by with `.getGroupByFieldNames().add(...)`.
1. Add a list of fields (as strings) to order by with `getOrderByFields().add(...)`.
1. Get the statistics query result from the `ServiceFeatureTable` with `.queryStatisticsAsync(...)` on the `StatisticsQueryParameters`.

## Relevant API
* QueryParameters
* StatisticDefinition
* StatisticType
* StatisticsQueryParameters
* StatisticsQueryResult

#### Tags
Search and Query