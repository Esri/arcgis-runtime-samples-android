# Configure subnetwork trace

Get a server-defined trace configuration for a given tier and modify its traversability scope, add new condition barriers and control what is included in the subnetwork trace result.

![Image of configure subnetwork trace](configure-subnetwork-trace.png)

## Use case

While some traces are built from an ad-hoc group of parameters, many are based on a variation of the trace configuration taken from the subnetwork definition. For example, an electrical trace will be based on the trace configuration of the subnetwork, but may add additional clauses to constrain the trace along a single phase. Similarly, a trace in a gas or electric design application may include features with a status of "In Design" that are normally excluded from trace results.

## How to use the sample

Sample loads with a server-defined trace configuration from a tier. Check or uncheck which options to include in the trace - such as containers or barriers. Use the selection boxes to define a new condition network attribute comparison. Click 'Trace' to run a subnetwork trace with this modified configuration from a default starting location.
Example barrier conditions for the default dataset:
* 'Transformer Load' EQUAL '15'
* 'Phases Current' DOES_NOT_INCLUDE_THE_VALUES 'A'
* 'Generation KW' LESS_THAN '50'

## How it works

1. Populate the traversability scope list with the enum values from `UtilityTraversabilityScope`.
2. Create and load a `UtilityNetwork` with a feature service URL, then get an asset type and a tier by their names.
3. Populate the choice list for the comparison source with the non-system defined `UtilityNetworkDefinition.networkAttributes`.  Populate the choice list for the comparison operator with the enum values from `UtilityAttributeComparisonOperator`.
4. Create a `UtilityElement` from this asset type to use as the starting location for the trace.
5. Update the selected barrier expression and the checked options in the UI using this tier's `UtilityTraceConfiguration`.
6. When 'Network Attribute' is selected, check whether it has a `CodedValueDomain`, and if so, populate the choice list for the comparison value with its `CodedValues`.  Otherwise, display a free-form textbox for entering an attribute value.
7. When 'Add' is clicked, create a new `UtilityNetworkAttributeComparison` using the selected comparison source, operator, and selected or typed value. Use the selected source's `UtilityNetworkAttribute.DataType` to convert the comparison value to the correct data type.
8. If the utility trace configuration's `UtilityTraversability.barriers` is not empty, create a `UtilityTraceOrCondition` with the existing barriers and the new comparison from step 7.
9. When 'Trace' is clicked, create `UtilityTraceParameters` passing in `UtilityTraceType.SUBNETWORK` and the default starting location.  Set its `UtilityTraceConfiguration` with the modified options, selections, and expression; then run a `UtilityNetwork.traceAsync(...)`.
10. When `Reset` is clicked, set the trace configurations expression back to its original value.
11. Display the count of returned `UtilityElementTraceResult.elements`.

## Relevant API

* CodedValueDomain
* UtilityAssetType
* UtilityAttributeComparisonOperator
* UtilityCategory
* UtilityCategoryComparison
* UtilityCategoryComparisonOperator
* UtilityDomainNetwork
* UtilityElement
* UtilityElementTraceResult
* UtilityNetwork
* UtilityNetworkAttribute
* UtilityNetworkAttributeComparison
* UtilityNetworkDefinition
* UtilityTerminal
* UtilityTier
* UtilityTraceAndCondition
* UtilityTraceConfiguration
* UtilityTraceOrCondition
* UtilityTraceParameters
* UtilityTraceResult
* UtilityTraceType
* UtilityTraversability

## About the data

The [Naperville electrical](https://sampleserver7.arcgisonline.com/arcgis/rest/services/UtilityNetwork/NapervilleElectric/FeatureServer) network feature service, hosted on ArcGIS Online, contains a utility network used to run the subnetwork-based trace shown in this sample.

## Tags

category comparison, condition barriers, network analysis, network attribute comparison, subnetwork trace, trace configuration, traversability, utility network, validate consistency
