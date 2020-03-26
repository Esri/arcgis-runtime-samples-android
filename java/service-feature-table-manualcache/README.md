# Service feature table (manual cache)

Display a feature layer from a service using the **manual cache** feature request mode.

![Image of service feature table manual cache](service-feature-table-manualcache.png)

## Use case

Service feature tables support three request modes, which define how features are requested from the service and stored in the local table. The feature request modes have different performance characteristics. Use **manual cache** in scenarios where you want to explicitly control requests for features. See [Table performance concepts](https://developers.arcgis.com/android/latest/guide/layers.htm#ESRI_SECTION1_40F10593308A4718971C9A8F5FB9EC7D) in the *ArcGIS Runtime SDK for Android* guide to learn more.

## How to use the sample

Run the sample and pan and zoom around the map. Features will be requested and displayed automatically.

## How it works

1. Set the `ServiceFeatureTable.setFeatureRequestMode(...)` property of the service feature table to `MANUAL_CACHE` before the table is loaded.
2. Load the table with `serviceFeatureTable.loadAsync()`.
3. Call `populateFromServiceAsync()` on the table to request features.

## Relevant API

* FeatureLayer
* FeatureRequestMode.MANUAL_CACHE
* ServiceFeatureTable
* ServiceFeatureTable.populateFromServiceAsync
* ServiceFeatureTable.setFeatureRequestMode

## About the data

The sample opens with a tree maintenance feature layer centered over San Francisco.

## Additional information

In **manual cache** mode, features are never automatically populated from the service. All features are loaded manually using calls to `populateFromServiceAsync`.

## Tags

cache, feature request mode, performance
