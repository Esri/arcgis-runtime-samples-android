# Create and save map

Create and save a map as an ArcGIS `PortalItem` (i.e. web map).

![Image of create and save map](create-save-map.png)

## Use case

Maps can be created programatically in code and then serialized and saved as an ArcGIS `web map`. A `web map` can be shared with others and opened in various applications and APIs throughout the platform, such as ArcGIS Pro, ArcGIS Online, the JavaScript API, Collector, and Explorer.

## How to use the sample

Use the navigation drawer to select the basemap and layers you'd like to add to your map. Tap the save button and enter a title, tags, and description for your map and then hit 'Save Map'. Sign into an ArcGIS Online account when prompted and save the map.

## How it works

1. A `Map` is created with a `Basemap` and a few operational layers.
2. A `Portal` object is created and loaded. This will issue an authentication challenge, prompting the user to provide credentials.
1. Use `ArcGISMap.saveAsAsync` passing in the `Portal`, and information about the map, to save the map to the portal.

NOTE: The app uses OAuth2 protocol to authenticate the named users.  In order to do that the app must declare an Activity and intent filter to redirect the URI. This will parse the authorization code from the response URI and use it to fetch the `OAuthTokenCredential`.

## Relevant API

* ArcGISMap
* Portal

## Tags

ArcGIS Online, ArcGIS Pro, portal, publish, share, web map
