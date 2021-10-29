# Show popup

Show a predefined popup from a web map.

![Show popup screenshot](show-popup.png)

## Use case

Many web maps contain predefined popups which are used to display the attributes associated with each feature layer in the map, such as hiking trails, land values, or unemployment rates. You can display text, attachments, images, charts, and web links. Rather than creating new popups to display information, you can easily access and display the predefined popups.

## How to use the sample

Tap on the features to prompt a popup that displays information about the feature.

## How it works

1. Create and load an `ArcGISMap` using a URL.
2. Set the map to a `MapView` and add an `onTouchListener`.
3. Use the `MapView.identifyLayerAsync()` method to identify the top-most feature.
4. Attach the `Popup` from `identifyLayerResultsFuture.get()` to the `PopupViewModel`
5. Present the view controller.

## Relevant API

* IdentifyLayerResult
* ArcGISMap
* PopupViewModel

## About the data

This sample uses a [feature layer](https://sampleserver6.arcgisonline.com/arcgis/rest/services/SF311/FeatureServer/0) that displays reported incidents in San Francisco.

## Tags

feature, feature layer, popup, web map
