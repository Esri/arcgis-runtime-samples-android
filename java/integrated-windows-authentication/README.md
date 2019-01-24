# Integrated Windows Authentication
Uses Windows credentials to access services hosted on a portal secured with Integrated Windows Authentication (IWA).

![Integrated windows authentication App](integrated-windows-authentication.png)

## Use case
IWA, which is built into Microsoft Internet Information Server (IIS), works well for intranet applications, but isn't always practical for internet apps.

## How to use the sample
1. Enter the URL to your IWA-secured portal in the edit text box at the top of the screen.
1. Tap the "Sign in" button to search for web maps stored on the portal.
1. You will be prompted for a username and password. 
1. When a correct credential is passed, the portal will load. Repeatedly entering the wrong credential will increase the `FailureCount`. After 5 attempts (in this case), no more prompts for a credential will appear.
1. Select a web map item to display it in the map view.

## How it works
`MapView` has a `identifyLayersAsync(screenLocation, tolerance, returnPopupsOnly, maximumResults)` method that is used in the sample. The method takes a screen location, tolerance, boolean for returning (a pop-up/pop-up and geo-element), and maximum results per layer, which results in a `ListenableFuture<List<IdentifyLayerResult>>`.
	
Layer name and a count of identified features held by each `IdentifyLayerResult` in the `ListenableFuture<List<...>>` is then taken and written out to a String. Finally, the resulting String is displayed in an Android AlertDialog.

## Relevant API
* ArcGISMapImageLayer
* FeatureLayer
* FeatureTable
* MapView

#### Tags
Search and Query