# Portal user info

Retrieve a user's details via a Portal.

![Image of access portal user info](portal-user-info.png)

## Use case

This portal information can be used to provide a customized UI experience for the user. For example, you can show a thumbnail next to their username in the header of an application to indicate that they are currently logged in. Additionally, apps such as Collector and Explorer use this functionality to integrate with Portal.

## How to use the sample

When prompted, enter your ArcGIS Online credentials.

## How it works

1. A `Portal` is created, and supplied a `Credential` which uses OAuth in user mode. 
2. When the app launches, the portal is loaded, which triggers an authentication challenge.
3. An `AuthenticationView` listens to the challenge and displays a login screen to allow user credentials to be entered.
4. Upon success, get a `PortalUser` using `portal.getUser()`. Get user attributes using:
    - `portalUser.getFullName()`
    - `portalUser.getEmail()`
    - `portalUser.getStartDate()`
5.  Load a thumbnail image using `portalUser.fetchThumbnailAsync()`
  

## Relevant API

* AuthenticationManager
* AuthenticationView
* Credential
* DefaultAuthenticationChallengeHandler
* PortalInfo
* PortalUser

## About the data

This sample signs into your ArcGIS online account and displays the user's profile information.

## Tags

account, avatar, bio, cloud and portal, email, login, picture, profile, user, username