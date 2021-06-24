# Show labels on layer

Display custom labels on a feature layer.

![Image of show labels on layer](show-labels-on-layer.png)

## Use case

Labeling features is useful to visually display a key piece of information or attribute of a feature on a map. For example, you may want to label rivers or streets with their names. 

## How to use the sample

Pan and zoom around the United States. Labels for US highways will be shown. 

## How it works

1. Create a `ServiceFeatureTable` using a feature service URL.
2. Create a `FeatureLayer` from the service feature table.
3. Create a `TextSymbol` to use for displaying the label text.
4. Create an `ArcadeLabelExpression` for the label definition.
    * You can use fields of the feature by using `$feature.field_name` in the expression.
5. Create a new `LabelDefinition` from the arcade label expression and text symbol.
6. Add the definition to the feature layer with `featureLayer.labelDefinitions.add(labelDefinition)` .
7. Lastly, enable labels on the layer using `featureLayer.isLabelsEnabled`.

## Relevant API

* FeatureLayer
* LabelDefinition
* TextSymbol

## About the data

This sample uses the [US Highways](http://sampleserver6.arcgisonline.com/arcgis/rest/services/USA/MapServer/1) feature service hosted on ArcGIS Online.

## Additional information

Help regarding the Arcade label expression script for defining a label definition can be found on the [ArcGIS Developers](https://developers.arcgis.com/arcade/) site.

## Tags

arcade, attribute, deconfliction, label, labeling, string, symbol, text, visualization
