# Portal User Info
Demonstrates use of the `DefaultAuthenticationChallengeHandler` class to take care of showing an authentication dialog for logging into a `Portal`. Once authenticated against the portal, the app displays information about the authenticated user's profile.

![Portal User Info App](portal-user-info.png) 

## How to the use the sample
Start the sample and input your ArcGISOnline account information.

## How it works
The `DefaultAuthenticationChallengeHandler` is the default implementation of the `AuthenticationChallengeHandler` interface to handle  all security types that ArcGIS supports (including OAuth).  This sample uses it to authenticate with a portal.  This is set on the `AuthenticationManager`. To authenticate the user you create a `Portal` object, provide the essential credentials and then asynchronously load the portal.

## Relevant API
* AuthenticationManager
* DefaultAuthenticationChallengeHandler
* PortalInfo
* PortalUser

#### Tags
Cloud and Portal
