# Closest Facilites

This sample app shows how to find facilities closest to an incident.  In this sample we use the [recreation facilities](http://sampleserver6.arcgisonline.com/arcgis/rest/services/Recreation/FeatureServer/0) as facilities and the closest facility analysis from our [sample server](http://sampleserver6.arcgisonline.com/arcgis/rest/services/NetworkAnalysis/SanDiego/NAServer/ClosestFacility).  Please note that this service is limited to the San Diego Metro area, for [full data coverage](http://resources.arcgis.com/en/help/arcgis-rest-api/#/Data_Coverage_for_Network_Analysis_Services/02r300000034000000/) please use the [ArcGIS Online Network Analysis Service](http://resources.arcgis.com/en/help/arcgis-rest-api/#/Closest_Facility_Service_with_Synchronous_Execution/02r3000000n7000000/). 

# Features
* Calculate route to closest alternative fuel station in San Diego
* Use a feature service query url to limit geographic facilities
* Support for credentials to support ArcGIS Online Network Analysis Service

# Sample Design 
This sample uses the [ClosestFacilityTask](https://developers.arcgis.com/en/android/api-reference/reference/com/esri/core/tasks/ags/na/ClosestFacilityTask.html) class to find the closest recreation facilities based on parameters set with [ClosestFacilityParameters](https://developers.arcgis.com/en/android/api-reference/reference/com/esri/core/tasks/ags/na/ClosestFacilityParameters.html) and results bundled in a [ClosestFacilityResult](https://developers.arcgis.com/en/android/api-reference/reference/com/esri/core/tasks/ags/na/ClosestFacilityResult.html) server response.  There are private fields for user credentials which if you edit the strings will automatically cause the sample to use the [ArcGIS Online Network Analysis Service](http://route.arcgis.com/arcgis/index.html).  