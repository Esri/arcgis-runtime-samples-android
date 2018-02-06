# List Related Features

![List Related Features App](list-related-features.png)

The List Related Features app has a `MapView` loaded with a web map `FeatureLayer`. When you tap on a `Feature` you get a returned list of related features in an Android `BottomSheet`. The `FeatureLayer` has more than one relationship, the `ListView` results for each relationship.

## Features

* ArcGISFeature
* ArcGISFeatureLayerInfo
* ArcGISFeatureTable
* RelatedFeatureQueryResult
* RelatedQueryParameters

## Developer Pattern
The `FeatureTable` from a selected `Feature` is used as a parameter on a `FeatureTable.queryRelatedFeaturesAsync` method.  The results returned represent related `Feature`s of which you can obtain the `ArcGISFeatureLayerInfo` to get the field used to filter the attribute values and notify the `ArrayAdapter` of the changes.

```java
ArcGISFeatureTable selectedTable = (ArcGISFeatureTable)feature.getFeatureTable();

final ListenableFuture<List<RelatedFeatureQueryResult>> relatedFeatureQueryResultFuture = selectedTable.queryRelatedFeaturesAsync(arcGISFeature);
relatedFeatureQueryResultFuture.addDoneListener(new Runnable() {
    @Override
    public void run() {
        try {
            List<RelatedFeatureQueryResult> relatedFeatureQueryResultList = relatedFeatureQueryResultFuture.get();
            // iterate over returned RelatedFeatureQueryResults
            for(RelatedFeatureQueryResult relatedQueryResult : relatedFeatureQueryResultList){
                // iterate over Features returned
                for (Feature relatedFeature : relatedQueryResult) {
                    // Get the Display field to use as filter on related attributes
                    ArcGISFeature agsFeature = (ArcGISFeature) relatedFeature;
                    String displayFieldName = agsFeature.getFeatureTable().getLayerInfo().getDisplayFieldName();
                    String displayFieldValue = agsFeature.getAttributes().get(displayFieldName).toString();
    
                    mRelatedValues.add(displayFieldValue);
                    // notify ListAdapter content has changed
                    mArrayAdapter.notifyDataSetChanged();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception occurred: " + e.getMessage());
        }
    }
});
```