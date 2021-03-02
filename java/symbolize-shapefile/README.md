# Symbolize shapefile

Display a shapefile with custom symbology.

![Image of symbolize shapefile](symbolize-shapefile.png)

## Use case

Feature layers created from shapefiles do not possess any rendering information, and will be assigned with a default symbology. You can apply custom styling to ensure that the content is visible and usable in the context of a specific map. For example, you could use this to visually differentiate between features originating from two different shapefiles, by applying a blue color to one, and a red color to the other.

## How to use the sample

Pan and zoom around the map to view the symbology renderer set to the feature layer created from the shapefile. 

## How it works

1. Create a `ShapefileFeatureTable`, providing the path to a shapefile.
2. Create a `FeatureLayer` from a `ShapeFileFeatureTable`.
3. Create a `SimpleRenderer` to override the default symbology. The simple renderer takes a symbol and applies that to all features in a layer.
4. Apply the renderer to the `FeatureLayer` by setting the renderer.

## Relevant API

* FeatureLayer
* ShapefileFeatureTable
* SimpleFillSymbol
* SimpleLineSymbol
* SimpleRenderer

## Offline Data

1. Download the data from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=d98b3e5293834c5f852f13c569930caa).
2. Extract the contents of the downloaded zip file to disk.
3. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
4. Push the data into the scoped storage of the sample app:
`adb push Aurora_CO_shp/. /Android/data/com.esri.arcgisruntime.sample.symbolizeshapefile/files`

## About the data

This sample displays a shapefile containing subdivisions in Aurora, CO.

## Additional information

While shapefiles contain no rendering information, other data sources such as Service Feature Tables or Geodatabase Feature Tables can contain such information. As a result, the rendering properties of the other data sources can be pre-defined by the author.

## Tags

package, shape file, shapefile, symbology, visualization
