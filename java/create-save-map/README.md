# Create and Save a Map
Create a map and save it to your portal.

![Create and Save a Map App](create-save-map.png)

## How to use the sample
This sample uses the [named user login](https://developers.arcgis.com/authentication/#named-user-login) authentication pattern. Before a user can save a map they have to login to their [ArcGIS Online](https://www.arcgis.com/) account. Follow the steps below before running the app.  

1. Open the navigation drawer by tapping on the Drawer Toggle or sliding right from the left side of the screen. Choose basemap and operational layers to create a map. Tap on save menu option to save the map. User will be provided with an authentication page to login.  Once logged in you provide a title, tags and description (optional) for the map.
1. Login to your [ArcGIS Developers site](http://developers.arcgis.com) account and [Register](https://developers.arcgis.com/applications/#/new/) your app.  Once registered, select the **Authentication** tab taking note of your **Client ID**.
1. A custom Redirect URI has been set up for this app, while still under the **Authentication** tab in your [ArcGIS Developers site](http://developers.arcgis.com) account, navigate down the page to the **Redirect URIs** section and add `my-ags-app://auth` redirect uri. 
1. Open the **portal_settings.xml** resource file found in **..\src\res\values\** directory.  Edit the following with the `CLIENT_ID` value you obtained above:  
	```xml
	<!-- Client ID of your registered application --> 
	<item name="client_id">your-clientID</item> 
	```

## How it works
1. Create an `OAuthLoginManager`.
1. Use it to `lauchOAuthBrowserPage`.
1. Receive the resulting `Intent` in the MapSaveActivity.
1. Fetch the OAuth token from the OAuthLogin Manager using `fetchOAuthTokenCredential(intent)`
1. Set the OAuth credentials to a `Portal`.
1. Use `ArcGISMap.saveAsAsync`to save the map to the portal.

NOTE: The app uses OAuth2 protocol to authenticate the named users.  In order to do that the app must declare an Activity and intent filter to redirect the URI. This will parse the authorization code from the response URI and use it to fetch the `OAuthTokenCredential`.

## Relevant API
* ArcGISMap
* OAuthLoginManager
* OAuthTokenCredential
* Portal
* PortalSettings
* PortalItem
