# Map Load Status
Tell what the map's load status is--obtained from the enum value from a `LoadStatus` class. 

![Map Load Status App](map-load-status.png)

## How to use the sample
The `LoadStatus` is considered loaded when any of the following are true:
* The map has a valid spatial reference
* The map has an an initial viewpoint
* One of the map's predefined layers has been created.

A listener is set up on the map to handle the `LoadStatusChangedEvent`, and the status text is updated when the status changes.

## How it works
The `addLoadStatusChangedListener` method of class `ArcGISMap` listens for `LoadStatusChangedEvent`. To get the load status use method `getNewLoadStatus().name()` on the changed event.

## Relevant API
* ArcGISMap
* LoadStatusChangedListener
* MapView

#### Tags
Maps and Scenes