# Portal user info

Retrieve a user's details via a Portal.

![Image of access portal user info](portal-user-info.png)

## Use case

This portal information can be used to provide a customized UI experience for the user. For example, you can show a thumbnail next to their username in the header of an application to indicate that they are currently logged in. Additionally, apps such as Collector and Explorer use this functionality to integrate with Portal.

## How to use the sample

When prompted, enter your ArcGIS Online credentials.

## How it works

1. Create a `Portal`, requesting an URL and requiring login.
2. When the app launches, the portal is loaded, which triggers an authentication challenge.
3. Display a login screen with `AuthenticationView`.
4. Upon successful login, get a `PortalUser` using `portal.user`. Get user attributes using:
    * `portalUser.fullName`
    * `portalUser.email`
    * `portalUser.created`
5.  Load a thumbnail image using `portalUser.fetchThumbnailAsync()`
  
## Relevant API

* AuthenticationManager
* AuthenticationManager.CredentialCache
* DefaultAuthenticationChallengeHandler
* PortalInfo
* PortalUser

## About the data

This sample signs into your ArcGIS online account and displays the user's profile information.

## Tags

account, avatar, bio, cloud and portal, email, login, picture, profile, user, username
