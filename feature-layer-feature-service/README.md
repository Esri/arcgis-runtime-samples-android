#Feature layer feature service
This sample shows how to use a layer from an ArcGIS feature service as a feature layer.

![Feature Layer Feature Service](featurelayer-feature-service.png)

## How it works

There are two classes you need to utilize. First, create a service feature table using the URL to the layer in the feature service you want to use. This is the datasource. Then, create a feature layer and pass in the service feature table you have created. Add the feature layer to a map, then set the map on a  map view and the layer will be displayed using  default modes and properties as defined on the service.
