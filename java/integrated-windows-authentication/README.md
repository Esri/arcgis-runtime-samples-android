# Integrated Windows Authentication

Uses Windows credentials to access services hosted on a portal secured with Integrated Windows Authentication (IWA).

![Integrated windows authentication App](integrated-windows-authentication.png)

## Use case

IWA, which is built into Microsoft Internet Information Server (IIS), works well for intranet applications, but isn't always practical for internet apps.

## How to use the sample

1. Enter the URL to your IWA-secured portal in the edit text view at the top of the screen.
2. Tap either the "Search Public" (which will search for portals on www.arcgis.com) or "Search Secure" (which will search your IWA-secured portal), for web maps stored on the portal.
3. If you tap "Search Secure", you will be prompted for a username (including domain, such as username@DOMAIN or domain\username), and password.
4. If you authenticate successfully, portal item results will display in the recycler view.
5. Tap a web map item to display it in the map view.

## How it works

1. The `AuthenticationManager` object is configured with a challenge handler that will prompt for a Windows login (username including domain, and password) if a secure resource is encountered.
2. When a search for portal items is performed against an IWA-secured portal, the challenge handler creates an `UserCredential` object from the information entered by the user.
3. If the user authenticates, the search returns a list of web maps from `PortalItem` objects and the user can select one to display as an `ArcGISMap`.

## Relevant API

* AuthenticationManager
* Portal
* UserCredential

#### Tags
Cloud and Portal