# Add ENC exchange set

Display nautical charts conforming to the ENC specification.
 
![Add ENC exchange set App](add-enc-exchange-set.png)
 
## Use case
 
Maritime applications require conformity to strict specifications over how hydrographic data is displayed digitally to ensure the safety of traveling vessels.
 
S-57 is the IHO (International Hydrographic Organization) Transfer Standard for digital hydrographic data. The symbology standard for this is called S-52. There are different product specifications for this standard. ENC (Electronic Navigational Charts) is one such specification developed by IHO.
 
An ENC exchange set is a catalog of data files which can be loaded as cells. The cells contain information on how symbols should be displayed in relation to one another, so as to represent information such as depth and obstacles accurately.
 
## How it works

1. Specify the path to a local CATALOG.031 file to create an `EncExchangeSet`.
2. After loading the exchange set, loop through the `EncDataset` objects in `encExchangeSet.getDatasets()`.
3. Create an `EncCell` for each dataset. Then create an `EncLayer` for each cell.
4. Add the ENC layer to a map's operational layers collection to display it.

## Relevant API

* EncCell
* EncDataset
* EncExchangeSet
* EncLayer

## Offline Data

1. To use ENC in ArcGIS Runtime, extra resources are required [Hydrography Data](https://developers.arcgis.com/downloads/data).
1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=9d2987a825c646468b3ce7512fb76e2d).
1. Extract the contents of the downloaded zip files to disk.
1. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1 & 2.
1. Execute the following command:

`adb push hydrography /sdcard/ArcGIS/hydrography`

`adb push ExchangeSetwithoutUpdates /sdcard/ArcGIS/Samples/ENC/ExchangeSetwithoutUpdates`

Link | Local Location
---------|-------|
|[Hydrography Data](https://developers.arcgis.com/downloads/data)| `<sdcard>`/ArcGIS/hydrography/|
|[ENC Exchange Set](https://arcgisruntime.maps.arcgis.com/home/item.html?id=9d2987a825c646468b3ce7512fb76e2d)| `<sdcard>`/ArcGIS/Samples/ENC/ExchangeSetwithoutUpdates/|
 
#### Tags
Layers
ENC
maritime
nautical chart
hydrographic
data