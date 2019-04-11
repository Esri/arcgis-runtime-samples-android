# Read symbols from a mobile style

Open a mobile style (.stylx) and read its contents. Combine several 
symbols from the style into a single multilayer point symbol, then use
it to display graphics in the map view.

![Read symbols from a mobile style App](read-symbols-mobile-style.png)

## Use case

Multilayer symbols provide the ability to create more complex and
interesting symbology for geoelements. While these symbols can be
created from scratch, a more convenient workflow is to author them using
ArcGIS Pro and store them in a mobile style file (.stylx). ArcGIS
Runtime can read symbols from a mobile style, and you can modify and
combine them as needed in your app.

## How to use the sample

* Select symbols from each list box to create a face emoji. A preview of
  the symbol is updated as you make selections.
* You can optionally select a new color from the drop down list and set
  the symbol size using the slider.
* Tap the map to create a point graphic that uses the current face symbol.
* Tap the `Clear` button to clear all graphics from the display.

## How it works

1. On startup, read a mobile style file using `SymbolStyle.OpenAsync`. 
2. Get a list of all symbols in the style by calling `SearchSymbolsAsync` with the default search parameters.
3. Iterate the list of `SymbolStyleSearchResult` and add symbols to list boxes according to their category. Display a preview of each symbol with `CreateSwatchAsync`.
4. When symbol selections change, create a new multilayer symbol by passing the keys for the selected symbols into `GetSymbolAsync`. Color lock all symbol layers except the base layer and update the current symbol preview image.
5. Create graphics symbolized with the current symbol when the user taps the map view.

## Relevant API

* MultilayerPointSymbol
* MultilayerSymbol.CreateSwatchAsync
* SymbolLayer
* SymbolStyle
* SymbolStyle.GetSymbolAsync
* SymbolStyleSearchParameters

## Offline Data

A mobile style file (created using ArcGIS Pro) provides the symbols used by the sample.

Link | Local Location
---------|-------|
|[Emoji mobile style](https://arcgisruntime.maps.arcgis.com/home/item.html?id=1bd036f221f54a99abc9e46ff3511cbf)| `<sdcard>`/ArcGIS/Runtime/Data/style/emoji-mobile.stylx |


## Tags

advanced symbology, multilayer, mobile style, stylx
