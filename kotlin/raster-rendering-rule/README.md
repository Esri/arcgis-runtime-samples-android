# Raster Rendering Rule

This sample demonstrates how to create an `ImageServiceRaster`, fetch the `RenderingRules` from the service info, and use a `RenderingRule` to create an `ImageServiceRaster` and add it to a raster layer. 

![Raster Rendering Rule App](raster-rendering-rule.png)

## Features

* ImageServiceRaster
* RenderingRule
* RasterLayer

## Developer Pattern

 When the sample starts, an `ImageServiceRaster` is created and added to a `RasterLayer`.  The `RasterLayer` is then added to the map as an operational layer.  Connect to the `loadStatusChanged` signal for the `ImageServiceRaster`. Once the `ImageServiceRaster` is loaded, the `RenderingRuleInfos` are fetched. Iterate over each item in the `RenderingRuleInfos` to get the rendering rule name and populate `List` using the names. This becomes the list for the Android `Spinner`. When an item from the spinner is selected, the `RenderingRuleInfo` for the selected index is fetched from the service info. A `RenderingRule` object is created using the `RenderingRuleInfo` and applied to a newly created `ImageServiceRaster`. The `ImageServiceRaster` is then added to the `RasterLayer`.  

```kotlin
fun applyRenderingRule(imageServiceRaster: ImageServiceRaster, index: Int){
    // clear all rasters
    map.operationalLayers.clear()
    // get the rendering rule info at the selected index
    val renderRuleInfo = imageServiceRaster.serviceInfo.renderingRuleInfos[index]
    // create a rendering rule object using the rendering rule info
    val renderingRule = RenderingRule(renderRuleInfo)
    // create a new image service raster
    val appliedImageServiceRaster = ImageServiceRaster(resources.getString(R.string.image_service_url))
    // apply the rendering rule
    appliedImageServiceRaster.renderingRule = renderingRule
    // create a raster layer using the image service raster
    val rasterLayer = RasterLayer(appliedImageServiceRaster)
    // add the raster layer to the map
    map.operationalLayers.add(rasterLayer)
}
```