# Integrated Windows authentication

Connect to an IWA secured Portal and search for maps.

![Image of integrated windows authentication](integrated-windows-authentication.png)

## Use case

Your organization might use Integrated Windows Authentication (IWA) to secure ArcGIS Enterprise. This can be useful because the same credentials used to log into your work computer and network can be used to authenticate with ArcGIS. IWA is built into Microsoft Internet Information Server (IIS) and works well for intranet applications but isn't always practical for internet apps.


## How to use the sample

Enter the URL to your IWA-secured portal. Tap the button to search for web maps stored on the portal. You will be prompted for a user name, password, and domain. If you authenticate successfully, portal item results will display in the list. Select a web map item to display it in the map view.

## How it works

1. The `AuthenticationManager` object is configured with a challenge handler that will prompt for a Windows login (username, password, and domain) if a secure resource is encountered.
2. When a search for portal items is performed against an IWA-secured portal, the challenge handler creates an `UserCredential` object from the information entered by the user.
3. If the user authenticates, the search returns a list of web maps (`PortalItem`) and the user can select one to display as a `ArcGISMap`.

## Relevant API

* UserCredential
* Portal
* AuthenticationManager

## About the data

This sample searches for web map portal items on a secure portal. To successfully run the sample, you need:
 * Access to a portal secured with Integrated Windows Authentication that contains one or more web map items.
 * A login that grants you access to the portal.

## Additional information

More information about IWA and its use with ArcGIS can be found at the following links:
 * [IWA - Wikipedia](https://en.wikipedia.org/wiki/Integrated_Windows_Authentication)
 * [Use Integrated Windows Authentication with your portal](http://enterprise.arcgis.com/en/portal/latest/administer/windows/use-integrated-windows-authentication-with-your-portal.htm)

## Tags

authentication, security, Windows