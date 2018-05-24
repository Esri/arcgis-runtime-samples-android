# Show Labels on Layer
Demonstrates how to show labels on a feature layer.

![Show Labels on Layer App](show-labels-on-layer.png)

## How to use the sample
Simply load the sample. Notice the way labels change with different zoom levels.

## How it works

1. Create a `FeatureLayer` with a `ServiceFeatureTable` using an online feature service.
1. Create a `TextSymbol` to use for displaying the label text.
    - Create a JSON string for the label definition.
    - Set the "LabelExpressionInfo.expression" key to express what the text the label should display. You can use fields of the feature by using `$feature.field_name` in the expression.
    - To use the text symbol, set the "symbol" key to the symbol's JSON representation using `textSymbol.toJson()`.
1. Create a label definition from the JSON using `LabelDefinition.fromJson(json)`.
1. Add the definition to the feature layer with `featureLayer.getLabelDefinitions().add(labelDefinition)`.
1. Lastly, enable labels on the layer using `featureLayer.setLabelsEnabled()`.

## Relevant API
* FeatureLayer
* LabelDefinition
* TextSymbol

#### Tags
Visualization