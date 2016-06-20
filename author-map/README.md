# Author a map
This sample demonstrates how to author a map and save it to your portal

![screenshot1](https://cloud.githubusercontent.com/assets/12448081/16025921/69544758-317f-11e6-9417-de4d561064bf.png) ![screenshot2](https://cloud.githubusercontent.com/assets/12448081/16025943/89f2ff7c-317f-11e6-9063-82dd7139458e.png)
![screenshot3](https://cloud.githubusercontent.com/assets/12448081/16026079/93dbfaa6-3180-11e6-9a89-9e1513dff881.png) ![screenshot4](https://cloud.githubusercontent.com/assets/12448081/16026092/a463059a-3180-11e6-84a6-6814515f3287.png)
#How to use the sample
Open the drawer by tapping on the Drawer Toggle or sliding right from the left of the screen. Choose basemap and operational layers to author a map. Tap on save menu option to save the map. You will be required to login and provide a title, tags and description (optional) for the map.

#Features

* OAuthLoginManager
* Portal
* SaveAsAsync

# How it works

The sample uses a pre-populated list of basemaps and layers to create a ```Map``` and add operational layers on it. The authentication is handled by the ```OAuthLoginManager``` and upon successful response the credentials are extracted from the intent using ```fetchOAuthTokenCredentialAsync```. The fetched credentials are used in the ```Portal``` to save the map using ```saveAsAsync``` method

```java
            String[] portalSettings = getResources().getStringArray(R.array.portal);
            // create a Portal using the portal url from the array
            portal = new Portal(portalSettings[1], true);
            // set the credentials from the browser
            portal.setCredential(oauthCred);

            portal.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    Log.d("Portal", portal.getLoadStatus().name());
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
