# Featured User Group Sample

The primary purpose of this sample is to show how to log in to a portal, fetch info about the featured groups, fetch info about webmap items in a particular group, then fetch and display a particular webmap.

A secondary purpose is to demonstrate two different techniques for handling device configuration changes.

## Features

* Portal API
* User Credentials
* Simple Async Task
* Fetch Groups
* Fetch Group Thumbnail
* Fetch Portal Items
* Fetch Portal Item Thumbnail
* Fetch WebMap
* Create and Display MapView
* Device Configuration Changes

## How to use the Sample

The sample starts by logging in to the portal and displaying a list of featured groups. When you select a group it fetches and displays a list of web map items in the selected group. When you select an item it fetches and displays the web map. Press the Back key repeatedly to exit the app.

## Sample Design

This sample contains two activities, ```FeaturedGroupsActivity``` and ```MapActivity```. ```FeaturedGroupsActivity``` hosts two fragments, ```GroupsFragment``` and ```ItemsFragment```.

### FeaturedGroupsActivity

**FeaturedGroupsActivity** is the main activity of this sample. It simply hosts the ```GroupsFragment``` and the ```ItemsFragment```. On startup it launches a ```GroupsFragment```.

### GroupsFragment

**GroupsFragment** is responsible for logging in to the portal and displaying a list or grid of featured groups.

It handles device configuration changes, for example changing between portrait and landscape orientation, by calling ```setRetainInstance(true)``` in its ```onCreate()``` method. This stops the fragment from being destroyed when its activity is destroyed and recreated, so its Portal and PortalGroup objects are retained.

The ```onCreateView()``` method creates the view from the ```list_layout``` layout resource. The ```layout/list_layout.xml``` file contains a ListView for use in portrait orientation, but ```layout-land/list_layout.xml ``` contains a GridView for use in landscape orientation. An instance of private class ```FeaturedGroupListAdapter``` is set as the list adapter.

The ```onViewCreated()``` method  executes an AsyncTask to do most of the work. A private class ```FeaturedGroupsAsyncTask``` extends AsyncTask and overrides the following methods:

* ```onPreExecute()``` displays a progress dialog on the UI thread.
* ```doInBackground()```, which runs on a background thread, logs in to the server and fetches information about the featured groups. It then fetches the thumbnail for each group. The group title and thumbnail are saved in ```mFeaturedGroups```, the array list which backs the list adapter.
* ```onPostExecute()``` then displays the information on the UI thread by simply calling ```notifyDataSetChanged()``` on the list adapter.

A Portal object is created using the constructor which takes portal URL and credentials. The ```fetchPortalInfo()``` method is used to log in to the portal. This returns a PortalInfo object and its ```getFeaturedGroupsQueries()``` method provides a list of queries we can use to find the featured groups. Each query in turn is submitted using the ```Portal.findGroups()``` method. For each group, its thumbnail (if any) is fetched by calling ```fetchThumbnail()```.

When the user chooses a group, the ```onItemClick()``` method of the OnItemClickListener setup in ```onCreateView()``` creates and launches an ```ItemsFragment```, passing it the Portal object and the PortalGroup of the chosen group.

### ItemsFragment

**ItemsFragment** has a very similar structure to ```GroupsFragment```. It calls ```setRetainInstance(true)``` to retain the Portal, PortalGroup and PortalItem objects it uses. It creates its view from the ```list_layout``` layout resource, so this is a ListView or GridView dependant on orientation. It uses private classes to provide a list adapter and an AsyncTask to do the work.

The work done on the background thread consists of creating a query to find all web maps in the chosen group and submitting this using the ```Portal.findItems()``` method. For each item found, its thumbnail (if any) is fetched by calling ```fetchThumbnail()```. The item title and thumbnail are saved in mItems, the array list which backs the list adapter.

When the user chooses a web map, the ```onItemClick()``` method of the OnItemClickListener setup in ```onCreateView()``` launches a new activity, ```MapActivity```, and passes it the item ID of the chosen map.

### MapActivity

A separate activity, **MapActivity**, is used to display the map (see below for why). It displays a progress dialog and creates a WebMap by calling the ```newInstance()``` method that takes an itemId, Portal and CallbackListener. The CallbackListener’s ```onCallback()``` method is called when the WebMap has been created. It runs a Runnable on the UI thread that creates a MapView and uses it to display the WebMap. It sets an OnStatusChangedListener on the MapView so that it can dismiss the progress dialog when initialisation of the MapView is complete.

### Device Configuration Changes

The reason for using a separate activity to display the map is to optimise behaviour on device configuration changes. ```GroupsFragment``` and ```ItemsFragment``` use ```Fragment.setRetainInstance()``` to retain Portal, PortalGroup and PortalItem objects when their host activity is destroyed on configuration changes. However this technique would not work well for the WebMap object used by ```MapActivity``` because it becomes tied to the MapView used to display it and a new MapView must be created if the host activity is destroyed and recreated.

We solve this problem by isolating use of our WebMap in a separate activity, MapActivity, and specifying the ```android:configChanges``` attribute for ```MapActivity``` as follows in the app’s manifest file:
```
android:configChanges="orientation|screenSize|keyboard|keyboardHidden”
```

This stops ```MapActivity``` from being destroyed and restarted when orientation and keyboard configuration changes occur.

So why don't we use this technique in ```GroupsFragment``` and ```ItemsFragment```? These fragments make use of config-dependent resources, in particular using different layout files for different device orientations. If we specified the ```android:configChanges``` attribute as above for the activity hosting these fragments, we would need to write our own code to switch the config-dependent resources on each configuration change, rather than letting the Android system handle that for us. It would not be much work to do the switch of our one layout file ourselves, but some apps may make much more extensive use of config-dependent layouts, strings, drawables, dimensions, etc. This app shows how such apps can avoid having to handle the config changes themselves.
