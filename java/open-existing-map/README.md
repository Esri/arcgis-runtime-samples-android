# Open Existing Map
Open an existing map as a `PortalItem` from a `Portal`.

![Open Existing Map App](open-existing-map.png)

## How to use the sample
The app opens with a web map from a portal displayed.  Tap on the navigation drawer icon to see a list of pre-defined web maps. Select any of the web maps to close the drawer and open it up in the `MapView`.

## How it works
`Portal` objects represent information from a portal such as ArcGIS Online.  `PortalItem` represents an item stored in a portal.  We create a `Map` from a `Portal` & `PortalItem` objects then pass the `Map` to the `MapView`.

## Relevant API
* ArcGISMap
* MapView
* Portal
* PortalItem

#### Tags
Cloud and Portal