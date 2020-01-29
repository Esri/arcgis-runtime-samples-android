# Trace utility network

Discover connected features in a utility network using connected, subnetwork, upstream, and downstream traces.

![Trace utility network app](trace-utility-network.png)

## Use case

You can use a trace to visualize and validate the network topology of a utility network for quality assurance. Subnetwork traces are used for validating whether subnetworks, such as circuits or zones, are defined or edited appropriately.

## How to use the sample

Tap on one or more features while 'Add starting locations' or 'Add barriers' is selected. When a junction feature is identified, you may be prompted to select a terminal. When an edge feature is identified, the distance from the tapped location to the beginning of the edge feature will be computed. Select the type of trace using the drop down menu. Click 'Trace' to initiate a trace on the network. Click 'Reset' to clear the trace parameters and start over.

## How it works

1.  Create a `MapView` and add a `DefaultMapViewOnTouchListener` to listen for taps on the `MapView`.
2.  Create and load a `Map` that contains `FeatureLayer`(s) that are part of a utility network.
3.  Create and load a `UtilityNetwork` using the utility network feature service URL and the map created in step 2.
4.  Add a `GraphicsOverlay` with symbology that distinguishes starting locations from barriers.
5.  Identify features on the map and add a `Graphic` that represents its purpose (starting location or barrier) at the tapped location.
6.  Create a `UtilityElement` for the identified feature.
7.  Determine the type of this element using its `NetworkSource.sourceType` property.
8.  If the element is a junction with more than one terminal, display a terminal picker. Then set the junction's `Terminal` property with the selected terminal.
9.  If an edge, set its `FractionAlongLine` property using `GeometryEngine.fractionAlongEdge`.
10. Add this `UtilityElement` to a collection of starting locations or barriers.
11. Create `TraceParameters` with the selected trace type along with the collected starting locations and barriers (if applicable). 
12. Set the `TraceParameters.traceConfiguration` with the utility tier's `TraceConfiguration` property.
13. Run a `UtilityNetwork.traceAsync(...)` with the specified parameters.
14. For every `FeatureLayer` in the map, select the features using the `UtilityElement.objectId` from the filtered list of `UtilityElementTraceResult.elements`.

## Relevant API

* FractionAlong
* UtilityAssetType
* UtilityDomainNetwork
* UtilityElement
* UtilityElementTraceResult
* UtilityNetwork
* UtilityNetworkDefinition
* UtilityNetworkSource
* UtilityTerminal
* UtilityTier
* UtilityTraceConfiguration
* UtilityTraceParameters
* UtilityTraceResult
* UtilityTraceType
* UtilityTraversability

## About the data

The [Naperville electrical network feature service hosted on ArcGIS Online](https://sampleserver7.arcgisonline.com/arcgis/rest/services/UtilityNetwork/NapervilleElectric/FeatureServer) contains a utility network used to run the subnetwork-based trace shown in this sample.

## Tags

condition barriers, downstream trace, network analysis, subnetwork trace, trace configuration, traversability, upstream trace, utility network, validate consistency
