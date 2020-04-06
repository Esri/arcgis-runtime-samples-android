# Colormap renderer

Apply a colormap renderer to a raster.

![Image of colormap renderer](colormap-renderer.png)

## Use case

A colormap renderer transforms pixel values in a raster to display raster data based on specific colors, aiding in visual analysis of the data. For example, a forestry commission may want to quickly visualize areas above and below the tree-line line occurring at a known elevation on a raster containing elevation values. They could overlay a transparent colormap set to color those areas below the tree-line elevation green, and those above white.

## How to use the sample

Pan and zoom to explore the effect of the colormap applied to the raster.

## How it works

1. Create a `Raster` from a raster file.
2. Create a `RasterLayer` from the raster.
3. Create a `List<Integer>` representing colors. Colors at the beginning of the list replace the darkest values in the raster and colors at the end of the list replaced the brightest values of the raster.
4. Create a `ColormapRenderer` with the color list: `ColormapRenderer(colors)`, and apply it to the raster layer with `rasterLayer.setRasterRenderer(colormapRenderer)`.


## Relevant API

* ColormapRenderer
* Raster
* RasterLayer

## Offline Data

1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=95392f99970d4a71bd25951beb34a508).
2. Extract the contents of the downloaded zip file to disk.
3. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
4. Push the data into the scoped storage of the sample app:
`adb push shasta /Android/data/com.esri.arcgisruntime.sample.colormaprenderer/files/shasta`

## About the data

The raster used in this sample shows an area in the south of the Shasta-Trinity National Forest, California.

## Tags

colormap, data, raster, renderer, visualization
