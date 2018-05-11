# Portal User Info
Use the `DefaultAuthenticationChallengeHandler` class to take care of showing an authentication dialog for logging into a `Portal`. Once authenticated against the portal, the app displays information about the authenticated user's profile.

![Portal User Info App](portal-user-info.png) 

## How to use the sample
When prompted, enter your ArcGIS Online credentials.

## How it works
The `DefaultAuthenticationChallengeHandler` is the default implementation of the `AuthenticationChallengeHandler` interface to handle  all security types that ArcGIS supports (including OAuth).  This sample uses it to authenticate with a portal.  This is set on the `AuthenticationManager`. To authenticate the user you create a `Portal` object, provide the essential credentials and then asynchronously load the portal as shown below:

```java
// Set the DefaultAuthenticationChallengeHandler to allow authentication with the portal.
DefaultAuthenticationChallengeHandler handler = new DefaultAuthenticationChallengeHandler(this);
AuthenticationManager.setAuthenticationChallengeHandler(handler);
// Set loginRequired to true always prompt for credential,
// When set to false to only login if required by the portal
final Portal portal = new Portal("http://www.arcgis.com", true);
```

The `Portal` class follows the loadable pattern and includes listeners to monitor the status of loading the portal.

```java
portal.addDoneLoadingListener(new Runnable() {
    @Override
    public void run() {
        if (portal.getLoadStatus() == LoadStatus.LOADED) {
        // loaded
    }
});
```

Once the portal has loaded you can access information about the portal and authenticated user.  

```java
// Get the authenticated portal user
PortalUser user = portal.getUser();
// get the users full name
String userName = user.getFullName();
```

## Relevant API
* AuthenticationManager
* DefaultAuthenticationChallengeHandler
* PortalInfo
* PortalUser

#### Tags
Cloud and Portal
