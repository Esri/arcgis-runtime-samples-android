# AddCSV2GraphicsLayer
This sample demonstrates how to access a file based feed from a URL to add features to your map. For this example, storm reports provided in the form of Comma Separated Value (CSV) records from the United States National Weather Service's Storm Prediction Center are used as the CSV data.

# Sample Design 
This sample has a MapView with a basemap layer from ArcGIS Online and an Android DatePicker. The basemap layer is added to the map when the application first starts, and the initial extent of the map is set to show the United States, since the data is relevant only to this specific part of the world. Once the application loads, the user can click the button at the top of the display to invoke a DatePicker dialog box. The user specifies a date and the reports for that day display on the map.

The reports come from Hypertext Transfer Protocol uniform resource locator (HTTP URL) connections to CSV files hosted by the National Weather Service. For example, a URL connection is made to http://www.spc.noaa.gov/climo/reports/" + date + "_rpts_wind.csv for the Wind reports. The application connects to these files and parses the x,y coordinates with the other attributes, creating a point graphic feature with attributes for each event. Symbology and rendering are applied to the graphics layer to show tornados in red, hail reports in green, and wind damage in blue.

The user can then zoom and pan the map display, and can single-tap an event to view more information via an OnSingleTapListener implemented and added to the map.

If there are no storms for the selected date, a message appears indicating that no storms were reported.