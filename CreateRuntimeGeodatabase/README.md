# Create Local Runtime Geodatabase
This sample demonstrates the [services pattern](https://developers.arcgis.com/en/android/guide/create-an-offline-map.htm#ESRI_SECTION1_E2093983C98B4E74A1ED574608537642) for generating a runtime geodatabase from a feature service.  

# Sample Design
This sample is designed to allow users to generate a runtime geodatabase on the device from a pre-defined feature service.  Once successfully downloaded the features from the local runtime geodatabase are added to the MapView as an operational layer.  The main API features used are the ```GenerateGeodatabaseParameters``` class to define the parameters to allow you to control features in the generated runtime geodatabase and ```GeodatabaseSyncTask``` class which creates the runtime geodatabase and downloads the file when it has been successfully created. 

# How to use the Sample
The sample starts up with a MapView containing the [World Topographic Basemap](http://www.arcgis.com/home/item.html?id=30e5fe3149c34df1ba922e6f5bbf808f).  The action bar as a download icon that when pressed will initiate the creation, download, and adding the local runtime geodatabase features from the [Wildfire Response Points](http://services.arcgis.com/P3ePLMYs2RVChkJx/arcgis/rest/services/Wildfire/FeatureServer/0) Wildfire feature service.  The runtime geodatabase file directory will be displayed in a [TextView](http://developer.android.com/reference/android/widget/TextView.html) above the ```MapView```.  The default download location is ```<EXTERNAL-STORAGE-DIR>/ArcGIS/samples/CRGdb/wildfire.geodatase```.  

