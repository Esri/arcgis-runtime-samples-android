# Authenticate with OAuth

This sample demonstrates how to authenticate with ArcGIS Online (or your own portal) using OAuth2 to access secured resources (such as private web maps or layers). Accessing secured items requires a login on the portal that hosts them (an ArcGIS Online account, for example). This sample utilizes Android WebView to show the 
OAuth sign-in page in a dialog.

Your app may need to access items that are only shared with authorized users. For example, your organization may host private data layers or feature services that are only accessible by verified users. You may also need to take advantage of premium ArcGIS Online services, such as geocoding or routing, that require a named user login.

![Authenticate with OAuth](authenticate-with-oauth.png)

## How to use the sample

1. Before you run the sample, specify your OAuth configuration along with the portal to login to and the webmap id in strings.xml.
   http://android.esri.com:8080/docs/arcgis-android/publicAPIRelease/com/esri/arcgisruntime/security/OAuthConfiguration.html
   https://developers.arcgis.com/labs/android/access-services-with-oauth-2/
2. Once you launch the app, you will be challenged for an ArcGIS Online login to view the private layers.
3. Enter a user name and password for an ArcGIS Online named user account (such as your ArcGIS for Developers account).
4. If you authenticate successfully, the private layers will display in the map.

## How it works
1. When the app loads, a web map containing premium content (world traffic service) is attempted to be loaded in the map view.
2. In response to the attempt to access secured content, the `AuthenticationManager` shows an OAuth authentication dialog from ArcGIS Online.
3. If the user authenticates successfully, the private layers will display in the map.

## Relevant API
 * AuthenticationManager
 * AuthenticationChallengeHandler
 * OAuthConfiguration
 * ArcGISPortal
 * Credential

#### Tags
Authentication
Security
OAuth