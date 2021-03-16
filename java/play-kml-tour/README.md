# Play KML tour

Play tours in KML files.

![Image of play KML tour](play-kml-tour.png)

## Use case

KML, the file format used by Google Earth, supports creating tours, which can control the viewpoint of the scene, hide and show content, and play audio. Tours allow you to easily share tours of geographic locations, which can be augmented with rich multimedia. Runtime allows you to consume these tours using a simple API.

## How to use the sample

The sample will load the KMZ file from ArcGIS Online. When a tour is found, the _Play_ button will be enabled. Use _Play_ and _Pause_ to control the tour. When you're ready to show the tour, use the reset button to return the tour to the unplayed state.

## How it works

1. Create a `KmlDataSet` from the local kmz file and instantiate a layer from it with `new KmlLayer(kmlDataSet)`.  
2. Create the KML tour controller. Wire up the buttons to the `kmlController.play()`, `kmlController.pause()`, and `kmlController.reset()` methods.
3. Explore the tree of KML content to find the first KML tour. Once a tour is found, provide it to the KML tour controller.

## Relevant API

* KmlTour
* KmlTourController
* KmlTourController.pause()
* KmlTourController.play()
* KmlTourController.reset()

## Offline Data

1. Download the data from [ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=f10b1d37fdd645c9bc9b189fb546307c).
2. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1.
3. Push the data into the scoped storage of the sample app:
`adb push Esri_tour.kmz /Android/data/com.esri.arcgisruntime.sample.playkmltour/files/Esri_tour.kmz`

## About the data

This sample uses a custom tour created by a member of the ArcGIS Runtime API samples team. When you play the tour, you'll see a narrated journey through some of Esri's offices.

## Additional information

See [Touring in KML](https://developers.google.com/kml/documentation/touring) in *Keyhole Markup Language* for more information.

## Tags

animation, interactive, KML, narration, pause, play, story, tour
