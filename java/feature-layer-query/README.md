#Feature layer query
This sample demonstrates how to query a feature layer via feature table.

![FeatureLayer Query](featurelayer-query.png)

## How to use this sample
The sample provides a search bar on the top, where you can input the name of a US State. When you search the app performs a query on the feature table and based on the result either highlights the state geometry or provides an error.

## How it works
When you hit the search button, the sample creates an query parameter object and specifies the where clause on it, using the text you provided. It then fires the query on the feature table using the query features method. In the completion block it gets back an feature query result. It iterates through the results and finds the first  feature which it then highlights using select features method on the feature layer.
