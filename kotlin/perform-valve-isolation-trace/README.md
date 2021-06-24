# Perform valve isolation trace

Run a filtered trace to locate operable features that will isolate an area from the flow of network resources.

![Image of a utility network with an isolation trace applied to it](perform-valve-isolation-trace.png)

## Use case

Determine the set of operable features required to stop a network's resource, effectively isolating an area of the network. For example, you can choose to return only accessible and operable valves: ones that are not paved over or rusted shut.

## How to use the sample

Create and set the configuration's filter barriers by selecting a category. Toggle 'Include isolated features' as required. Tap 'Trace' to run a subnetwork-based isolation trace.

## How it works

1. Create a `MapView`.
2. Create and load a `ServiceGeodatabase` with a feature service URL and get tables with their layer IDs.
3. Create an `ArcGISMap` object that contains `FeatureLayer`(s) created from the service geodatabase's tables.
4. Create and load an `UtilityNetwork` with the same feature service URL and map.
5. Create a default starting location from a given asset type and global ID.
6. Add a `GraphicsOverlay` with an `Graphic` that represents this starting location.
7. Populate the choice list for the filter barriers from the `categories` property of `UtilityNetworkDefinition`.
8. Get a default `UtilityTraceConfiguration` from a given tier in a domain network. Set its `filter` property with an `UtilityTraceFilter` object.
9. When "Trace" is tapped,
    * Create a new `UtilityCategoryComparison` with the selected category and `UtilityCategoryComparisonOperator.EXISTS`.
    * Assign this condition to `traceConfiguration.filter.barriers` from the default configuration from step 7. Update this configuration's `isIncludeIsolatedFeatures` property.
    * Create a `UtilityTraceParameters` with `UtilityTraceType.ISOLATION` and the default starting location from step 4.
    * Set its `UtilityTraceConfiguration` with this configuration and then, run a `UtilityNetwork.traceAsync(traceParameters)`.
9. For every `FeatureLayer` in the map, create `QueryParameters` and add any of the `UtilityElementTraceResult.elements` whose `NetworkSource.name` matches the feature layer's `FeatureTable.tableName`. Use the query parameters to select the features with `featureLayer.selectFeaturesAsync(...)`

## Relevant API

* ServiceGeodatabase
* UtilityCategory
* UtilityCategoryComparison
* UtilityCategoryComparisonOperator
* UtilityElement
* UtilityElementTraceResult
* UtilityNetwork
* UtilityNetworkDefinition
* UtilityTier
* UtilityTraceFilter
* UtilityTraceParameters
* UtilityTraceResult
* UtilityTraceType

## About the data

The [Naperville gas network](https://sampleserver7.arcgisonline.com/server/rest/services/UtilityNetwork/NapervilleGas/FeatureServer) feature service, hosted on ArcGIS Online, contains a utility network used to run the isolation trace shown in this sample.


## Tags

category comparison, condition barriers, isolated features, network analysis, subnetwork trace, trace configuration, trace filter, utility network
