# Class Breaks Renderer  
The ```ClassBreaksRenderer``` sample is an app to demonstrate rendering using class breaks. The sample depends on the feature layer to be running: http://tmservices1.esri.com/arcgis/rest/services/LiveFeeds/NOAA_METAR_current_wind_speed_direction/MapServer


#How to use the Sample
This sample uses ```GeodatabaseFeatureServiceTable``` to display the ```FeatureLayer```. Class breaks are created and added to the layer. Wind stations are displayed using arrow markers whose size depends upon the wind speeds and rotation depends on the wind direction.  

## App usage
1. The feature layer might take a few seconds to load. Otherwise check if the service is running.
2. Once the layer is loaded, click on an arrow marker. 
3. A callout is displayed showing the **Station Name**, **Country**, **Temperature** and **Wind Speed**.
