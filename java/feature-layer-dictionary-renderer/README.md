# Feature layer dictionary renderer

Convert features into graphics to show them with mil2525d symbols.

![Image of feature layer dictionary renderer](feature-layer-dictionary-renderer.png)

## Use case

A dictionary renderer uses a style file along with a rule engine to display advanced symbology. This is useful for displaying features using precise military symbology.

## How to use the sample

Pan and zoom around the map. Observe the displayed military symbology on the map.

## How it works

1. Create a `Geodatabase` using `new Geodatabase(geodatabasePath)`.
2. Load the geodatabase asynchronously using `Geodatabase.loadAsync()`.
3. Instantiate a `DictionarySymbolStyle`  using `DictionarySymbolStyle(dictionarySymbolStylePath)`.
4. Load the symbol dictionary asynchronously using `dictionarySymbolStyle.loadAsync()`.
5. Wait for geodatabase to completely load by connecting to `Geodatabase.addDoneLoadingListener()`.
6. For each `GeoDatabaseFeatureTable` in the `GeoDatabase`, create a feature layer with it, then add it to the map using `Map.getOperationalLayers().add(FeatureLayer)`.
7. Create `DictionaryRenderer(dictionarySymbolStyle)` and attach to the feature layer using `FeatureLayer.setRenderer(dictionaryRenderer)`.
8. Set the viewpoint of the map view to the extent of the feature layer using `MapView.setViewpointGeometryAsync(featureLayer.getFullExtent())`.

## Relevant API

* DictionaryRenderer
* DictionarySymbolStyle

## Offline Data

1. Download the data [stylx file](https://www.arcgis.com/home/item.html?id=c78b149a1d52414682c86a5feeb13d30) and [geodatabase](https://www.arcgis.com/home/item.html?id=e0d41b4b409a49a5a7ba11939d8535dc) from ArcGIS Online.
2. Extract the contents of the downloaded zip file to disk.
3. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
4. Push the data into the scoped storage of the sample app:
 	* `adb push mil2525d.stylx /Android/data/com.esri.arcgisruntime.sample.featurelayerdictionaryrenderer/files/mil2525d.stylx`
	* `adb push militaryoverlay.geodatabase /Android/data/com.esri.arcgisruntime.sample.featurelayerdictionaryrenderer/files/militaryoverlay.geodatabase`

## Tags

military, symbol
