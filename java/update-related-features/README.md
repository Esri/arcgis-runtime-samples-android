# Update Related Features
A `MapView` loaded with two related `FeatureLayer`'s, national parks and preserves.  The relationships between the layers is defined in the service. When you tap on a national park `Feature` the app identifies the feature and performs a related table query then shows the annual visitors amount for the preserve. You can update the visitor amount by tapping the drop-down in the `Callout` and selecting a different amount. The app will apply the update on the server.  The color coding of the Preserves features are outlined in the Legend below. The color will change to correlate with the updated values from the app.

![Update Related Features App](update-related-features.png)
![Map Legend](legend.png)

## How to use the sample
Tap on a feature to select it. A callout will appear with a spinner which can be used to select new data. Selecting new data will also update the server.

## How it works
When you tap on the map the app identifies if a feature is selected and queries for related features on its `FeatureTable`.  Results are shown in an editable `Callout` where you can update the visitor amount by selecting a new value from the drop-down list.  

Updates can be applied to the server using `ServiceFeatureTable.updateFeatureAsync(...)` and `ServiceFeatureTable.applyEditsAsync()`

## Relevant API
* ArcGISFeature
* ServiceFeatureTable
* RelatedQueryParameters
* RelatedFeatureQueryResult

#### Tags
Edit and Manage Data