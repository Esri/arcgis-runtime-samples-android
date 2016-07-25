# Authoring a map
This sample demonstrates how to author a map and save it to your portal. This sample uses the [named user login](https://developers.arcgis.com/authentication/#named-user-login) authentication pattern.  Before a user can save a map they have to login to their [ArcGIS Online](https://www.arcgis.com/) account. Follow the [authentication](#authentication) steps below before running the app.  

![author-map](author-map.png)

## Authentication
This sample uses the [named user login](https://developers.arcgis.com/authentication/#named-user-login) authentication pattern.  As a developer, you will need the following to make use of this pattern:  

### User account **Portal URL**
Your Portal Organizational URL can be found in your ArcGIS Online profile.  Login to [ArcGIS Developers site](http://developers.arcgis.com).  Click on your account name and open **Password and Profile** in a new tab as this will take you to your ArcGIS Online account.  In your ArcGIS Online account page click on your account name and open your profile.  Your organizational URL is provided in your profile under **Organization URL** 

### Your apps **Client ID**
Navigate back to your [ArcGIS Developers site](http://developers.arcgis.com) account and [Register](https://developers.arcgis.com/applications/#/new/) your app.  Once registered, select the **Authentication** tab taking note of your **Client ID**.

### A custom **Redirect URI**

A custom Redirect URI has been set up for this app, while still under the **Authentication** tab in your [ArcGIS Developers site](http://developers.arcgis.com) account, navigate down the page to the **Redirect URIs** section and add `my-ags-app://auth` redirect uri. 

### Edit **portal_settings.xml** source file
Open the **portal_settings.xml** resource file found in **..\src\res\values\** directory.  Edit the following with the values you obtained above:  

```xml
<!-- your account URL path--> 
<item name="url">https://your-team.maps.arcgis.com/</item> 
<!-- Client ID of your registered application --> 
<item name="client_id">your-clientID</item> 
```

The app uses OAuth2 protocol to authenticate the named users.  In order to do that the app must declare an Activity and [intent filter](https://developer.android.com/guide/components/intents-filters.html) the redirect URI which will parse the authorization code from the response URI and use it to fetch the `OAuthTokenCredential`.  The general workflow is as follows: 

1. `OAuthLoginManager oAuthLoginManager = new OAuthLoginManager(context, portalUrl, clientId, redirectUri, expiration);`
2. `oAuthLoginManager.launchOAuthBrowserPage();`
3. Receive the resulting `Intent` in the activity declared in the manifest
4. `OAuthTokenCredential credential = oAuthLoginManager.fetchOAuthTokenCredential(intent)`


You can see the developer pattern in this app in the **MainActivity.java** class:

```java

    private void oAuthBrowser() {

        try {
            // create a OAuthLoginManager object with portalURL, clientID, redirectUri and expiration
            String[] portalSettings = getResources().getStringArray(R.array.portal);
            oauthLoginManager = new OAuthLoginManager(portalSettings[1], portalSettings[2], portalSettings[3], 0);
            // launch the browser to get the credentials
            oauthLoginManager.launchOAuthBrowserPage(getApplicationContext());

        } catch (Exception e) {
            Log.e("error-", e.getMessage() + "");
        }


    }
```


The resulting `Intent` is handled in the **AndroidManifest.xml** file with the following::

```xml
        <activity android:name="com.esri.arcgisruntime.sample.authoringamap.MapSaveActivity">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <action android:name="android.intent.action.PICK" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:host="auth"
                    android:scheme="my-ags-app" />
            </intent-filter>
        </activity>

```

Then we fetch the credentials in the **MapSaveActivity.java** class

```java
// onCreate()
fetchCredentials(intent);

    private void fetchCredentials(Intent intent) {
        // Fetch oauth access token.
        final ListenableFuture<OAuthTokenCredential> future = oauthLoginManager.fetchOAuthTokenCredentialAsync(intent);
        future.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    oauthCred = future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

```

## How to use the sample
Open the drawer by tapping on the Drawer Toggle or sliding right from the left of the screen. Choose basemap and operational layers to author a map. Tap on save menu option to save the map. User will be provided with an authentication page to login.  Once logged in you provide a title, tags and description (optional) for the map.

## Features
* OAuthLoginManager
* Portal

## How it works
The sample uses a pre-populated list of basemaps and layers to create a ```Map``` and add operational layers on it. The authentication is handled by the ```OAuthLoginManager``` and upon successful response the credentials are extracted from the intent using ```fetchOAuthTokenCredentialAsync```. The fetched credentials are used in the ```Portal``` to save the map using ```saveAsAsync``` method.  The following pattern saves the map in the **MapSaveActivity.java**:

```java
String[] portalSettings = getResources().getStringArray(R.array.portal);
// create a Portal using the portal url from the array
portal = new Portal(portalSettings[1], true);
// set the credentials from the browser
portal.setCredential(oauthCred);

portal.addDoneLoadingListener(new Runnable() {
    @Override
    public void run() {
        // if portal is LOADED, save the map to the portal
        if (portal.getLoadStatus() == LoadStatus.LOADED) {
            // Save the map to an authenticated Portal, with specified title, tags, description, and thumbnail.
            // Passing 'null' as portal folder parameter saves this to users root folder.
            final ListenableFuture<PortalItem> saveAsFuture = MainActivity.mMap.saveAsAsync(portal, null, mTitle, mTagsList, mDescription, null);
            saveAsFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    // Check the result of the save operation.
                    try {
                        PortalItem newMapPortalItem = saveAsFuture.get();
                        Toast.makeText(getApplicationContext(), getString(R.string.map_successful), Toast.LENGTH_SHORT).show();
                    } catch (InterruptedException | ExecutionException e) {
                        // If saving failed, deal with failure depending on the cause...
                        Log.e("Exception", e.toString());
                    }
                }
            });
        }
    }
});
portal.loadAsync();
```
