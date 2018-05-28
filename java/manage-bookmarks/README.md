# Manage Bookmarks
Access and add bookmarks to a map.

![Manage Bookmarks App](manage-bookmarks.png)

## How to use the sample
This sample already has some bookmarks added to the map. You can tap on the spinner bar on the top to access these bookmarks. When the bookmark is selected the MapView is updated to show the viewpoint associated with the bookmark. To add a new bookmark pan/zoom in to the new location for the bookmark. Tap on the floating action button, which will pop up a dialog box where you can enter the name you want to give to this bookmark. When you tap ok the new bookmark will be created and it will show up in the spinner list.

## How it works
1. Get the `BookmarkList` from the `MapView`.
1. Add `Bookmark`s to the `BookmarkList`.
1. Use `MapView.setViewpointAsync(...)` to the `BookMark`s `getViewpoint()`.

## Relevant API
* ArcGISMap
* MapView
* BookmarkList
* Bookmark
* Viewpoint

#### Tags
MapViews, SceneViews and UI