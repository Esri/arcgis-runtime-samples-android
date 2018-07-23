# Service Feature Table (Cache)
Use a feature service with a service feature table in on-interaction-cache mode (which is the default mode for service feature tables). 

![Service FeatureTable Cache App](service-feature-table-cache.png)

## How to use the sample
Simply run the app.

## How it works
Set the `ServiceFeatureTable.FeatureRequestMode` property of the service feature table to `ON_INTERACTION_CACHE` before the table is loaded. The mode cannot be changed once the table has been loaded.

## Relevant API
* FeatureLayer
* ServiceFeatureTable
* ServiceFeatureTable.setFeatureRequestMode(...)

#### Tags
Edit and Manage Data
