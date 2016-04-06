# ArcGIS Tiled Layer Render Mode

![](arcgis-tiledlayer-rendermode.png)

The sample demonstrates how the Tiled layer Render Mode affects what scale the layer is rendered at. There are 2 types of render modes, **SCALE** (which is the default) and **AESTHETIC**. **SCALE** always renders the layer at the correct scale which can result in a fuzzy effect on high DPI screens, **AESTHETIC** aims to ensure the layer draws clearly which will result in the map scale not being respected. 

## Features
* ArcGISTiledLayer
* Basemap
* Map
* MapView

## How to use the sample
The ArcGIS Tile Layer Render Mode app uses a Tiled layer basemap from an ArcGIS Online service URL. The **RenderMode** spinner lets to select the various possible Render Modes. The user can zoom in/out on the map to visually observe the impact of the Render Mode selected. You can navigate to the predefined Tiled layer scales using the seekbar. The seekbar scale values are displayed in a TextView located at top right corner of MapView. The user can also do fine-grained zoom in/out operations using gestures on the MapView. 


