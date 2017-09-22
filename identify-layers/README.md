# Identify Layers

This sample demonstrates how to identify features on a map acoss different layers present in the map.

## How to use the sample

Tap on map to get features at that location. The features are returned from different layers in the map.

## Features
* ArcGISMapImageLayer
* FeatureLayer
* Basemap

## How it works


`MapView` has a `identifyLayersAsync(screenLocation, tolerance, returnPopupsOnly, maximumResults)` method that is used in the sample. The method takes a screen location, tolerance, boolean for returning (a pop-up/pop-up and geo-element), and maximum results per layer, which results in a `ListenableFuture<List<IdentifyLayerResult>>`.
	
For a feature layer, the result object might have the `geoElements` property populated, if any elements are present at that location. For a map image layer, the result object might have sublayer result objects populated with geoElements or they might in turn have sublayer result objects.