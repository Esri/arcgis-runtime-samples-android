# Convert to MGRS Grid
This sample app demonstrates how to convert a map point to military grid and displays the conversion on the map.  

# Sample Design 
The app responds to user interaction by way of a single tap on the map.  The MapView's onSingleTapListener handles a single tap event and converts the map point to military grid string.  A graphic is created to represent the point on the map with the converted military grid coordinate show in as a text symbol.