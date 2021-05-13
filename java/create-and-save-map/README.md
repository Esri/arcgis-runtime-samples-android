# Create and save map

Create and save a map as an ArcGIS `PortalItem` (i.e. web map).

![Image of create and save map](create-and-save-map.png)

## Use case

Maps can be created programmatically in code and then serialized and saved as an ArcGIS web map. A web map can be shared with others and opened in various applications and APIs throughout the platform, such as ArcGIS Pro, ArcGIS Online, the JavaScript API, Collector, and Explorer.

## How to use the sample

Use the navigation drawer to select the basemap and layers you'd like to add to your map. Tap the save button and enter a title, tags, and description for your map and then hit 'Save Map'. Sign into an ArcGIS Online account when prompted and save the map.

## How it works

1. An `ArcGISMap` is created with a `Basemap` and a few operational layers.
2. A `Portal` object is created and loaded. This will issue an authentication challenge, prompting the user to provide credentials.
3. Use `ArcGISMap.saveAsAsync(...)` passing in the `Portal`, and information about the map, to save the map to the portal.

## Relevant API

* ArcGISMap
* Portal

## Additional information

In this sample, an API key is set directly on `Basemap` objects rather than on the whole app using the `ArcGISRuntimeEnvironment` class. This is useful in a scenario where an individual developer is part of an organization within ArcGIS Online that uses an API key to access a range of `BasemapStyle`s. In the case that an individual member of the organization wants to save a map locally to their account, and not that of the organization, they can set the organization's API key on the basemap, and log in to their own account when challenged.  The individual can then save the final map to their own ArcGIS Online account.

## Tags

ArcGIS Online, ArcGIS Pro, portal, publish, share, web map
