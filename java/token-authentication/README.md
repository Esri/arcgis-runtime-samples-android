# Token authentication

This sample demonstrates how to access a web map that is secured with ArcGIS token-based authentication.

![Token authentication](token-authentication.png)

## How to use the sample

1. Once you launch the app, you will be challenged for an ArcGIS Online login to view the protected map service.
1. Enter a user name and password for an ArcGIS Online named user account (such as your ArcGIS for Developers account).
1. If you authenticate successfully, the protected map service will display in the map.


## How it works

1. Create an `AuthenticationChallengeHandler` using the `DefaultAuthenticationChallengeHandler` to handle authentication challenges sent by the protected map service, `new DefaultAuthenticationChallengeHandler(Context)`.
1. Set the `AuthenticationChallengeHandler` used by the `AuthenticationManager`, `AuthenticationManager.setAuthenticationChallengeHandler(AuthenticationChallengeHandler)`
1. Create a portal to ArcGIS Online, `new Portal("portal url")`
1. Create a portal item for the protected web map using the portal and Item ID of the protected map service, `new PortalItem(Portal, "map service ID")`
1. Create a map to display in the map view using the portal item, `new ArcGISMap(PortalItem)`
1. Set the map to display in the map view, `MapView.setMap(ArcGISMap)`

## Relevant API

* AuthenticationManager
* AuthenticationChallengeHandler
* DefaultAuthenticationChallengeHandler
* Portal
* PortalItem
* Map
* MapView

#### Tags
Authentication
Security