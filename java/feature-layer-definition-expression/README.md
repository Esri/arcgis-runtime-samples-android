# Feature layer definition expression
### Category: Search and Query
This sample demonstrates how you can limit which features to display on the map. Use the buttons in the bottom toolbar to apply or reset definition expression.

![Feature Layer Definition Expression App](feature-layer-definition-expression.png)

## How it works

You can achieve this by setting the definition expression property on a feature layer. It is the syntax of a SQL where clause by which to limit which features are displayed on the map.

## Developer Pattern
```java
private void applyDefinitionExpression() {
    // apply a definition expression on the feature layer
    // if this is called before the layer is loaded, it will be applied to the loaded layer
    mFeatureLayer.setDefinitionExpression("req_Type = 'Tree Maintenance or Damage'");
}

private void resetDefinitionExpression() {
    // set the definition expression to nothing (empty string, null also works)
    mFeatureLayer.setDefinitionExpression("");
}
```
