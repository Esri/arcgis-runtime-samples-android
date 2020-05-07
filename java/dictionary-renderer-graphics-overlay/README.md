# Dictionary renderer with graphics overlay

Create graphics using a local mil2525d style file and an XML file with key/value pairs for each graphic.

![Image of dictionary renderer graphics overlay](dictionary-renderer-graphics-overlay.png)

## Use case

Use a dictionary renderer on a graphics overlay to display more transient data, such as military messages coming through a local tactical network.

## How to use the sample

Run the sample and view the military symbols on the map.    

## How it works

1. Create a new `DictionarySymbolStyle` with `DictionarySymbolStyle.createFromFile()`.
2. Create a new `DictionaryRenderer`, passing in the `symbolDictionary`.
3. Create a new `GraphicsOverlay`.
4. Set the  dictionary renderer to the graphics overlay.
5. Parse through the local XML file creating a map of key/value pairs for each block of attributes.
6. Create a `Graphic` for each attribute.
7. Use the `_wkid` key to get the geometry's spatial reference.
8. Use the `_control_points` key to get the geometry's shape.
9. Add the graphic to the graphics overlay.

## Relevant API

* DictionaryRenderer
* DictionarySymbolStyle
* GraphicsOverlay

## Offline Data

1. Download the data from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=c78b149a1d52414682c86a5feeb13d30).
2. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
3. Push the data into the scoped storage of the sample app:
`adb push mil2525d.stylx /Android/data/com.esri.arcgisruntime.sample.dictionaryrenderergraphicsoverlay/files/mil2525d.stylx`

### Tags

defense, military, situational awareness, tactical, visualization
