# Identify raster cell

Get the cell value of a local raster at the tapped location and display the result in a callout.

![Image of identify raster cell](identify-raster-cell.png)

## Use case

You may want to identify a raster layer to get its exact cell value in the case the approximate value conveyed by its symbology is not sufficient. The information available for the raster cell depends on the type of raster layer being identified. For example, a 3-band satellite or aerial image might provide 8-bit RGB values, whereas a digital elevation model (DEM) would provide floating point z values. By identifying a raster cell of a DEM, you can retrieve the precise elevation of a location.

## How to use the sample

Tap or double tap drag an area of the raster to identify it and see the raster cell attributes information displayed in a callout.

## How it works

1. Create a `DefaultMapViewOnTouchListener` on the `MapView`.
2. On tap or double tap drag:
   * Call `identifyLayerAsync(...)` passing in the raster layer, screen point, tolerance, and maximum number of results per layer.
   * Add a done loading listener for the result of the identify and then get the `GeoElement` from the layer result and get any `RasterCell`s from them.
   * Create a callout at the calculated map point and populate the callout content with text from the `RasterCell` attributes.
   * Show the callout.

## Relevant API

* GeoView.identifyLayerAsync(...)
* IdentifyLayerResult
* RasterCell
* RasterCell.attributes
* RasterLayer
* 
## Offline Data

1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=b5f977c78ec74b3a8857ca86d1d9b318).
2. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
3. Push the data into the scoped storage of the sample app:
`adb push SA_EVI_8Day_03May20/. /Android/data/com.esri.arcgisruntime.sample.identifyrastercell/files`

## About the data

The data shown is an NDVI classification derived from MODIS imagery between 27 Apr 2020 and 4 May 2020. It comes from the [NASA Worldview application](https://worldview.earthdata.nasa.gov/). In a normalized difference vegetation index, or [NDVI](https://en.wikipedia.org/wiki/Normalized_difference_vegetation_index), values range between -1 and +1 with the positive end of the spectrum showing green vegetation.

## Tags

band, cell, cell value, continuous, discrete, identify, pixel, pixel value, raster
