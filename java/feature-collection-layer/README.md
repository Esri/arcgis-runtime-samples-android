# Feature Collection Layer
Creates a new Feature Collection with a Point, Polyline, and Polygon Feature Collection Table. The Collection is then displayed on the ArcGISMap as a Feature Collection Layer.

![Feature Collection Layer App](feature-collection-layer.png)

## How it works
A `FeatureCollectionLayer` is created from a `FeatureCollection` and is added to the map's operational layers. 
Then, a point `FeatureCollectionTable`, a polyline `FeatureCollectionTable`, and a polygon `FeatureCollectionTable` are created, and their schemas and renderers are defined. 
Next, features are added to each table, and each table is added to the `FeatureCollection`. 
In this case, hardcoded features are added to the tables for display on the map. 
However, a common use case is to read a CSV or some other data source, and to populate the table with the attributes and geometry provided in the external data source.

## Relevant API
* FeatureCollection
* FeatureCollectionLayer
* FeatureCollectionTable
* Feature
* Field
* SimpleFillSymbol
* SimpleLineSymbol
* SimpleMarkerSymbol
* SimpleRenderer

### Tags
Layers         

