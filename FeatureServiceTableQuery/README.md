# Feature Service Table Query

![feature service feature table query app](feature-service-table-query.png)

This sample shows how you can create a ```GeodatabaseFeatureServiceTable```, initialize it from a feature service, create a ```FeatureLayer``` based on that table, and also query the table. Features found by the query are then selected in the map.

To perform a query, select values in the two spinners, and then press the **OK** button.

## Features

- ```GeodatabaseFeatureServiceTable```
- ```FeatureLayer```, including ```queryFeatures```, ```clearSelection```, and ```selectFeature``` methods
- ```CodedValueDomain```

## Sample Design

In this app, the ```MainActivity``` contains a ```MapView``` that contains the ```FeatureLayer```. Spinners are populated with values from two fields in the table. The values from the spinners are used to construct a where clause which is then used in the ```queryFeatures``` method call. 

The results of the query are reported to the user in the query callback, and the features in the query result are used to select features in the ```FeatureLayer``` in the ```MapView```.