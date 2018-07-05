# Service Feature Table (Manual Cache)
Use a feature service in manual cache mode. This mode does not fetch features until a call to populate from service is called. From this point onwards, no additional queries are sent to the server to retrieve features. If you know what features you need ahead of time, this is the best mode.

![Service FeatureTable Manual Cache App](service-feature-table-manualcache.png)

## How to use the sample
Simply run the app.

## How it works
Set the `ServiceFeatureTable.FeatureRequestMode` to `MANUAL_CACHE` on an service feature table before it is loaded, then call populate from service using a query which defines the required features.

## Relevant API
* FeatureLayer
* ServiceFeatureTable
* ServiceFeatureTable.setFeatureRequestMode(...)

#### Tags
Edit and Manage Data
