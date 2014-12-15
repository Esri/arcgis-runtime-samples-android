# Local MBTiles
This sample extends the abstract base class ```TiledServiceLayer``` and implements the ```getTile()``` method to fetch MBTiles from a SQLite Database.  

## Sample Design 

### MBTiles format
Map Box is a popular open source tool for publishing tile based web maps.  It uses a tiling scheme similar to Open Street Maps known as the Tile Map Service (TMS) specification.  Instead of storing its tiles in a compact cache format like Esri, their tools store the tiles in a SQLite database.  The full specification is available [here](https://github.com/mapbox/mbtiles-spec).

The TMS Specification is similar to other web map tiling schemes used by ArcGIS Online®, Google Maps®, or Open Street Maps.  For convenience, the pre-rendered image tiles are stored in a SQL database in a table named **tiles**.  The table has three integer fields named **zoom_level**, **tile_column**, and **tile_row**.  The last field is a blob named **tile_data** which holds the PNG or JPEG tile image. 


### MBTilesLayer
The ArcGIS Runtime SDK for Android introduced ```TiledServiceLayer``` abstract base class in version 10.1.1.  It is used as the base class for ```ArcGISTiledMapService```, ```BingMapsLayer```, and ```OpenStreetMapLayer```. The class exposes a protected abstract method, ```getTile()```, to fetch tiles.  This sample extends this class and implements the ```getTile()``` method to create a layer by fetching MBTiles from a SQLite database.  

### LocalMBTiles
To use the ```MBTilesLayer``` class just construct the layer, passing the location of the MBTiles SQLite database and add the layer to the MapView.  The ```LocalMBTiles``` class is an sample usage of the ```MBTilesLayer``` based local data stored on device.  

## Provision your device
1. Download the data from [ArcGIS Online](http://www.arcgis.com/home/item.html?id=7b650618563741ca9a5186c1aa69126e).  
2. Extract the contents of the downloaded zip file to disk.  
3. Create an ArcGIS/samples/mbtiles folder on your device. This requires you to use the [Android Debug Bridge (adb)](https://developer.android.com/guide/developing/tools/adb.html) tool found in **<sdk-dir>/platform-tools**.
4. Open up a command prompt and execute the ```adb shell``` command to start a remote shell on your target device.
5. Navigate to your sdcard directory, e.g. ```cd /storage/sdcard0/```.  
6. Create the ArcGIS/samples/mbtiles directory, ```mkdir ArcGIS/samples/mbtiles```.
7. You should now have the following directory on your target device, ```/storage/sdcard0/ArcGIS/samples/mbtiles```. We will copy the contents of the downloaded data into this directory. Note:  Directory may be slightly different on your device.
8. Exit the shell with the, ```exit``` command.
9. While still in your command prompt, navigate to the folder where you extracted the contents of the data from step 1 and execute the following command: 
	* ```adb push world_countries.mbtiles /storage/sdcard0/ArcGIS/samples/mbtiles```

## Using the Sample
Once you have the sample deployed to your device you can interact with the sample with the following: 

1. The map will display the World Streets Basemap with the MBTiles layer overlayed.  
2. Double tap on the map to zoom in and see the zoom level dependency on the overlayed MBTiles.

