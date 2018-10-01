# Display KML
Display a KML layer from a URL, portal item, or local KML file.

![Display KML App](display-kml.png)

## How to use the sample
Select the KML layer you'd like to display from the dropdown menu in the upper right.

Before attempting to load a KML file from local storage, the file will have to be sideloaded onto the device. See [Offline Data](#offline-data).

## How it works
1. To create a KML layer from a URL, create a `KMLDataset` using the URL to the KML file. Then pass the data set to the `KmlLayer` constructor.
2. To create a KML layer from a portal item, construct a `PortalItem` with a portal and the KML portal item. Pass the portal item to the `KmlLayer` constructor.
1. To create a KML layer from a local file, create a `KMLDataset` using a path to the local KML file in external storage. Then pass the data set to the `KmlLayer` constructor.
1. Add the layer as an operational layer to the map with `map.getOperationalLayers().add(kmlLayer)`.

## Relevant API
* KmlDataset
* KmlLayer
* Portal
* PortalItem

## Offline Data
1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=324e4742820e46cfbe5029ff2c32cb1f).
1. Extract the contents of the downloaded zip file to disk.
1. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
1. Execute the following command:
`adb push US_State_Capitals.kml /sdcard/ArcGIS/Samples/KML/US_State_Capitals.kml`


Link | Local Location
---------|-------|
|[US State Capitals KML](https://arcgisruntime.maps.arcgis.com/home/item.html?id=324e4742820e46cfbe5029ff2c32cb1f)| `<sdcard>`/ArcGIS/Samples/KML/US_State_Capitals.kml|

#### Tags
Layers
