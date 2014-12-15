# ExportTileCacheTask Sample
This sample demonstrates how to extract a tile package for offline use from a tiled basemap service on ArcGIS Online.

# Sample Design
This sample allows user to export tile cache locally on to device storage, from a tiled basemap service on ArcGIS Online that supports the "export tiles" operation. Once the tile cache is successfully downloaded the sample switches the online tile layer in the MapView with the local tile cache. The Sample uses ```ExportTileCacheParameters``` class to which lets you specify if the downloaded tile cache will be a tile package(tpk) or a compact cache, the Level of details(LODs), the extent the tile cache will cover and ```ExportTileCacheTask``` class which takes these parameters and generates the tile cache.

# How to use the Sample
The Sample starts with a MapView containing the World Street Map. The sample by default is set to generate the tile cache as a compact cache. The 'Select Levels' button lets the user specify the levels to be downloaded. The viewable area in the map is the extent of the map that will be downloaded. The user can change the extent by panning and zooming in/out. Once user hits the download button, the process to create and download the compact cache will be initiated. When the download is done the Sample switches the online tile layer with the local tile layer. The default download location is ```<EXTERNAL-STORAGE-DIR>/ArcGIS/samples/tiledcache/```


