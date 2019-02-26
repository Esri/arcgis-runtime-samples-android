# Generate Offline Map Overrides
Take a web map offline with additional overrides.

![Generate Offline Map Overrides App](generate-offline-map-overrides.png)

## How to use the sample
To take a web map offline:

1. Click on "Generate Offline Map (Overrides)".
1. Use the seek bars to adjust the min/max "Scale Level"s to be taken offline for the Streets basemap.
1. Use "Extent Buffer Distance" seek bar to set the buffer radius for the streets basemap. 
1. Use the check boxes to include/exclude the "System Valves" and "Service Connections" layers. 
1. Use the "Min Hydrant Flow Rate" seek to select the maximum flow rate for the features from the "Hydrant" layer.
1. Use the "Water Pipes" check box to crop the water pipes feature layer. 
1. Click "Start Job".
1. You may be prompted to sign into arcgis.com.
1. Wait for the progress bar to indicate that the task has completed.
1. You should see that the basemap does not display beyond the min max scale selected. The System Valves and Service Connections should be included or omitted from the offline map and the Hydrants layer should contain a subset of the original features based on your selection. Finally, the network data set should extend past or be cropped to the target area based on your selection.

## How it works
The sample creates a `PortalItem` object using a web map’s ID. This portal item is also used to initialize an `OfflineMapTask` object. When the button is clicked, the sample requests the default parameters for the task, with the selected extent, by calling `OfflineMapTask.createDefaultGenerateOfflineMapParameters(areaOfInterest)`. Once the parameters are retrieved, they are used to create a set of `GenerateOfflineMapParameterOverrides` by calling `OfflineMapTask.createGenerateOfflineMapParameterOverridesAsync(generateOfflineMapParameters)`. The overrides are then adjusted so that specific layers will be taken offline using custom settings.

### Streets basemap (adjust scale range)
In order to minimize the download size for offline map, this sample reduces the scale range for the "World Streets Basemap" layer by adjusting the relevant `ExportTileCacheParameters` in the `GenerateOfflineMapParameterOverrides`. The basemap layer is used to construct an `OfflineMapParametersKey`object. The key is then used to retrieve the specific `ExportTileCacheParameters` for the basemap and the `levelIds` are updated to skip unwanted levels of detail (based on the values selected in the UI). Note that the original "Streets" basemap is swapped for the "for export" version of the service - see https://www.arcgis.com/home/item.html?id=e384f5aa4eb1433c92afff09500b073d.

### Streets Basemap (buffer extent)
To provide context beyond the study area, the extent for streets basemap is padded. Again, the key for the basemap layer is used to obtain the key and the default extent `Geometry` is retrieved. This extent is then padded (by the distance specified in the UI) using the `GeometryEngine.bufferGeodesic(areaOfInterest, bufferDistance)` function and applied to the `ExportTileCacheParameters`.
 
### System Valves and Service Connections (skip layers)
In this example, the survey is primarily concerned with the Hydrants layer, so other information is not taken offline: this keeps the download smaller and reduces clutter in the offline map. The two layers "System Valves" and "Service Connections" are retrieved from the operational layers list of the map. They are then used to construct an `OfflineMapParametersKey`. This key is used to obtain the relevant `GenerateGeodatabaseParameters` from the `GenerateOfflineMapParameterOverrides.generateGeodatabaseParameters()` property. The `GenerateLayerOption` for each of the layers is removed from the geodatabase parameters `layerOptions` by checking for the `FeatureLayer.getServiceLayerId()`. Note, that you could also choose to download only the schema for these layers by setting the `GenerateLayerOption.setQueryOption(setQueryOption(GenerateLayerOption.QueryOption.NONE)`.
 
### Hydrant Layer (filter features)
Next, the hydrant layer is filtered to exclude certain features. This approach could be taken if the offline map is intended for use with only certain data - for example, where a re-survey is required. To achieve this, a whereClause (for example, "Flow Rate (GPM) < 500") needs to be applied to the hydrant's `GenerateLayerOption` in the `GenerateGeodatabaseParameters`. The minimum flow rate value is obtained from the UI setting. The sample constructs a key object from the hydrant layer as in the previous step, and iterates over the available `GenerateGeodatabaseParameters` until the correct one is found and the `GenerateLayerOption` can be updated.

### Water Pipes Data set (skip geometry filter)
Lastly, the water network data set is adjusted so that the features are downloaded for the entire data set - rather than clipped to the area of interest. Again, the key for the layer is constructed using the layer and the relevant `GenerateGeodatabaseParameters` are obtained from the overrides dictionary. The layer options are then adjusted to set `useGeometry` to false.

Having adjusted the `GenerateOfflineMapParameterOverrides` to reflect the custom requirements for the offline map, the original parameters and the custom overrides, along with the download path for the offline map, are then used to create a `GenerateOfflineMapJob` object from the offline map task. This job is then started and on successful completion the offline map is added to the map view. To provide feedback to the user, the progress property of `GenerateOfflineMapJob` is displayed in a window.

As the web map that is being taken offline contains an Esri basemap, this sample requires that you sign in with an ArcGIS Online organizational account.

## Relevant API
* OfflineMapTask
* GenerateGeodatabaseParameters
* GenerateOfflineMapParameters
* GenerateOfflineMapParameterOverrides
* GenerateOfflineMapJob
* GenerateOfflineMapResult
* ExportTileCacheParameters

#### Tags
Edit and Manage Data
