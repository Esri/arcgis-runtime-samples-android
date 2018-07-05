# Map Image Layer Tables
Get a non-spatial table from an ArcGIS map image layer and query it to get related features in another table.

The non-spatial tables contained by a map service may contain additional information about sublayer features. Such information can be accessed by traversing table relationships defined in the service.

![Map Image Layer Tables App](map-image-layer-tables.png)

## How to use the sample
1. Launch the sample, the map displays at the extent of the "Service Request" layer.
1. The `ListView` is populated with service request comment records that have a valid (non-null) comment.
1. Select one of the service request comments in the list box to see the related service request feature selected in the map.

## How it works
The `ArcGISMapImageLayer` in the map uses the `ServiceRequests` map service as its data source. This service is hosted by ArcGIS Server, and is composed of one sublayer (`ServiceRequests`) and one non-spatial table (`ServiceRequestComments`). The non-spatial table is accessed using the Tables property of `ArcGISMapImageLayer`. The table can be queried like any other `FeatureTable`, including queries for related features. The comments table is queried for records where the `[comments]` field is not `null` and the result is used to populate the list box (should be four records or so). When a selection is made in the list box, the service request layer is queried for features related to the selected comment. The feature(s) selected by the query are then selected in the service request layer.

## Relevant API
* ServiceFeatureTable
* ArcGISMapImageLayer
* LoadTablesAndLayersAsync
* Tables
* ArcGISMapImageSublayer

#### Tags
Search and Query
