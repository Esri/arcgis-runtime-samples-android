# Create and save map

Create and save a map as an ArcGIS `PortalItem` (i.e. web map).

![Image of create and save map](create-and-save-map.png)

## Use case

Maps can be created programmatically in code and then serialized and saved as an ArcGIS web map. A web map can be shared with others and opened in various applications and APIs throughout the platform, such as ArcGIS Pro, ArcGIS Online, the JavaScript API, Collector, and Explorer.

## How to use the sample

When you run the sample, you will be challenged for an ArcGIS Online login. Enter a user name and password for an ArcGIS Online named user account (such as your ArcGIS for Developers account). Then, choose the basemap and layers for your new map. To save the map, add a title, tags, description (optional), and a folder on your portal (you will need to create one in your portal's My Content section if you don't already have one). Click the Save button to save the map to the chosen folder.

## How it works

1. Set a `DefaultAuthenticationChallengeHandler` to the app's `AuthenticationManager`.
2. Create a new `Portal` and load it. Once it has loaded, an authentication challenge will be issued and you'll be prompted to enter your username and password.
3. Access the `PortalUserContent` with `portal.getUser().fetchContentAsync().get()`, to get the user's list of portal folders with `portalUserContent.getFolders()`.
4. Create an `ArcGISMap` with a `BasemapStyle` and a few operational layers.
5. Call `map.saveMapAsAsync()` to save a new `ArcGISMap` with the specified title, tags, and folder to the portal.

## Relevant API

* ArcGISMap
* Portal

## Additional information

In this sample, an API key is set directly on `Basemap` objects rather than on the whole app using the `ArcGISRuntimeEnvironment` class. This is useful in a scenario where an individual developer is part of an organization within ArcGIS Online that uses an API key to access a range of `BasemapStyle`s. In the case that an individual member of the organization wants to save a map locally to their account, and not that of the organization, they can set the organization's API key on the basemap, and log in to their own account when challenged.  The individual can then save the final map to their own ArcGIS Online account.

## Tags

ArcGIS Online, ArcGIS Pro, portal, publish, share, web map
