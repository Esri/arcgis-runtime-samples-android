# Feature layer update attributes

Update feature attributes in an online feature service.    

![Image of update attributes feature service](feature-layer-update-attributes.png)

## Use case

Online feature services can be updated with new data. This is useful for updating existing data in real time while working in the field.

## How to use the sample

To change the feature's damage property, tap the feature to select it, and update the damage type using the drop down.

## How it works

1. Create a `ServiceFeatureTable` object from a URL.
    * When the table loads, you can get the domain to determine which options to present in your UI.
2. Create a `FeatureLayer` object from the `ServiceFeatureTable`.
3. Select features from the `FeatureLayer`.
4. To update the feature's attribute, first load it, then use `setAttributeValue`.
5. Update the table with `updateFeatureAsync`.
6. After a change, apply the changes on the server using `applyEditsAsync`.

## Relevant API

* ArcGISFeature
* FeatureLayer
* ServiceFeatureTable

## Tags

attribute, coded value, coded value domain, domain, editing, value
