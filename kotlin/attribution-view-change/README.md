# Attribution view change

Make UI elements respond to changes in the attribution view.

![Image of attribution view change](attribution-view-change.png)

## Use case

ArcGIS Online basemaps, Esri data services, or Esri API technology used in an application require inclusion of an Esri attribution. Depending on its content, the attribution bar may be expandible and collapsible. In this case, expanding the attribution bar may obstruct some of the user interface in the application. To avoid this, the developer may want to make some of the UI elements reposition when the attribution bar is interacted with.

## How to use the sample

Select the attribution bar to expand it, and observe how the floating action button moves up. Select the attribution bar again to minimize it and see how the floating action button moves down.

## How it works

1. Add a layout change listener to the Map View with `addAttributionViewLayoutChangeListener`.
2. Get the difference in pixels by using `hightDelta = oldBottom - bottom`.
3. Change the floating action button's bottom margin using its `layoutParams`.

## Relevant API

* MapView

## Additional information

For more information, see [Attribution in your app](https://developers.arcgis.com/terms/attribution/).

## Tags

translation, UI, user interface
