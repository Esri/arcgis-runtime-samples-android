# GeoJson Earthquake Map
This sample demonstrates how to retrieve information from a GeoJson feed. We use the daily summary of earthquake events above 2.5 magnitude.  You can easily change the service url using [USGS GeoJson Feed](http://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php).  The url is set in the samples string resource file. JSON parser is used to parse the GeoJSON feed.

## Features
* ArcGISTiledMapServiceLayer
* GeometryEngine
* GraphicsLayer
* SimpleMarkerSymbol

## Sample Design 
A graphics layer is added on a basemap. The graphic layer displays the earthquake events in the form of circle graphics. On clicking on an event, a callout is displayed showing the magnitude, place, time, gap and rms for that earthquake. 